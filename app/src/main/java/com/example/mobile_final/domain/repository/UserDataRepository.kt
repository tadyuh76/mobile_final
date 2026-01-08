package com.example.mobile_final.domain.repository

import com.example.mobile_final.domain.model.Activity
import com.example.mobile_final.domain.model.LocationPoint

/**
 * Repository for backing up user activity data to Firestore.
 * Handles private user data sync (distinct from public social feed).
 */
interface UserDataRepository {
    /**
     * Backup a single activity with its location points to user's cloud storage.
     * @param userId The authenticated user's ID
     * @param activity The activity to backup
     * @param locationPoints Location points for the activity
     * @return Result indicating success or failure
     */
    suspend fun backupActivity(
        userId: String,
        activity: Activity,
        locationPoints: List<LocationPoint>
    ): Result<Unit>

    /**
     * Backup all local activities to user's cloud storage.
     * @param userId The authenticated user's ID
     * @param activities List of activities with their location points
     * @return Result indicating success or failure
     */
    suspend fun backupAllActivities(
        userId: String,
        activities: List<Pair<Activity, List<LocationPoint>>>
    ): Result<Unit>

    /**
     * Restore all activities from user's cloud storage.
     * @param userId The authenticated user's ID
     * @return List of activities with their location points
     */
    suspend fun restoreUserActivities(userId: String): Result<List<Pair<Activity, List<LocationPoint>>>>

    /**
     * Delete an activity backup from user's cloud storage.
     * @param userId The authenticated user's ID
     * @param activityStartTime Unique identifier for the activity
     * @return Result indicating success or failure
     */
    suspend fun deleteActivityBackup(userId: String, activityStartTime: Long): Result<Unit>

    /**
     * Update an existing activity backup in user's cloud storage.
     * @param userId The authenticated user's ID
     * @param activity The updated activity
     * @param locationPoints Updated location points
     * @return Result indicating success or failure
     */
    suspend fun updateActivityBackup(
        userId: String,
        activity: Activity,
        locationPoints: List<LocationPoint>
    ): Result<Unit>
}
