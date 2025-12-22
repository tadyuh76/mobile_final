package com.example.mobile_final.domain.model

import com.example.mobile_final.data.local.entity.LocationPointEntity

data class LocationPoint(
    val id: Long = 0,
    val activityId: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val timestamp: Long,
    val speedMps: Float? = null
) {
    fun toEntity(): LocationPointEntity {
        return LocationPointEntity(
            id = id,
            activityId = activityId,
            latitude = latitude,
            longitude = longitude,
            altitude = altitude,
            timestamp = timestamp,
            speedMps = speedMps
        )
    }

    companion object {
        fun fromEntity(entity: LocationPointEntity): LocationPoint {
            return LocationPoint(
                id = entity.id,
                activityId = entity.activityId,
                latitude = entity.latitude,
                longitude = entity.longitude,
                altitude = entity.altitude,
                timestamp = entity.timestamp,
                speedMps = entity.speedMps
            )
        }
    }
}
