package com.nyoom.data.repository

import com.nyoom.data.dao.CoordinateDao
import com.nyoom.data.dao.TripDao
import com.nyoom.data.model.Coordinate
import com.nyoom.data.model.Trip
import kotlinx.coroutines.flow.Flow

class TripRepository(
    private val tripDao: TripDao,
    private val coordinateDao: CoordinateDao,
) {
    fun getAllTrips(): Flow<List<Trip>> = tripDao.getAllTrips()

    suspend fun getTripById(id: Int): Trip? = tripDao.getTripById(id)

    suspend fun insertTrip(trip: Trip): Long = tripDao.insert(trip)

    suspend fun updateTrip(trip: Trip) = tripDao.update(trip)

    suspend fun deleteTrip(id: Int) = tripDao.delete(id)

    suspend fun insertCoordinate(coordinate: Coordinate) =
        coordinateDao.insert(coordinate)

    suspend fun insertCoordinates(coordinates: List<Coordinate>) =
        coordinateDao.insertAll(coordinates)

    suspend fun getCoordinatesByTripId(tripId: Int): List<Coordinate> =
        coordinateDao.getByTripId(tripId)

    fun observeCoordinatesByTripId(tripId: Int): Flow<List<Coordinate>> =
        coordinateDao.observeByTripId(tripId)

    suspend fun deleteCoordinatesByTripId(tripId: Int) =
        coordinateDao.deleteByTripId(tripId)
}
