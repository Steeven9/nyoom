package com.nyoom.ui.riding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nyoom.ui.LocalLocationTracker
import com.nyoom.ui.LocalTripRepository
import kotlinx.coroutines.delay

@Composable
fun RidingScreen() {
    val context = LocalContext.current
    val repository = LocalTripRepository.current
    val locationTracker = LocalLocationTracker.current

    val viewModel = viewModel<RidingViewModel>(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RidingViewModel(locationTracker, repository) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    var elapsedTimeMs by remember { mutableLongStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.start()
        }
    }

    LaunchedEffect(uiState.isRunning && !uiState.isPaused) {
        if (uiState.isRunning && !uiState.isPaused) {
            while (uiState.isRunning && !uiState.isPaused) {
                delay(1000)
                elapsedTimeMs += 1000
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("nyoom", fontSize = 32.sp)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${formatTime(elapsedTimeMs)}", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Time")
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Card(modifier = Modifier.weight(1f).padding(8.dp)) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${String.format("%.2f", uiState.distanceKm)} km", fontSize = 20.sp)
                    Text("Distance")
                }
            }
            Card(modifier = Modifier.weight(1f).padding(8.dp)) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${String.format("%.1f", uiState.currentSpeedKmh)} km/h", fontSize = 20.sp)
                    Text("Speed")
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Card(modifier = Modifier.weight(1f).padding(8.dp)) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${String.format("%.1f", uiState.avgSpeedKmh)} km/h", fontSize = 16.sp)
                    Text("Avg Speed")
                }
            }
            Card(modifier = Modifier.weight(1f).padding(8.dp)) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${String.format("%.1f", uiState.maxSpeedKmh)} km/h", fontSize = 16.sp)
                    Text("Max Speed")
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            if (!uiState.isRunning) {
                Button(onClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        viewModel.start()
                    } else {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }) {
                    Text("Start")
                }
            } else {
                if (!uiState.isPaused) {
                    Button(onClick = { viewModel.pause() }) {
                        Text("Pause")
                    }
                } else {
                    Button(onClick = { viewModel.resume() }) {
                        Text("Resume")
                    }
                }
                Button(onClick = {
                    viewModel.stop()
                    elapsedTimeMs = 0
                }) {
                    Text("Stop")
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / 60000) % 60
    val hours = ms / 3600000
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}
