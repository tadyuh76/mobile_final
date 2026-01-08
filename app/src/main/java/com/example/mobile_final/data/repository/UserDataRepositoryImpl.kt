package com.example.mobile_final.data.repository

import com.example.mobile_final.data.local.entity.ActivityType
import com.example.mobile_final.domain.model.Activity
import com.example.mobile_final.domain.model.LocationPoint
import com.example.mobile_final.domain.repository.UserDataRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserDataRepository {

    /**
     * Converts an Activity and its LocationPoints to a Firestore-compatible map.
     */
    private fun activityToMap(activity: Activity, locationPoints: List<LocationPoint>): Map<String, Any?> {
        return hashMapOf(
            "type" to activity.type.name.lowercase(),
            "startTime" to activity.startTime,
            "endTime" to activity.endTime,
            "distanceMeters" to activity.distanceMeters,
            "durationSeconds" to activity.durationSeconds,
            "caloriesBurned" to activity.caloriesBurned,
            "avgPaceSecondsPerKm" to activity.avgPaceSecondsPerKm,
            "stepCount" to activity.stepCount,
            "isPublic" to activity.isPublic,
            "weatherTemperature" to activity.weatherTemperature,
            "weatherHumidity" to activity.weatherHumidity,
            "weatherCode" to activity.weatherCode,
            "weatherWindSpeed" to activity.weatherWindSpeed,
            "weatherDescription" to activity.weatherDescription,
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
    }

    /**
     * Converts a Firestore document to an Activity and LocationPoints pair.
     */
    private fun mapToActivity(docId: String, data: Map<String, Any>): Pair<Activity, List<LocationPoint>>? {
        return try {
            val activity = Activity(
                id = 0, // Will be assigned by Room on insert
                type = ActivityType.fromString(data["type"] as? String ?: "running"),
                startTime = (data["startTime"] as? Long) ?: return null,
                endTime = data["endTime"] as? Long,
                distanceMeters = (data["distanceMeters"] as? Double) ?: 0.0,
                durationSeconds = (data["durationSeconds"] as? Long) ?: 0L,
                caloriesBurned = ((data["caloriesBurned"] as? Long) ?: 0L).toInt(),
                avgPaceSecondsPerKm = ((data["avgPaceSecondsPerKm"] as? Long) ?: 0L).toInt(),
                stepCount = ((data["stepCount"] as? Long) ?: 0L).toInt(),
                isSynced = true, // Mark as synced since it came from cloud
                isPublic = (data["isPublic"] as? Boolean) ?: false,
                weatherTemperature = data["weatherTemperature"] as? Double,
                weatherHumidity = (data["weatherHumidity"] as? Long)?.toInt(),
                weatherCode = (data["weatherCode"] as? Long)?.toInt(),
                weatherWindSpeed = data["weatherWindSpeed"] as? Double,
                weatherDescription = data["weatherDescription"] as? String
            )

            val locationPointsList = data["locationPoints"] as? List<*>
            val locationPoints = locationPointsList?.mapNotNull { point ->
                val pointMap = point as? Map<*, *> ?: return@mapNotNull null
                LocationPoint(
                    id = 0, // Will be assigned by Room on insert
                    activityId = 0, // Will be updated when activity is inserted
                    latitude = (pointMap["latitude"] as? Double) ?: 0.0,
                    longitude = (pointMap["longitude"] as? Double) ?: 0.0,
                    altitude = pointMap["altitude"] as? Double,
                    timestamp = (pointMap["timestamp"] as? Long) ?: 0L,
                    speedMps = (pointMap["speedMps"] as? Double)?.toFloat()
                )
            } ?: emptyList()

            Pair(activity, locationPoints)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun backupActivity(
        userId: String,
        activity: Activity,
        locationPoints: List<LocationPoint>
    ): Result<Unit> {
        return try {
            // Use startTime as document ID for consistency
            val docId = activity.startTime.toString()
            val activityData = activityToMap(activity, locationPoints)

            firestore.collection("users")
                .document(userId)
                .collection("activities")
                .document(docId)
                .set(activityData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun backupAllActivities(
        userId: String,
        activities: List<Pair<Activity, List<LocationPoint>>>
    ): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val userActivitiesRef = firestore.collection("users")
                .document(userId)
                .collection("activities")

            activities.forEach { (activity, locationPoints) ->
                val docId = activity.startTime.toString()
                val activityData = activityToMap(activity, locationPoints)
                batch.set(userActivitiesRef.document(docId), activityData)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restoreUserActivities(userId: String): Result<List<Pair<Activity, List<LocationPoint>>>> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("activities")
                .get()
                .await()

            val activities = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                mapToActivity(doc.id, data)
            }

            Result.success(activities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteActivityBackup(userId: String, activityStartTime: Long): Result<Unit> {
        return try {
            val docId = activityStartTime.toString()
            firestore.collection("users")
                .document(userId)
                .collection("activities")
                .document(docId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateActivityBackup(
        userId: String,
        activity: Activity,
        locationPoints: List<LocationPoint>
    ): Result<Unit> {
        return try {
            val docId = activity.startTime.toString()
            val activityData = activityToMap(activity, locationPoints)

            firestore.collection("users")
                .document(userId)
                .collection("activities")
                .document(docId)
                .set(activityData) // Use set instead of update to overwrite completely
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
