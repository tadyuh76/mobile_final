package com.example.mobile_final.domain.repository

import com.example.mobile_final.domain.model.Activity
import com.example.mobile_final.domain.model.LocationPoint
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    // Activity CRUD
    suspend fun insertActivity(activity: Activity): Long
    suspend fun updateActivity(activity: Activity)
    suspend fun deleteActivity(activity: Activity)
    suspend fun deleteActivityById(id: Long)
    suspend fun getActivityById(id: Long): Activity?
    fun getActivityByIdFlow(id: Long): Flow<Activity?>
    fun getAllActivities(): Flow<List<Activity>>

    // Location Points
    suspend fun insertLocationPoint(locationPoint: LocationPoint): Long
    suspend fun insertLocationPoints(locationPoints: List<LocationPoint>)
    suspend fun getLocationPointsForActivity(activityId: Long): List<LocationPoint>
    fun getLocationPointsForActivityFlow(activityId: Long): Flow<List<LocationPoint>>

    // Sync
    suspend fun getUnsyncedActivities(): List<Activity>
    suspend fun markAsSynced(id: Long)

    // Statistics
    fun getActivitiesForDay(startOfDay: Long, endOfDay: Long): Flow<List<Activity>>
    fun getActivitiesForWeek(startOfWeek: Long, endOfWeek: Long): Flow<List<Activity>>
    fun getActivitiesForMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<Activity>>
    suspend fun getTotalDistanceForPeriod(startTime: Long, endTime: Long): Double
    suspend fun getTotalDurationForPeriod(startTime: Long, endTime: Long): Long
    suspend fun getActivityCountForPeriod(startTime: Long, endTime: Long): Int
    suspend fun getTotalCaloriesForPeriod(startTime: Long, endTime: Long): Int
}
