package com.nyoom.ui.diary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nyoom.data.model.Trip
import com.nyoom.ui.LocalTripRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiaryScreen(navController: NavController) {
    val repository = LocalTripRepository.current

    val viewModel = viewModel<DiaryViewModel>(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DiaryViewModel(repository) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
        }
    } else if (uiState.trips.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text("No trips yet")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(uiState.trips) { trip ->
                TripCard(trip, navController = navController) { viewModel.deleteTrip(trip.id) }
            }
        }
    }
}

@Composable
private fun TripCard(trip: Trip, navController: NavController, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp).clickable {
        navController.navigate("map/${trip.id}")
    }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Trip ID: ${trip.id}")
            Text(formatDate(trip.startTime), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Distance: ${String.format("%.2f", trip.distanceKm)} km")
            if (trip.endTime != null) {
                Text("Duration: ${formatDuration(trip.endTime - trip.startTime)}")
            }
            if (trip.avgSpeed != null) {
                Text("Avg Speed: ${String.format("%.1f", trip.avgSpeed)} km/h")
            }
            if (trip.maxSpeed != null) {
                Text("Max Speed: ${String.format("%.1f", trip.maxSpeed)} km/h")
            }
            Row {
                Button(
                    onClick = onDelete,
                    modifier = Modifier.padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
}

private fun formatDuration(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / 60000) % 60
    val hours = ms / 3600000
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}
