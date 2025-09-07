//package com.example.knowledgegraph
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.setValue
//import androidx.compose.runtime.mutableStateOf
//
//
////for the app selections
//
//import android.content.pm.PackageManager
//import android.graphics.drawable.Drawable
//import androidx.compose.ui.platform.LocalContext
//
//import androidx.compose.runtime.LaunchedEffect
//
//import android.app.Activity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//
//
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
//@Composable
//fun HomeScreen(name: String, navController: NavController) {
//    val context = LocalContext.current
//    val activity = context as? Activity
//    var statusMessage by remember { mutableStateOf<String?>(null) }
//    LaunchedEffect(Unit) {
//        val embeddingModel = EmbeddingModel(context)
//        withContext(Dispatchers.IO) {
//            val model = EmbeddingModel(context)
//            val result = model.generateEmbeddingsFromKG()
//            withContext(Dispatchers.Main) {
//                statusMessage = result
//            }
//        }
//        if (
//            activity != null &&
//            ContextCompat.checkSelfPermission(
//                context,
//                android.Manifest.permission.READ_CALENDAR
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                activity,
//                arrayOf(android.Manifest.permission.READ_CALENDAR),
//                1001
//            )
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(32.dp)
//    ) {
//        Spacer(modifier = Modifier.height(32.dp))
//        Text(
//            text = "Welcome to the Home Screen, $name!",
//            style = MaterialTheme.typography.headlineMedium
//        )
//
//        Spacer(modifier = Modifier.height(32.dp))
//
//        Button(
//            onClick = {
//                navController.navigate("login") {
//                    // Prevents going back
//                    popUpTo("home/$name") { inclusive = true }
//                }
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Logout")
//        }
//        Spacer(modifier = Modifier.height(64.dp))
//        // Creating the apps that need to be selected
//        Text(
//            text = "The Knowledge Graph is saved as a .csv file",
//            style = MaterialTheme.typography.titleMedium
//        )
//        Spacer(modifier = Modifier.height(64.dp))
//        if (statusMessage != null) {
//            Spacer(modifier = Modifier.height(16.dp))
//            Text(
//                text = statusMessage!!,
//                style = MaterialTheme.typography.bodyLarge,
//                color = MaterialTheme.colorScheme.primary
//            )
//        }
//
//        // calling the knowledge base here
//        KnowledgeBase()
//    }
//}
//
package com.example.knowledgegraph

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(name: String, navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity

    // ViewModel (for location refresh)
    val locationViewModel: LocationViewModel = viewModel()

    // Init the embedding model ONCE
    val embeddingModel = remember { EmbeddingModel(context) }

    // For background work
    val scope = rememberCoroutineScope()

    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Ask for calendar + location permissions once here
    LaunchedEffect(Unit) {
        if (activity == null) return@LaunchedEffect

        val needCalendar = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CALENDAR
        ) != PackageManager.PERMISSION_GRANTED

        val needFine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED

        val needCoarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED

        val toAsk = buildList {
            if (needCalendar) add(Manifest.permission.READ_CALENDAR)
            if (needFine) add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (needCoarse) add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }.toTypedArray()

        if (toAsk.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, toAsk, /*reqCode=*/1001)
        }

        // Kick a location refresh (LocationViewModel internally checks perms)
        locationViewModel.refreshLocation()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to the Home Screen, $name!",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate("login") {
                    popUpTo("home/$name") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "The Knowledge Graph is saved to: Knowledge_graph.csv",
            style = MaterialTheme.typography.titleMedium
        )

        if (statusMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = statusMessage!!,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Manual regenerate embeddings button (optional)
        OutlinedButton(
            onClick = {
                statusMessage = "Generating embeddings..."
                scope.launch {
                    val msg = withContext(Dispatchers.Default) {
                        embeddingModel.generateEmbeddingsFromKG()
                    }
                    statusMessage = msg
                }
            }
        ) { Text("Regenerate Embeddings Now") }

        Spacer(modifier = Modifier.height(24.dp))

        // Build/overwrite Knowledge_graph.csv (calendar + location).
        // When CSV updates, regenerate embeddings so they reflect the latest file.
        KnowledgeBase(
            onCsvUpdated = {
                statusMessage = "Generating embeddings from updated CSV..."
                scope.launch {
                    val msg = withContext(Dispatchers.Default) {
                        embeddingModel.generateEmbeddingsFromKG()
                    }
                    statusMessage = msg
                }
            }
        )
    }
}