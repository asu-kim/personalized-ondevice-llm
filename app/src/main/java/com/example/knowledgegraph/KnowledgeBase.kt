package com.example.knowledgegraph

import android.content.Context
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

import android.content.Intent
import android.net.Uri

data class KnowledgeTriple(val subject: String, val predicate: String, val obj: String)

fun saveTriplesToCSV(context: Context, triples: List<KnowledgeTriple>, fileName: String = "Knowledge_graph.csv") {
    val csvHeader = "Subject,Predicate,Object"
    val csvBody = triples.joinToString("\n") { "\"${it.subject}\",\"${it.predicate}\",\"${it.obj}\"" }
    val csvContent = "$csvHeader\n$csvBody"


    val file = File(context.getExternalFilesDir(null), fileName)
    file.writeText(csvContent)

    Log.d("CSV", "Saved to: ${file.absolutePath}")

    context.grantUriPermission(
        "ai.mlc.mlcchat",  // MLC Chat package name
        Uri.parse("content://com.example.knowledgegraph.kgprovider"),
        Intent.FLAG_GRANT_READ_URI_PERMISSION
    )
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
        CalendarContract.Events.TITLE,
        CalendarContract.Events.DTSTART,
        CalendarContract.Events.EVENT_LOCATION,
        CalendarContract.Events.CALENDAR_ID
    )

    // Optional: Replace with your actual Google account calendar ID or account name
//    val selection = "${CalendarContract.Events.ACCOUNT_NAME} = ?"
//    val selectionArgs = arrayOf("asu.kim.2024@gmail.com")
    val selection = "${CalendarContract.Events.CALENDAR_ID} = ?"
    val selectionArgs = arrayOf("4")

    val cursor = context.contentResolver.query(
        CalendarContract.Events.CONTENT_URI,
        projection,
        null,
        null,
        "${CalendarContract.Events.DTSTART} DESC"
    )

    cursor?.use {
        val titleIndex = it.getColumnIndex(CalendarContract.Events.TITLE)
        val timeIndex = it.getColumnIndex(CalendarContract.Events.DTSTART)
        val locationIndex = it.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)

        while (it.moveToNext()) {
            val title = it.getString(titleIndex)
            val startTime = it.getLong(timeIndex)
            val location = it.getString(locationIndex) ?: "unspecified"

            if (!title.isNullOrBlank()) {
                triples.add(KnowledgeTriple(title, "starts at", Date(startTime).toString()))
                triples.add(KnowledgeTriple(title, "location", location))

                Log.d("CalendarDebug", "Event: \"$title\" at ${Date(startTime)} in $location")
            }
        }
    }

    return triples
}

@Composable
fun KnowledgeBase() {
    var subject by remember { mutableStateOf("") }
    var predicate by remember { mutableStateOf("") }
    var obj by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val knowledgeGraph = remember { mutableStateListOf<KnowledgeTriple>() }
    val context = LocalContext.current
    val calendarTriples = remember { mutableStateListOf<KnowledgeTriple>() }

    LaunchedEffect(Unit) {
        logAvailableCalendars(context) // Log all calendars

        val fromCalendar = getCalendarTriples(context)
        calendarTriples.clear()
        calendarTriples.addAll(fromCalendar)

        // Only add distinct subjects to avoid drawing the same subject node twice
        val distinctTriples = fromCalendar.distinctBy { it.subject }
//        knowledgeGraph.clear()
        knowledgeGraph.addAll(fromCalendar)
        saveTriplesToCSV(context, knowledgeGraph)

    }

    Column(modifier = Modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))

        if (knowledgeGraph.isNotEmpty()) {
            Text("Knowledge Graph:", style = MaterialTheme.typography.titleMedium)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = 400.dp)
            ) {
                //KnowledgeGraph(triples = knowledgeGraph)
            }
        }
    }
}

@Composable
fun KnowledgeGraph(triples: List<KnowledgeTriple>) {
    val textMeasurer = rememberTextMeasurer()
    val grouped = triples.groupBy { it.subject }

    val rowHeight = 100f
    val maxObjects = grouped.maxOfOrNull { it.value.size } ?: 1
    val canvasHeightDp = ((grouped.size * (rowHeight * (maxObjects + 1))) + 200).dp
    val safeCanvasHeight = if (canvasHeightDp > 8000.dp) 8000.dp else canvasHeightDp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 400.dp, max = safeCanvasHeight)
            .verticalScroll(rememberScrollState())
    ) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(safeCanvasHeight)
        ) {
            val nodeRadius = 50f
            val spacing = 350f
            val startX = 200f
            var currentY = 200f

            for ((subject, group) in grouped) {
                val subjectOffset = Offset(startX, currentY)

                drawCircle(Color.Yellow, nodeRadius, subjectOffset)

                drawText(
                    textMeasurer,
                    text = subject,
                    topLeft = subjectOffset - Offset(40f, 60f),
                    style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                )

                group.forEachIndexed { index, triple ->
                    val objectY = currentY + index * rowHeight
                    val objectOffset = Offset(startX + spacing, objectY)

                    drawCircle(Color.Cyan, nodeRadius, objectOffset)

                    drawLine(
                        color = Color.Black,
                        start = subjectOffset + Offset(50f, 0f),
                        end = objectOffset - Offset(50f, 0f),
                        strokeWidth = 3f
                    )

                    drawText(
                        textMeasurer,
                        text = triple.obj,
                        topLeft = objectOffset - Offset(40f, 40f),
                        style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                    )

                    drawText(
                        textMeasurer,
                        text = triple.predicate,
                        topLeft = Offset(subjectOffset.x + 150f, objectY - 50f),
                        style = TextStyle(fontSize = 8.sp, color = Color.DarkGray)
                    )
                }

                currentY += (group.size + 1) * rowHeight
            }
        }
    }
}