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
import kotlinx.coroutines.delay
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

        // Start timer to update elapsed time every 1 second
        viewModelScope.launch {
            while (_uiState.value.isRunning) {
                delay(1000)
                if (_uiState.value.isRunning && !_uiState.value.isPaused) {
                    val elapsed = System.currentTimeMillis() - tripStartTime
                    _uiState.value = _uiState.value.copy(elapsedTimeMs = elapsed)
                    updateAvgSpeed()
                }
            }
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
        val endTime = System.currentTimeMillis()

        viewModelScope.launch {
            if (currentTripId != null) {
                val trip = repository.getTripById(currentTripId!!) ?: return@launch

                // Calculate final average speed using actual elapsed time
                val elapsedSeconds = (endTime - tripStartTime) / 1000.0
                val elapsedHours = elapsedSeconds / 3600.0
                val finalAvgSpeed = if (elapsedHours > 0) {
                    _uiState.value.distanceKm / elapsedHours
                } else {
                    0.0
                }

                val updatedTrip = trip.copy(
                    endTime = endTime,
                    distanceKm = _uiState.value.distanceKm,
                    avgSpeed = finalAvgSpeed,
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
        val elapsedSeconds = (_uiState.value.elapsedTimeMs / 1000.0)
        val elapsedHours = elapsedSeconds / 3600.0
        if (elapsedHours > 0 && _uiState.value.distanceKm > 0) {
            val avgSpeed = _uiState.value.distanceKm / elapsedHours
            _uiState.value = _uiState.value.copy(avgSpeedKmh = avgSpeed)
        }
    }
}
