package com.example.mobile_final.data.repository

import com.example.mobile_final.data.local.dao.UserSettingsDao
import com.example.mobile_final.domain.model.UserSettings
import com.example.mobile_final.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val userSettingsDao: UserSettingsDao
) : SettingsRepository {

    override fun getSettings(): Flow<UserSettings> {
        return userSettingsDao.getSettings().map { entity ->
            UserSettings.fromEntity(entity)
        }
    }

    override suspend fun getSettingsOnce(): UserSettings {
        return UserSettings.fromEntity(userSettingsDao.getSettingsOnce())
    }

    override suspend fun updateSettings(settings: UserSettings) {
        userSettingsDao.insertSettings(settings.toEntity())
    }
}
