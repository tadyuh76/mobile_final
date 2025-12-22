package com.example.mobile_final.domain.model

import com.example.mobile_final.data.local.entity.ActivityEntity
import com.example.mobile_final.data.local.entity.ActivityType

data class Activity(
    val id: Long = 0,
    val type: ActivityType,
    val startTime: Long,
    val endTime: Long? = null,
    val distanceMeters: Double = 0.0,
    val durationSeconds: Long = 0,
    val caloriesBurned: Int = 0,
    val avgPaceSecondsPerKm: Int = 0,
    val stepCount: Int = 0,
    val isSynced: Boolean = false
) {
    fun toEntity(): ActivityEntity {
        return ActivityEntity(
            id = id,
            type = type.name.lowercase(),
            startTime = startTime,
            endTime = endTime,
            distanceMeters = distanceMeters,
            durationSeconds = durationSeconds,
            caloriesBurned = caloriesBurned,
            avgPaceSecondsPerKm = avgPaceSecondsPerKm,
            stepCount = stepCount,
            isSynced = isSynced
        )
    }

    companion object {
        fun fromEntity(entity: ActivityEntity): Activity {
            return Activity(
                id = entity.id,
                type = ActivityType.fromString(entity.type),
                startTime = entity.startTime,
                endTime = entity.endTime,
                distanceMeters = entity.distanceMeters,
                durationSeconds = entity.durationSeconds,
                caloriesBurned = entity.caloriesBurned,
                avgPaceSecondsPerKm = entity.avgPaceSecondsPerKm,
                stepCount = entity.stepCount,
                isSynced = entity.isSynced
            )
        }
    }
}
