package com.nyoom.ui

import androidx.compose.runtime.compositionLocalOf
import com.nyoom.data.repository.TripRepository
import com.nyoom.service.LocationTracker

val LocalTripRepository = compositionLocalOf<TripRepository> {
    error("TripRepository not provided")
}

val LocalLocationTracker = compositionLocalOf<LocationTracker> {
    error("LocationTracker not provided")
}
