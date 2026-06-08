package com.nyoom.ui.riding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyoom.data.model.Coordinate
import com.nyoom.data.model.Trip
import com.nyoom.data.repository.TripRepository
import com.nyoom.service.LocationTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.max

data class RidingUiState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedTimeMs: Long = 0,
    val distanceKm: Double = 0.0,
    val currentSpeedKmh: Double = 0.0,
    val avgSpeedKmh: Double = 0.0,
    val maxSpeedKmh: Double = 0.0,
)

class RidingViewModel(
    private val locationTracker: LocationTracker,
    private val repository: TripRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RidingUiState())
    val uiState: StateFlow<RidingUiState> = _uiState.asStateFlow()

    private var currentTripId: Int? = null
    private var tripStartTime: Long = 0
    private var maxSpeed: Double = 0.0

    init {
        viewModelScope.launch {
            locationTracker.totalDistance.collectLatest { distance ->
                _uiState.value = _uiState.value.copy(distanceKm = distance)
                updateAvgSpeed()
            }
        }

        viewModelScope.launch {
            locationTracker.currentSpeed.collectLatest { speed ->
                val speedKmh = speed * 3.6
                maxSpeed = max(maxSpeed, speedKmh)
                _uiState.value = _uiState.value.copy(
                    currentSpeedKmh = speedKmh,
                    maxSpeedKmh = maxSpeed
                )
            }
        }

        viewModelScope.launch {
            locationTracker.locations.collectLatest { locations ->
                if (locations.isNotEmpty() && currentTripId != null) {
                    locations.forEach { location ->
                        val coordinate = Coordinate(
                            tripId = currentTripId!!,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            timestamp = location.time,
                            accuracy = location.accuracy,
                        )
                        repository.insertCoordinate(coordinate)
                    }
                }
            }
        }
    }

    fun start() {
        tripStartTime = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(isRunning = true, isPaused = false)

        viewModelScope.launch {
            val trip = Trip(startTime = tripStartTime)
            currentTripId = repository.insertTrip(trip).toInt()
            locationTracker.startTracking()
        }
    }

    fun pause() {
        _uiState.value = _uiState.value.copy(isPaused = true)
        locationTracker.stopTracking()
    }

    fun resume() {
        _uiState.value = _uiState.value.copy(isPaused = false)
        locationTracker.startTracking()
    }

    fun stop() {
        locationTracker.stopTracking()

        viewModelScope.launch {
            if (currentTripId != null) {
                val endTime = System.currentTimeMillis()
                val trip = repository.getTripById(currentTripId!!) ?: return@launch

                val updatedTrip = trip.copy(
                    endTime = endTime,
                    distanceKm = _uiState.value.distanceKm,
                    avgSpeed = _uiState.value.avgSpeedKmh,
                    maxSpeed = _uiState.value.maxSpeedKmh,
                )
                repository.updateTrip(updatedTrip)
            }

            _uiState.value = RidingUiState()
            currentTripId = null
            maxSpeed = 0.0
            locationTracker.reset()
        }
    }

    private fun updateAvgSpeed() {
        val elapsedHours = (_uiState.value.elapsedTimeMs / 1000.0 / 3600.0)
        if (elapsedHours > 0) {
            val avgSpeed = _uiState.value.distanceKm / elapsedHours
            _uiState.value = _uiState.value.copy(avgSpeedKmh = avgSpeed)
        }
    }
}
