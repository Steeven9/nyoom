package com.nyoom.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var tripId: Int = 0

    fun setTripId(id: Int) {
        if (tripId != id) {
            tripId = id
            loadCoordinates()
        }
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

class MapViewModelFactory(private val repository: TripRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MapViewModel(repository) as T
    }
}

