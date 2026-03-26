package com.example.knowledgegraph

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.provider.CalendarContract
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.drawText
import java.util.*
import java.io.File
import java.io.FileOutputStream
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay



import java.text.Normalizer



data class KnowledgeTriple(val subject: String, val predicate: String, val obj: String)


fun saveTriplesToCSV(context: Context, triples: List<KnowledgeTriple>, fileName: String = "Knowledge_graph.csv") {
    val file = File(context.getExternalFilesDir(null), fileName)
    val existingLines = if (file.exists()) file.readLines().toMutableSet() else mutableSetOf()

    val newLines = triples.map { "\"${it.subject}\",\"${it.predicate}\",\"${it.obj}\"" }
        .filterNot { existingLines.contains(it) }

    if (newLines.isEmpty()) return

    val needsHeader = existingLines.isEmpty()
    file.appendText(buildString {
        if (needsHeader) append("Subject,Predicate,Object\n")
        newLines.forEach { append("$it\n") }
    })

    Log.d("CSV", "Appended ${newLines.size} new triples to: ${file.absolutePath}")
}


fun logAvailableCalendars(context: Context) {
    val projection = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.NAME,
        CalendarContract.Calendars.ACCOUNT_NAME,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
    )

    val cursor = context.contentResolver.query(
        CalendarContract.Calendars.CONTENT_URI,
        projection,
        null,
        null,
        null
    )

    cursor?.use {
        val idIndex = it.getColumnIndex(CalendarContract.Calendars._ID)
        val nameIndex = it.getColumnIndex(CalendarContract.Calendars.NAME)
        val accountIndex = it.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)
        val displayNameIndex = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)

        while (it.moveToNext()) {
            val id = it.getLong(idIndex)
            val name = it.getString(nameIndex)
            val account = it.getString(accountIndex)
            val displayName = it.getString(displayNameIndex)
            Log.d("CalendarInfo", "ID: $id, Name: $name, Account: $account, Display Name: $displayName")
        }
    }
}



fun getCalendarTriples(context: Context): List<KnowledgeTriple> {
    val triples = mutableListOf<KnowledgeTriple>()

    val projection = arrayOf(
        CalendarContract.Instances.TITLE,
        CalendarContract.Instances.BEGIN,
        CalendarContract.Instances.END,
        CalendarContract.Instances.EVENT_LOCATION
    )

    val now = System.currentTimeMillis()
    val oneYearFromNow = now + 365L * 24 * 60 * 60 * 1000 // 1 year ahead

    val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
    ContentUris.appendId(builder, now)
    ContentUris.appendId(builder, oneYearFromNow)

    val uri = builder.build()
    val cursor = context.contentResolver.query(
        uri,
        projection,
        null,
        null,
        "${CalendarContract.Instances.BEGIN} ASC"
    )

    cursor?.use {
        val titleIndex = it.getColumnIndex(CalendarContract.Instances.TITLE)
        val startTimeIndex = it.getColumnIndex(CalendarContract.Instances.BEGIN)
        val endTimeIndex = it.getColumnIndex(CalendarContract.Instances.END)
        val locationIndex = it.getColumnIndex(CalendarContract.Instances.EVENT_LOCATION)

        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
        isoFormatter.timeZone = TimeZone.getDefault()

        while (it.moveToNext()) {
            val title = it.getString(titleIndex)
            val startTime = it.getLong(startTimeIndex)
            val endTime = it.getLong(endTimeIndex)
            val location = it.getString(locationIndex) ?: "unspecified"
            val startIso = isoFormatter.format(Date(startTime))
            val endIso = isoFormatter.format(Date(endTime))
            val intervalIso = "$startIso/$endIso"

            if (!title.isNullOrBlank()) {
                triples.add(KnowledgeTriple(title, "at", intervalIso))
                if (!location.isNullOrBlank()) {
                    triples.add(KnowledgeTriple(title, "location", location))
                }
            }
        }
    }

   
    return triples.distinctBy { Triple(it.subject, it.predicate, it.obj) }
}

@Composable
fun KnowledgeBase(locationViewModel: LocationViewModel = viewModel()) {
    var subject by remember { mutableStateOf("") }
    var predicate by remember { mutableStateOf("") }
    var obj by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val lastUpdateDate  = remember {mutableStateOf<String?>(null)}
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val knowledgeGraph = remember { mutableStateListOf<KnowledgeTriple>() }
    val context = LocalContext.current
    val calendarTriples = remember { mutableStateListOf<KnowledgeTriple>() }
    val cacheTriples = remember { mutableStateListOf<KnowledgeTriple>()}
    val locationViewModel: LocationViewModel = viewModel()


    LaunchedEffect(Unit) {
        val permissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            (context as? Activity)?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.READ_CALENDAR),
                    102
                )
            }
            return@LaunchedEffect
        }

     
        logAvailableCalendars(context)

        val fromCalendar = getCalendarTriples(context)
        calendarTriples.clear()
        calendarTriples.addAll(fromCalendar)

        val distinctTriples = fromCalendar.distinctBy { it.subject }
       
        Log.d ("DateDebug", "LastDate: ${lastUpdateDate.value}, Today: $today")
        if (lastUpdateDate.value != today) {
            cacheTriples.clear()
            cacheTriples.addAll(calendarTriples)
            knowledgeGraph.addAll(fromCalendar)
            knowledgeGraph.addAll(locationViewModel.locationTriples)
            saveTriplesToCSV(context, knowledgeGraph)


            lastUpdateDate.value = today
        } else {
            knowledgeGraph.addAll(cacheTriples)
            knowledgeGraph.addAll(locationViewModel.locationTriples)
        }
    }
    
    Column(modifier = Modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))

        if (knowledgeGraph.isNotEmpty()) {
            //Text("Knowledge Graph:", style = MaterialTheme.typography.titleMedium)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = 400.dp)
            ) {
                
            }
        }
    }
}
