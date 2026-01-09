package com.example.mobile_final.data.repository

import com.example.mobile_final.domain.model.Activity
import com.example.mobile_final.domain.model.LocationPoint
import com.example.mobile_final.domain.repository.SocialActivity
import com.example.mobile_final.domain.repository.SocialRepository
import com.example.mobile_final.data.local.entity.ActivityType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SocialRepository {

    private val publicActivitiesCollection = firestore.collection("public_activities")

    override suspend fun publishActivity(
        userId: String,
        userDisplayName: String?,
        userPhotoUrl: String?,
        activity: Activity,
        locationPoints: List<LocationPoint>
    ): Result<Unit> {
        return try {
            val docId = "${userId}_${activity.startTime}"

            val activityData = hashMapOf(
                "userId" to userId,
                "userDisplayName" to userDisplayName,
                "userPhotoUrl" to userPhotoUrl,
                "type" to activity.type.name.lowercase(),
                "startTime" to activity.startTime,
                "endTime" to activity.endTime,
                "distanceMeters" to activity.distanceMeters,
                "durationSeconds" to activity.durationSeconds,
                "caloriesBurned" to activity.caloriesBurned,
                "avgPaceSecondsPerKm" to activity.avgPaceSecondsPerKm,
                "stepCount" to activity.stepCount,
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

            publicActivitiesCollection.document(docId).set(activityData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unpublishActivity(userId: String, activityStartTime: Long): Result<Unit> {
        return try {
            val docId = "${userId}_${activityStartTime}"
            publicActivitiesCollection.document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getPublicActivities(limit: Int): Flow<List<SocialActivity>> = callbackFlow {
        val listener = publicActivitiesCollection
            .orderBy("startTime", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val activities = snapshot.documents.mapNotNull { doc ->
                        try {
                            val userId = doc.getString("userId") ?: return@mapNotNull null
                            val userDisplayName = doc.getString("userDisplayName")
                            val userPhotoUrl = doc.getString("userPhotoUrl")

                            val activity = Activity(
                                id = 0, // Not used for social feed
                                type = ActivityType.fromString(doc.getString("type") ?: "running"),
                                startTime = doc.getLong("startTime") ?: 0L,
                                endTime = doc.getLong("endTime"),
                                distanceMeters = doc.getDouble("distanceMeters") ?: 0.0,
                                durationSeconds = doc.getLong("durationSeconds") ?: 0L,
                                caloriesBurned = (doc.getLong("caloriesBurned") ?: 0L).toInt(),
                                avgPaceSecondsPerKm = (doc.getLong("avgPaceSecondsPerKm") ?: 0L).toInt(),
                                stepCount = (doc.getLong("stepCount") ?: 0L).toInt(),
                                isSynced = false,
                                isPublic = true,
                                weatherTemperature = doc.getDouble("weatherTemperature"),
                                weatherHumidity = doc.getLong("weatherHumidity")?.toInt(),
                                weatherCode = doc.getLong("weatherCode")?.toInt(),
                                weatherWindSpeed = doc.getDouble("weatherWindSpeed"),
                                weatherDescription = doc.getString("weatherDescription")
                            )

                            val locationPointsList = doc.get("locationPoints") as? List<*>
                            val locationPoints = locationPointsList?.mapNotNull { point ->
                                val pointMap = point as? Map<*, *> ?: return@mapNotNull null
                                LocationPoint(
                                    id = 0,
                                    activityId = 0,
                                    latitude = (pointMap["latitude"] as? Double) ?: 0.0,
                                    longitude = (pointMap["longitude"] as? Double) ?: 0.0,
                                    altitude = pointMap["altitude"] as? Double,
                                    timestamp = (pointMap["timestamp"] as? Long) ?: 0L,
                                    speedMps = (pointMap["speedMps"] as? Double)?.toFloat()
                                )
                            } ?: emptyList()

                            SocialActivity(
                                userId = userId,
                                userDisplayName = userDisplayName,
                                userPhotoUrl = userPhotoUrl,
                                activity = activity,
                                locationPoints = locationPoints
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(activities)
                }
            }

        awaitClose { listener.remove() }
    }
}
