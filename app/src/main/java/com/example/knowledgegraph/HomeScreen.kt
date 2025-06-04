package com.example.knowledgegraph

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.runtime.remember

//for the app selections

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import android.Manifest
import android.app.Activity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.location.Geocoder
import java.util.Locale

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable
)

@Composable
fun HomeScreen(name: String, navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity
    LaunchedEffect(Unit) {
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
        Spacer(modifier = Modifier.height(16.dp))
//        AppSelection()

        //GetCurrentLocationComposable()
        // calling the knowledge base here
        KnowledgeBase()
    }
}

