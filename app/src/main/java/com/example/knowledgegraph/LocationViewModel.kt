package com.example.knowledgegraph

import android.Manifest
import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import androidx.core.content.edit

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private var lastAddress: String? = null
    private var currentStayId: String? = null

    private val _locationTriples = mutableStateListOf<KnowledgeTriple>()
    val locationTriples: SnapshotStateList<KnowledgeTriple> = _locationTriples

    init {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
        lastAddress = sharedPrefs.getString("last_location", null)
        currentStayId = sharedPrefs.getString("current_stay_id", null)
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        viewModelScope.launch {
            while (true) {
                val context = getApplication<Application>().applicationContext
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                        .addOnSuccessListener { location ->
                            if (location == null) {
                                Log.d("LocationViewModel", "Current location is null (GPS might be disabled or signal is weak)")
                                return@addOnSuccessListener
                            }
                            Log.d("LocationViewModel", "Location fetched: ${location.latitude}, ${location.longitude}")
                            viewModelScope.launch {
                                try {
                                    val addresses = withContext(Dispatchers.IO) {
                                        val geocoder = Geocoder(context, Locale.getDefault())
                                        geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                    }
                                    if (!addresses.isNullOrEmpty()) {
                                        val address = addresses[0].getAddressLine(0)
                                        val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                                        val checkInTriples = listOf(
                                            KnowledgeTriple("user", "located_at", address),
                                            KnowledgeTriple(address, "last_seen_at", dateTime)
                                        )

                                        _locationTriples.clear()
                                        _locationTriples.addAll(checkInTriples)
                                        
                                        withContext(Dispatchers.IO) {
                                            saveTriplesToCSV(context, checkInTriples)
                                        }

                                        if (address != lastAddress) {
                                            lastAddress = address
                                            val sharedPrefs = context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
                                            sharedPrefs.edit {
                                                putString("last_location", address)
                                            }
                                        }
                                    } else {
                                        Log.d("LocationViewModel", "Geocoder returned no addresses")
                                    }
                                } catch (e: Exception) {
                                    Log.e("LocationViewModel", "Error fetching address from location", e)
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("LocationViewModel", "Failed to get current location", e)
                        }
                }
                delay(30_000)
            }
        }
    }
}
