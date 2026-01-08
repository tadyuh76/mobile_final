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
    val isSynced: Boolean = false,
    val isPublic: Boolean = false,
    // Weather data captured at activity start
    val weatherTemperature: Double? = null,
    val weatherHumidity: Int? = null,
    val weatherCode: Int? = null,
    val weatherWindSpeed: Double? = null,
    val weatherDescription: String? = null
) {
    /**
     * Returns true if weather data was captured for this activity.
     */
    fun hasWeatherData(): Boolean = weatherTemperature != null

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
            isSynced = isSynced,
            isPublic = isPublic,
            weatherTemperature = weatherTemperature,
            weatherHumidity = weatherHumidity,
            weatherCode = weatherCode,
            weatherWindSpeed = weatherWindSpeed,
            weatherDescription = weatherDescription
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
                isSynced = entity.isSynced,
                isPublic = entity.isPublic,
                weatherTemperature = entity.weatherTemperature,
                weatherHumidity = entity.weatherHumidity,
                weatherCode = entity.weatherCode,
                weatherWindSpeed = entity.weatherWindSpeed,
                weatherDescription = entity.weatherDescription
            )
        }
    }
}
