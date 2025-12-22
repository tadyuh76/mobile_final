package com.example.mobile_final.data.repository

import com.example.mobile_final.data.local.dao.ActivityDao
import com.example.mobile_final.data.local.dao.LocationPointDao
import com.example.mobile_final.data.local.entity.ActivityType
import com.example.mobile_final.domain.model.Activity
import com.example.mobile_final.domain.model.LocationPoint
import com.example.mobile_final.domain.repository.BackupInfo
import com.example.mobile_final.domain.repository.SyncRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val activityDao: ActivityDao,
    private val locationPointDao: LocationPointDao
) : SyncRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val ACTIVITIES_COLLECTION = "activities"
        private const val LOCATION_POINTS_COLLECTION = "locationPoints"
        private const val BACKUP_INFO_DOC = "backupInfo"
    }

    override suspend fun backupActivities(userId: String): Result<Int> {
        return try {
            val activities = activityDao.getAllActivitiesOnce()
            var backedUpCount = 0

            activities.forEach { activityEntity ->
                val activity = Activity.fromEntity(activityEntity)
                val locationPoints = locationPointDao.getLocationPointsForActivity(activityEntity.id)
                    .map { LocationPoint.fromEntity(it) }

                backupActivity(userId, activity, locationPoints)

                // Mark as synced
                activityDao.update(activityEntity.copy(isSynced = true))
                backedUpCount++
            }

            // Update backup info
            updateBackupInfo(userId, activities.size)

            Result.success(backedUpCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restoreActivities(userId: String): Result<Int> {
        return try {
            val activitiesSnapshot = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACTIVITIES_COLLECTION)
                .get()
                .await()

            var restoredCount = 0

            for (activityDoc in activitiesSnapshot.documents) {
                val activityData = activityDoc.data ?: continue

                // Check if activity already exists locally by startTime
                val startTime = activityData["startTime"] as? Long ?: continue
                val existingActivity = activityDao.getActivityByStartTime(startTime)

                if (existingActivity == null) {
                    // Create new activity
                    val activity = Activity(
                        id = 0, // Will be auto-generated
                        type = ActivityType.fromString(activityData["type"] as? String ?: "running"),
                        startTime = startTime,
                        endTime = activityData["endTime"] as? Long,
                        distanceMeters = (activityData["distanceMeters"] as? Number)?.toDouble() ?: 0.0,
                        durationSeconds = (activityData["durationSeconds"] as? Number)?.toLong() ?: 0L,
                        caloriesBurned = (activityData["caloriesBurned"] as? Number)?.toInt() ?: 0,
                        avgPaceSecondsPerKm = (activityData["avgPaceSecondsPerKm"] as? Number)?.toInt() ?: 0,
                        stepCount = (activityData["stepCount"] as? Number)?.toInt() ?: 0,
                        isSynced = true
                    )

                    // Insert activity
                    val newActivityId = activityDao.insert(activity.toEntity())

                    // Restore location points
                    val locationPointsSnapshot = activityDoc.reference
                        .collection(LOCATION_POINTS_COLLECTION)
                        .get()
                        .await()

                    for (pointDoc in locationPointsSnapshot.documents) {
                        val pointData = pointDoc.data ?: continue
                        val locationPoint = LocationPoint(
                            id = 0,
                            activityId = newActivityId,
                            latitude = (pointData["latitude"] as? Number)?.toDouble() ?: 0.0,
                            longitude = (pointData["longitude"] as? Number)?.toDouble() ?: 0.0,
                            altitude = (pointData["altitude"] as? Number)?.toDouble(),
                            timestamp = (pointData["timestamp"] as? Number)?.toLong() ?: 0L,
                            speedMps = (pointData["speedMps"] as? Number)?.toFloat()
                        )
                        locationPointDao.insert(locationPoint.toEntity())
                    }

                    restoredCount++
                }
            }

            Result.success(restoredCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun backupActivity(
        userId: String,
        activity: Activity,
        locationPoints: List<LocationPoint>
    ): Result<Unit> {
        return try {
            val activityRef = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACTIVITIES_COLLECTION)
                .document(activity.startTime.toString())

            // Create activity document
            val activityData = hashMapOf(
                "type" to activity.type.name.lowercase(),
                "startTime" to activity.startTime,
                "endTime" to activity.endTime,
                "distanceMeters" to activity.distanceMeters,
                "durationSeconds" to activity.durationSeconds,
                "caloriesBurned" to activity.caloriesBurned,
                "avgPaceSecondsPerKm" to activity.avgPaceSecondsPerKm,
                "stepCount" to activity.stepCount
            )

            activityRef.set(activityData).await()

            // Backup location points
            val batch = firestore.batch()
            locationPoints.forEachIndexed { index, point ->
                val pointRef = activityRef
                    .collection(LOCATION_POINTS_COLLECTION)
                    .document(index.toString())

                val pointData = hashMapOf(
                    "latitude" to point.latitude,
                    "longitude" to point.longitude,
                    "altitude" to point.altitude,
                    "timestamp" to point.timestamp,
                    "speedMps" to point.speedMps
                )
                batch.set(pointRef, pointData)
            }
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBackupInfo(userId: String): Result<BackupInfo> {
        return try {
            val infoDoc = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            val activityCount = infoDoc.getLong("activityCount")?.toInt() ?: 0
            val lastBackupTime = infoDoc.getLong("lastBackupTime")

            Result.success(BackupInfo(activityCount, lastBackupTime))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun updateBackupInfo(userId: String, activityCount: Int) {
        val data = hashMapOf(
            "activityCount" to activityCount,
            "lastBackupTime" to System.currentTimeMillis()
        )

        firestore
            .collection(USERS_COLLECTION)
            .document(userId)
            .set(data)
            .await()
    }
}
