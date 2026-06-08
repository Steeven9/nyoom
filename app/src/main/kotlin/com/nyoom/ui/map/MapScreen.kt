package com.nyoom.ui.map

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nyoom.ui.LocalTripRepository
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline as OSMPolyline

@Composable
fun MapScreen(tripId: Int, navController: NavController) {
    val context = LocalContext.current
    val repository = LocalTripRepository.current
    val viewModel: MapViewModel = viewModel(factory = MapViewModelFactory(repository))

    val uiState by viewModel.uiState.collectAsState()

    val mapView = remember {
        // Set user agent for osmdroid
        Configuration.getInstance().apply {
            userAgentValue = "com.nyoom/0.1.0"
        }
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            // Set default center to avoid empty map
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(0.0, 0.0))
        }
    }

    LaunchedEffect(tripId) {
        viewModel.setTripId(tripId)
    }

    LaunchedEffect(uiState.coordinates) {
        if (uiState.coordinates.isNotEmpty()) {
            // Clear previous overlays
            mapView.overlays.clear()

            // Create polyline with visible styling
            val polyline = OSMPolyline().apply {
                setPoints(uiState.coordinates.map { coord ->
                    GeoPoint(coord.latitude, coord.longitude)
                })
                // Set polyline color to red, width to 5 pixels
                outlinePaint.color = android.graphics.Color.RED
                outlinePaint.strokeWidth = 5f
            }
            mapView.overlays.add(polyline)

            // Calculate bounds
            val points = uiState.coordinates.map { GeoPoint(it.latitude, it.longitude) }
            if (points.isNotEmpty()) {
                val minLat = points.minOf { it.latitude }
                val maxLat = points.maxOf { it.latitude }
                val minLon = points.minOf { it.longitude }
                val maxLon = points.maxOf { it.longitude }

                // Center map on bounds
                val centerLat = (minLat + maxLat) / 2
                val centerLon = (minLon + maxLon) / 2
                mapView.controller.setCenter(GeoPoint(centerLat, centerLon))

                // Set zoom level based on distance
                val latDiff = maxLat - minLat
                val lonDiff = maxLon - minLon
                val maxDiff = maxOf(latDiff, lonDiff)
                val zoomLevel = when {
                    maxDiff > 1.0 -> 8.0
                    maxDiff > 0.1 -> 12.0
                    maxDiff > 0.01 -> 14.0
                    else -> 18.0
                }
                mapView.controller.setZoom(zoomLevel)
            }

            mapView.invalidate()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        // Loading indicator
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }

        // Debug info panel with test button
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.White.copy(alpha = 0.9f))
                .padding(12.dp)
        ) {
            Text("Trip ID: $tripId", color = Color.Black)
            Text("Coordinates: ${uiState.coordinates.size}", color = Color.Black)
            if (uiState.coordinates.isEmpty() && !uiState.isLoading) {
                Text("⚠ No coordinates found", color = Color.Red)
            } else if (uiState.coordinates.isNotEmpty()) {
                val first = uiState.coordinates.first()
                val last = uiState.coordinates.last()
                Text("From: ${String.format("%.4f", first.latitude)}, ${String.format("%.4f", first.longitude)}", color = Color.Black)
                Text("To: ${String.format("%.4f", last.latitude)}, ${String.format("%.4f", last.longitude)}", color = Color.Black)
            }
        }

        // Back button
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Icon(painterResource(android.R.drawable.ic_menu_close_clear_cancel), "Back")
        }
    }
}
