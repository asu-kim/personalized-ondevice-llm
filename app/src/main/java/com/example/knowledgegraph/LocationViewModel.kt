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
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                val address = addresses[0].getAddressLine(0)
                                val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                                if (address != lastAddress) {
                                    // New location, start a new stay
                                    lastAddress = address
                                    currentStayId = "stay_${System.currentTimeMillis() / 1000}"
                                    
                                    val newTriples = listOf(
                                        KnowledgeTriple(currentStayId!!, "is_type", "location_stay"),
                                        KnowledgeTriple(currentStayId!!, "at_location", address),
                                        KnowledgeTriple(currentStayId!!, "start_time", dateTime),
                                        KnowledgeTriple(currentStayId!!, "end_time", dateTime),
                                        KnowledgeTriple("user", "stayed_at", currentStayId!!)
                                    )
                                    _locationTriples.clear()
                                    _locationTriples.addAll(newTriples)
                                    saveTriplesToCSV(context, newTriples)

                                    val sharedPrefs = context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)
                                    sharedPrefs.edit {
                                        putString("last_location", address)
                                        putString("current_stay_id", currentStayId)
                                    }
                                } else if (currentStayId != null) {
                                    // Same location, just update end_time
                                    val updatedTriple = KnowledgeTriple(currentStayId!!, "end_time", dateTime)
                                    saveTriplesToCSV(context, listOf(updatedTriple))
                                }
                            }
                        }
                    }
                }
                delay(30_000) // Increased delay slightly to reduce overhead
            }
        }
    }
}
