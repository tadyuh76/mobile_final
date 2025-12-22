package com.example.mobile_final.domain.repository

import com.example.mobile_final.domain.model.Activity
import com.example.mobile_final.domain.model.LocationPoint

interface SyncRepository {
    suspend fun backupActivities(userId: String): Result<Int>
    suspend fun restoreActivities(userId: String): Result<Int>
    suspend fun backupActivity(userId: String, activity: Activity, locationPoints: List<LocationPoint>): Result<Unit>
    suspend fun getBackupInfo(userId: String): Result<BackupInfo>
}

data class BackupInfo(
    val activityCount: Int,
    val lastBackupTime: Long?
)
