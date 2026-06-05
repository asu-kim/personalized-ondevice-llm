package com.example.knowledgegraph

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
import java.io.File

@Composable
fun HomeScreen(name: String, navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val embeddingModel = remember { EmbeddingModel(context) }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val result = embeddingModel.generateEmbeddingsFromKG()
            withContext(Dispatchers.Main) {
                statusMessage = result
            }
        }

        if (
            activity != null &&
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.READ_CALENDAR),
                1001
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome to the Home Screen, $name!",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

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

        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = "The Knowledge Graph is saved as a .csv file",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(64.dp))

        if (statusMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = statusMessage!!,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

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
        ) {
            Text("Regenerate Embeddings Now")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                navController.navigate("csv_viewer")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Knowledge Graph")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Delete Knowledge Graph")
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to delete the Knowledge Graph files (CSV and Vector)? This cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            val csvFile = File(context.getExternalFilesDir(null), "Knowledge_graph.csv")
                            val vecFile = File(context.getExternalFilesDir(null), "Knowledge_graph.vec")
                            val csvDeleted = if (csvFile.exists()) csvFile.delete() else true
                            val vecDeleted = if (vecFile.exists()) vecFile.delete() else true
                            
                            statusMessage = if (csvDeleted && vecDeleted) {
                                "Files deleted successfully."
                            } else {
                                "Failed to delete files."
                            }
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        KnowledgeBase()
    }
}