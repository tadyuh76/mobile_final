package com.example.mobile_final.domain.repository

import com.example.mobile_final.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<UserSettings>
    suspend fun getSettingsOnce(): UserSettings
    suspend fun updateSettings(settings: UserSettings)
}
