package com.nyoom.ui.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyoom.data.model.Coordinate
import com.nyoom.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MapUiState(
    val coordinates: List<Coordinate> = emptyList(),
    val isLoading: Boolean = false,
)

class MapViewModel(
    private val repository: TripRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val tripId: Int = savedStateHandle["tripId"] ?: 0

    init {
        loadCoordinates()
    }

    private fun loadCoordinates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.observeCoordinatesByTripId(tripId).collect { coordinates ->
                _uiState.value = _uiState.value.copy(coordinates = coordinates, isLoading = false)
            }
        }
    }
}
