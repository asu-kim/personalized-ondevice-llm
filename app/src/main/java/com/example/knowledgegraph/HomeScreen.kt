package com.example.knowledgegraph

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf


//for the app selections

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.runtime.LaunchedEffect

import android.app.Activity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(name: String, navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    var statusMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val embeddingModel = EmbeddingModel(context)
        withContext(Dispatchers.IO) {
            val model = EmbeddingModel(context)
            val result = model.generateEmbeddingsFromKG()
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
                    // Prevents going back
                    popUpTo("home/$name") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
        Spacer(modifier = Modifier.height(64.dp))
        // Creating the apps that need to be selected
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

        // calling the knowledge base here
        KnowledgeBase()
    }
}

