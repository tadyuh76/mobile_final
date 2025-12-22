package com.example.mobile_final.domain.model

import com.example.mobile_final.data.local.entity.ActivityType
import com.example.mobile_final.data.local.entity.UserSettingsEntity
import com.example.mobile_final.ui.theme.ThemeMode

data class UserSettings(
    val preferredActivityType: ActivityType = ActivityType.RUNNING,
    val useMetricUnits: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val weight: Float = 70f,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
) {
    fun toEntity(): UserSettingsEntity {
        return UserSettingsEntity(
            id = 1,
            preferredActivityType = preferredActivityType.name.lowercase(),
            useMetricUnits = useMetricUnits,
            notificationsEnabled = notificationsEnabled,
            weight = weight,
            themeMode = themeMode.name.lowercase()
        )
    }

    companion object {
        fun fromEntity(entity: UserSettingsEntity?): UserSettings {
            return entity?.let {
                UserSettings(
                    preferredActivityType = ActivityType.fromString(it.preferredActivityType),
                    useMetricUnits = it.useMetricUnits,
                    notificationsEnabled = it.notificationsEnabled,
                    weight = it.weight,
                    themeMode = themeModeFromString(it.themeMode)
                )
            } ?: UserSettings()
        }

        private fun themeModeFromString(value: String): ThemeMode {
            return when (value.lowercase()) {
                "light" -> ThemeMode.LIGHT
                "dark" -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
        }
    }
}
