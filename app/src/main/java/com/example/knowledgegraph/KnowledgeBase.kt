package com.example.knowledgegraph
// 1000398288919-vvmvp4lav37a7u0d6168orsg45mr4a8f.apps.googleusercontent.com
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.drawscope.DrawScope
//import androidx.compose.ui.graphics.drawscope.drawContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

//keyboard
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

//for calendar
import android.content.Context
import android.provider.CalendarContract
import java.util.*
import androidx.compose.ui.platform.LocalContext


data class KnowledgeTriple(val subject: String, val predicate: String, val obj: String)



fun getCalendarTriples(context: Context): List<KnowledgeTriple> {
    val triples = mutableListOf<KnowledgeTriple>()
    val projection = arrayOf(
        CalendarContract.Events.TITLE,
        CalendarContract.Events.DTSTART,
        CalendarContract.Events.EVENT_LOCATION
    )

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

    if (triples.isEmpty()) {
        Log.w("CalendarDebug", " No events found in the calendar.")
    }

    return triples
}


@Composable
fun KnowledgeBase(){
    var subject by remember { mutableStateOf("")}
    var predicate by remember { mutableStateOf("") }
    var obj by remember { mutableStateOf("")}
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

//    We need to store this triple, creating a val
    val knowledgeGraph = remember { mutableStateListOf<KnowledgeTriple>()}
//
    val context = LocalContext.current
    val calendarTriples = remember { mutableStateListOf<KnowledgeTriple>() }

// Extract events and populate knowledgeGraph once
    LaunchedEffect(Unit) {
        val fromCalendar = getCalendarTriples(context)
        calendarTriples.clear()
        calendarTriples.addAll(fromCalendar)

        // Add only new items to knowledgeGraph
        fromCalendar.forEach {
            if (!knowledgeGraph.contains(it)) {
                knowledgeGraph.add(it)
            }
        }
    }
    // storing the value
    Column(modifier = Modifier.padding(16.dp)){
//        TextField(value = subject, onValueChange = { subject = it}, label = {Text("Subject")})
        TextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = {
                    focusManager.clearFocus()
                }) {
                    Icon(Icons.Default.Check, contentDescription = "Done")
                }
            }
        )
//        TextField(value = predicate, onValueChange = { predicate = it }, label = {Text("Predicate")})
        TextField(
            value = predicate,
            onValueChange = { predicate = it },
            label = { Text("Predicate") },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = {
                    focusManager.clearFocus()
                }) {
                    Icon(Icons.Default.Check, contentDescription = "Done")
                }
            }
        )
        TextField(
            value = obj,
            onValueChange = { obj = it },
            label = { Text("Object") },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = {
                    focusManager.clearFocus()
                }) {
                    Icon(Icons.Default.Check, contentDescription = "Done")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
//        adding this triple to the list
        if (knowledgeGraph.isNotEmpty()) {
            Text("Knowledge Graph:", style = MaterialTheme.typography.titleMedium)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = 400.dp)
            ) {
                KnowledgeGraph(triples = knowledgeGraph)
            }
        }
    }

}


@Composable
fun KnowledgeGraph(triples: List<KnowledgeTriple>) {
    val textMeasurer = rememberTextMeasurer()

    // Fixed canvas height to show ~3-4 triples safely
    val canvasHeight = 1200.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(canvasHeight)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val nodeRadius = 100f
            val spacing = 300f
            val startX = 200f
            var currentY = 300f

            val maxTriplesToDraw = 3 // Avoid overflow
            triples.take(maxTriplesToDraw).forEach { triple ->
                val subjectOffset = Offset(startX, currentY)
                val objectOffset = Offset(startX + spacing, currentY)

                drawCircle(Color.Yellow, radius = nodeRadius, center = subjectOffset)
                drawCircle(Color.Cyan, radius = nodeRadius, center = objectOffset)

                drawLine(
                    color = Color.Black,
                    start = subjectOffset + Offset(100f, 0f),
                    end = objectOffset - Offset(100f, 0f),
                    strokeWidth = 4f
                )

                drawText(
                    textMeasurer = textMeasurer,
                    text = triple.subject,
                    topLeft = Offset(subjectOffset.x - 50f, subjectOffset.y - 50f),
                    style = TextStyle(color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                )

                drawText(
                    textMeasurer = textMeasurer,
                    text = triple.obj,
                    topLeft = Offset(objectOffset.x - 50f, objectOffset.y - 50f),
                    style = TextStyle(color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                )

                drawText(
                    textMeasurer = textMeasurer,
                    text = triple.predicate,
                    topLeft = Offset(subjectOffset.x + 150f, subjectOffset.y - 50f),
                    style = TextStyle(color = Color.Gray, fontSize = 8.sp)
                )

                currentY += 400f
            }
        }
    }
}

//@Composable
//fun KnowledgeGraph(triples: List<KnowledgeTriple>) {
//    val textMeasurer = rememberTextMeasurer()
//    val nodeHeight = 400f
//    val canvasHeight = remember(triples) { (triples.size * nodeHeight).dp + 300.dp }
//
//    // Cap max height to a safe value (optional)
//    val safeCanvasHeight = if (canvasHeight > 5000.dp) 5000.dp else canvasHeight
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .heightIn(min = 400.dp, max = safeCanvasHeight)
//            .verticalScroll(rememberScrollState())
//    ) {
//        Canvas(modifier = Modifier
//            .fillMaxWidth()
//            .height(safeCanvasHeight)
//        ) {
//            val nodeRadius = 100f
//            val spacing = 300f
//            val startX = 200f
//            var currentY = 300f
//
//            for (triple in triples) {
//                val subjectOffset = Offset(startX, currentY)
//                val objectOffset = Offset(startX + spacing, currentY)
//
//                drawCircle(Color.Yellow, radius = nodeRadius, center = subjectOffset)
//                drawCircle(Color.Cyan, radius = nodeRadius, center = objectOffset)
//
//                drawLine(
//                    color = Color.Black,
//                    start = subjectOffset + Offset(100f, 0f),
//                    end = objectOffset - Offset(100f, 0f),
//                    strokeWidth = 4f
//                )
//
//                drawText(
//                    textMeasurer = textMeasurer,
//                    text = triple.subject,
//                    topLeft = Offset(subjectOffset.x - 50f, subjectOffset.y - 50f),
//                    style = TextStyle(color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
//                )
//
//                drawText(
//                    textMeasurer = textMeasurer,
//                    text = triple.obj,
//                    topLeft = Offset(objectOffset.x - 50f, objectOffset.y - 50f),
//                    style = TextStyle(color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
//                )
//
//                drawText(
//                    textMeasurer = textMeasurer,
//                    text = triple.predicate,
//                    topLeft = Offset(subjectOffset.x + 150f, subjectOffset.y - 50f),
//                    style = TextStyle(color = Color.Gray, fontSize = 8.sp)
//                )
//
//                currentY += nodeHeight
//            }
//        }
//    }
//}

