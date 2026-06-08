package com.nyoom.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(tripId: Int, viewModel: MapViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    val cameraPositionState = rememberCameraPositionState()

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapLoaded = {
            if (uiState.coordinates.isNotEmpty()) {
                val latLngList = uiState.coordinates.map { LatLng(it.latitude, it.longitude) }
                val bounds = LatLngBounds.builder().apply {
                    latLngList.forEach { include(it) }
                }.build()
                cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            }
        }
    )
}
