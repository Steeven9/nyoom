package com.nyoom

import android.app.Application
import com.google.android.gms.location.LocationServices
import com.nyoom.data.db.TripDatabase
import com.nyoom.data.repository.TripRepository
import com.nyoom.service.LocationTracker

class NyoomApp : Application() {
    private lateinit var database: TripDatabase
    private lateinit var repository: TripRepository
    lateinit var locationTracker: LocationTracker

    override fun onCreate() {
        super.onCreate()
        database = TripDatabase.getDatabase(this)
        repository = TripRepository(database.tripDao(), database.coordinateDao())
        locationTracker = LocationTracker(LocationServices.getFusedLocationProviderClient(this))
    }

    fun getRepository(): TripRepository = repository
    fun getLocationTracker(): LocationTracker = locationTracker
}
