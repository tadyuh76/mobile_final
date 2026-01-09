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
                    // Create new activity (including weather data)
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
                        isSynced = true,
                        // Weather data
                        weatherTemperature = (activityData["weatherTemperature"] as? Number)?.toDouble(),
                        weatherHumidity = (activityData["weatherHumidity"] as? Number)?.toInt(),
                        weatherCode = (activityData["weatherCode"] as? Number)?.toInt(),
                        weatherWindSpeed = (activityData["weatherWindSpeed"] as? Number)?.toDouble(),
                        weatherDescription = activityData["weatherDescription"] as? String
                    )

                    // Insert activity
                    val newActivityId = activityDao.insert(activity.toEntity())

                    // Restore location points from embedded array (same format as UserDataRepository)
                    val locationPointsList = activityData["locationPoints"] as? List<*>
                    locationPointsList?.forEach { point ->
                        val pointMap = point as? Map<*, *> ?: return@forEach
                        val locationPoint = LocationPoint(
                            id = 0,
                            activityId = newActivityId,
                            latitude = (pointMap["latitude"] as? Number)?.toDouble() ?: 0.0,
                            longitude = (pointMap["longitude"] as? Number)?.toDouble() ?: 0.0,
                            altitude = (pointMap["altitude"] as? Number)?.toDouble(),
                            timestamp = (pointMap["timestamp"] as? Number)?.toLong() ?: 0L,
                            speedMps = (pointMap["speedMps"] as? Number)?.toFloat()
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

            // Create activity document with embedded location points (same format as UserDataRepository)
            val activityData = hashMapOf(
                "type" to activity.type.name.lowercase(),
                "startTime" to activity.startTime,
                "endTime" to activity.endTime,
                "distanceMeters" to activity.distanceMeters,
                "durationSeconds" to activity.durationSeconds,
                "caloriesBurned" to activity.caloriesBurned,
                "avgPaceSecondsPerKm" to activity.avgPaceSecondsPerKm,
                "stepCount" to activity.stepCount,
                "isPublic" to activity.isPublic,
                // Weather data
                "weatherTemperature" to activity.weatherTemperature,
                "weatherHumidity" to activity.weatherHumidity,
                "weatherCode" to activity.weatherCode,
                "weatherWindSpeed" to activity.weatherWindSpeed,
                "weatherDescription" to activity.weatherDescription,
                // Location points as embedded array
                "locationPoints" to locationPoints.map { point ->
                    hashMapOf(
                        "latitude" to point.latitude,
                        "longitude" to point.longitude,
                        "altitude" to point.altitude,
                        "timestamp" to point.timestamp,
                        "speedMps" to point.speedMps
                    )
                }
            )

            activityRef.set(activityData).await()

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
