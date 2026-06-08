package com.nyoom.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val distanceKm: Double = 0.0,
    val avgSpeed: Double? = null,
    val maxSpeed: Double? = null,
)
