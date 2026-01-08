package com.example.mobile_final.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,              // "running", "walking", "cycling"
    val startTime: Long,           // Epoch millis
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
)

enum class ActivityType(val displayName: String) {
    RUNNING("Running"),
    WALKING("Walking"),
    CYCLING("Cycling");

    companion object {
        fun fromString(value: String): ActivityType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: RUNNING
        }
    }
}
