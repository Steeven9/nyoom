package com.nyoom.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nyoom.data.model.Coordinate
import kotlinx.coroutines.flow.Flow

@Dao
interface CoordinateDao {
    @Insert
    suspend fun insertAll(coordinates: List<Coordinate>)

    @Insert
    suspend fun insert(coordinate: Coordinate)

    @Query("SELECT * FROM coordinates WHERE tripId = :tripId ORDER BY timestamp")
    suspend fun getByTripId(tripId: Int): List<Coordinate>

    @Query("SELECT * FROM coordinates WHERE tripId = :tripId ORDER BY timestamp")
    fun observeByTripId(tripId: Int): Flow<List<Coordinate>>

    @Query("DELETE FROM coordinates WHERE tripId = :tripId")
    suspend fun deleteByTripId(tripId: Int)
}
