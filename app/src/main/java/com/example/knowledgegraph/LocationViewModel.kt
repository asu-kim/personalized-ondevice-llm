//package com.example.knowledgegraph
//
//import android.Manifest
//import android.app.Application
//import android.content.Context
//import android.content.SharedPreferences
//import android.location.Geocoder
//import android.util.Log
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.viewModelScope
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//import androidx.compose.runtime.mutableStateListOf
//import androidx.compose.runtime.snapshots.SnapshotStateList
//import com.google.android.gms.location.LocationServices
//import androidx.core.content.edit
//
//class LocationViewModel(application: Application) : AndroidViewModel(application) {
//
//    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
//    private var lastAddress: String? = null
//
//    private val _locationTriples = mutableStateListOf<KnowledgeTriple>()
//    val locationTriples: SnapshotStateList<KnowledgeTriple> = _locationTriples
//
//    init {
//        startLocationUpdates()
//    }
//
//    private fun startLocationUpdates() {
//        viewModelScope.launch {
//            while (true) {
//                val context = getApplication<Application>().applicationContext
//                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
//                    == android.content.pm.PackageManager.PERMISSION_GRANTED
//                ) {
//                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//                        location?.let {
//                            val geocoder = Geocoder(context, Locale.getDefault())
//                            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
//                            Log.d("LOCATION_LATLON", "Lat: ${it.latitude}, Lon: ${it.longitude}")
//                            Log.d("LATITUDE", "$addresses")
//                            if (!addresses.isNullOrEmpty()) {
//                                val address = addresses[0].getAddressLine(0)
//                                val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
//
//                                if (address != lastAddress) {
//                                    lastAddress = address
//                                    _locationTriples.add(KnowledgeTriple("User", "location", address))
//                                    _locationTriples.add(KnowledgeTriple("User", "starts at", dateTime))
//
//                                    // Save to CSV
//                                    saveTriplesToCSV(context, _locationTriples)
//
//                                    // Optionally save to SharedPreferences
//                                    val sharedPrefs = context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
//                                    sharedPrefs.edit {
//                                        putString("last_location", address)
//                                        putString("last_timestamp", dateTime)
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                delay(15_000) // check every 15 seconds
//            }
//        }
//    }
//}
package com.example.knowledgegraph

import android.Manifest
import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val fused = LocationServices.getFusedLocationProviderClient(application)

    private var lastAddress: String? = null

    // Public, observed by KnowledgeBase
    private val _locationTriples = androidx.compose.runtime.mutableStateListOf<KnowledgeTriple>()
    val locationTriples: androidx.compose.runtime.snapshots.SnapshotStateList<KnowledgeTriple> = _locationTriples

    init {
        // background gentle polling (optional)
        startLightPolling()
    }

    /** Call this to force an immediate location read (KnowledgeBase will invoke once on start). */
    fun refreshLocation() {
        viewModelScope.launch {
            readOnceAndEmit()
        }
    }

    /** A very light 60s polling to pick up moves without hammering battery. */
    private fun startLightPolling() {
        viewModelScope.launch {
            while (true) {
                readOnceAndEmit()
                delay(60_000) // 60s
            }
        }
    }

    private suspend fun readOnceAndEmit() {
        val ctx = getApplication<Application>().applicationContext

        val fineGranted = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            Log.w("LocationVM", "Location permission not granted; skipping.")
            return
        }

        try {
            // Prefer fresh reading; fall back to lastLocation if needed.
            val cts = CancellationTokenSource()
            val loc = fused.getCurrentLocation(
                if (fineGranted) Priority.PRIORITY_BALANCED_POWER_ACCURACY else Priority.PRIORITY_PASSIVE,
                cts.token
            ).awaitNullable()
                ?: fused.lastLocation.awaitNullable()

            if (loc == null) {
                Log.w("LocationVM", "No location available yet.")
                return
            }

            val geocoder = Geocoder(ctx, Locale.getDefault())
            val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
            if (addresses.isNullOrEmpty()) {
                Log.w("LocationVM", "Geocoder returned empty.")
                return
            }

            val address = addresses[0].getAddressLine(0)
            if (address.isNullOrBlank()) return

            // Only emit if changed
            if (address != lastAddress) {
                lastAddress = address

                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                _locationTriples.add(KnowledgeTriple("User", "location", address))
                _locationTriples.add(KnowledgeTriple("User", "starts at", timestamp))

                Log.d("LocationVM", "Emitted location: $address at $timestamp (triples=${_locationTriples.size})")
            }
        } catch (t: Throwable) {
            Log.e("LocationVM", "Error getting location: ${t.message}", t)
        }
    }
}

/* --- tiny await helper for Tasks without bringing full coroutines-play-services --- */
private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitNullable(): T? =
    kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) cont.resume(task.result, null)
            else cont.resume(null, null)
        }
    }