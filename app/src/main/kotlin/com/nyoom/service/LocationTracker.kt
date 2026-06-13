package com.nyoom.service

import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationTracker(
    private val fusedLocationClient: FusedLocationProviderClient,
) {
    private val _locations = MutableStateFlow<List<Location>>(emptyList())
    val locations: StateFlow<List<Location>> = _locations.asStateFlow()

    private val _currentSpeed = MutableStateFlow(0.0)
    val currentSpeed: StateFlow<Double> = _currentSpeed.asStateFlow()

    private val _totalDistance = MutableStateFlow(0.0)
    val totalDistance: StateFlow<Double> = _totalDistance.asStateFlow()

    private var isTracking = false
    private var lastLocation: Location? = null

    private val pollingInterval = 1000L

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (location in result.locations) {
                updateDistance(location)
                _currentSpeed.value = location.speed.toDouble()
                _locations.value = _locations.value + location
                lastLocation = location
            }
        }
    }

    private fun updateDistance(location: Location) {
        val last = lastLocation
        if (last != null) {
            val distance = last.distanceTo(location) / 1000.0
            _totalDistance.value = _totalDistance.value + distance
        }
    }

    fun startTracking() {
        if (isTracking) return
        isTracking = true
        _locations.value = emptyList()
        _totalDistance.value = 0.0
        _currentSpeed.value = 0.0

        val locationRequest = LocationRequest.Builder(pollingInterval)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateDelayMillis(pollingInterval)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } catch (e: SecurityException) {
            isTracking = false
            throw e
        }
    }

    fun stopTracking() {
        isTracking = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun reset() {
        _locations.value = emptyList()
        _totalDistance.value = 0.0
        _currentSpeed.value = 0.0
        lastLocation = null
    }

    fun isTrackingActive(): Boolean = isTracking
}
