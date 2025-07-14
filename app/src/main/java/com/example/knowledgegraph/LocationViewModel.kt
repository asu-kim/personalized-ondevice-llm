package com.example.knowledgegraph

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.location.Geocoder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.android.gms.location.LocationServices
import androidx.core.content.edit

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    private var lastAddress: String? = null

    private val _locationTriples = mutableStateListOf<KnowledgeTriple>()
    val locationTriples: SnapshotStateList<KnowledgeTriple> = _locationTriples

    init {
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        viewModelScope.launch {
            while (true) {
                val context = getApplication<Application>().applicationContext
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                            Log.d("LOCATION_LATLON", "Lat: ${it.latitude}, Lon: ${it.longitude}")
                            Log.d("LATITUDE", "$addresses")
                            if (!addresses.isNullOrEmpty()) {
                                val address = addresses[0].getAddressLine(0)
                                val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                                if (address != lastAddress) {
                                    lastAddress = address
                                    _locationTriples.add(KnowledgeTriple("User", "location", address))
                                    _locationTriples.add(KnowledgeTriple("User", "starts at", dateTime))

                                    // Save to CSV
                                    saveTriplesToCSV(context, _locationTriples)

                                    // Optionally save to SharedPreferences
                                    val sharedPrefs = context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
                                    sharedPrefs.edit {
                                        putString("last_location", address)
                                        putString("last_timestamp", dateTime)
                                    }
                                }
                            }
                        }
                    }
                }
                delay(15_000) // check every 15 seconds
            }
        }
    }
}