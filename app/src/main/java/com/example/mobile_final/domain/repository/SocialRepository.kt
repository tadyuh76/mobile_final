package com.example.mobile_final.domain.repository

import com.example.mobile_final.domain.model.Activity
import com.example.mobile_final.domain.model.LocationPoint
import kotlinx.coroutines.flow.Flow

interface SocialRepository {
    /**
     * Publishes an activity to the public feed in Firestore.
     */
    suspend fun publishActivity(
        userId: String,
        userDisplayName: String?,
        userPhotoUrl: String?,
        activity: Activity,
        locationPoints: List<LocationPoint>
    ): Result<Unit>

    /**
     * Removes an activity from the public feed.
     */
    suspend fun unpublishActivity(userId: String, activityStartTime: Long): Result<Unit>

    /**
     * Gets public activities from all users, ordered by time (newest first).
     */
    fun getPublicActivities(limit: Int = 50): Flow<List<SocialActivity>>
}

data class SocialActivity(
    val userId: String,
    val userDisplayName: String?,
    val userPhotoUrl: String?,
    val activity: Activity,
    val locationPoints: List<LocationPoint>
)
