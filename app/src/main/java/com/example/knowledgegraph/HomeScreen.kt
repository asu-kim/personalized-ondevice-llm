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
            text = "Select all the application you choose to extract the data from:",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
//        AppSelection()
        // calling the knowledge base here
        KnowledgeBase()
    }
}

//@Composable
//fun AppSelection() {
//    val context = LocalContext.current
//    val packageManager = context.packageManager
//
//    // Step 1: Get matching intent handlers
//    val calendarIntent = remember {
//        Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
//    }
//    val emailIntent = remember {
//        Intent(Intent.ACTION_SENDTO).setData(Uri.parse("mailto:"))
//    }
//    val smsIntent = remember {
//        Intent(Intent.ACTION_SENDTO).setData(Uri.parse("smsto:"))
//    }
//
//
////    val matchingPackages = remember {
////        val calendarApps = packageManager.queryIntentActivities(calendarIntent, 0)
////        val emailApps = packageManager.queryIntentActivities(emailIntent, 0)
////        val smsApps = packageManager.queryIntentActivities(smsIntent, 0)
////
////        (calendarApps + emailApps + smsApps)
////            .map { it.activityInfo.packageName }
////            .distinct()
////    }
//
//    val supportedPackages = listOf(
//        "com.google.android.calendar",
//        "com.google.android.gm",
//        "com.google.android.keep",
//        "com.whatsapp",
//        "org.telegram.messenger"
//    )
//    val installedApps = remember {
//        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
//            .map {
//                AppInfo(
//                    name = packageManager.getApplicationLabel(it).toString(),
//                    packageName = it.packageName,
//                    icon = packageManager.getApplicationIcon(it)
//                )
//            }
//    }
////
////
////    val filteredApps = installedApps.filter { it.packageName in matchingPackages }
////    val installedApps = remember {
////        val launchIntent = Intent(Intent.ACTION_MAIN, null)
////        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER)
////
////        packageManager.queryIntentActivities(launchIntent, 0)
////            .map {
////                AppInfo(
////                    name = it.loadLabel(packageManager).toString(),
////                    packageName = it.activityInfo.packageName,
////                    icon = it.loadIcon(packageManager)
////                )
////            }
////    }
//
//// Filter manually cause the other was not working
//    val filteredApps = installedApps.filter { it.packageName in supportedPackages }
//
//    Column(modifier = Modifier.padding(16.dp)) {
//        Text("Select from supported apps:", style = MaterialTheme.typography.titleMedium)
//        Spacer(modifier = Modifier.height(8.dp))
//
//        LazyColumn {
//            items(filteredApps) { app ->
//                Text(text = app.name)
//                Spacer(modifier = Modifier.height(4.dp))
//            }
//        }
////        LazyColumn {
////            items(installedApps) { app ->
////                Text(text = "${app.name} (${app.packageName})")
////            }
////        }
//        LaunchedEffect(Unit) {
//            Log.d("AppDebug", "Installed App Packages:")
//            installedApps.forEach {
//                Log.d("AppDebug", it.packageName)
//            }
//        }
//    }
//}