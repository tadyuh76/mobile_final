package com.example.mobile_final.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "location_points",
    foreignKeys = [
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["activityId"])]
)
data class LocationPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val activityId: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val timestamp: Long,
    val speedMps: Float? = null
)
