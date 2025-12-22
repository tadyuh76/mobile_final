package com.example.mobile_final.domain.model

import com.example.mobile_final.data.local.entity.ActivityType
import com.example.mobile_final.data.local.entity.UserSettingsEntity

data class UserSettings(
    val preferredActivityType: ActivityType = ActivityType.RUNNING,
    val useMetricUnits: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val weight: Float = 70f
) {
    fun toEntity(): UserSettingsEntity {
        return UserSettingsEntity(
            id = 1,
            preferredActivityType = preferredActivityType.name.lowercase(),
            useMetricUnits = useMetricUnits,
            notificationsEnabled = notificationsEnabled,
            weight = weight
        )
    }

    companion object {
        fun fromEntity(entity: UserSettingsEntity?): UserSettings {
            return entity?.let {
                UserSettings(
                    preferredActivityType = ActivityType.fromString(it.preferredActivityType),
                    useMetricUnits = it.useMetricUnits,
                    notificationsEnabled = it.notificationsEnabled,
                    weight = it.weight
                )
            } ?: UserSettings()
        }
    }
}
