package com.example.mobile_final.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,  // Single row for settings
    val preferredActivityType: String = "running",
    val useMetricUnits: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val weight: Float = 70f  // kg, for calorie calculation
)
