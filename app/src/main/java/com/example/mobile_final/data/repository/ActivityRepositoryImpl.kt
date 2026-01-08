package com.example.mobile_final.data.repository

import com.example.mobile_final.data.local.dao.ActivityDao
import com.example.mobile_final.data.local.dao.LocationPointDao
import com.example.mobile_final.domain.model.Activity
import com.example.mobile_final.domain.model.LocationPoint
import com.example.mobile_final.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    private val activityDao: ActivityDao,
    private val locationPointDao: LocationPointDao
) : ActivityRepository {

    // Activity CRUD
    override suspend fun insertActivity(activity: Activity): Long {
        return activityDao.insertActivity(activity.toEntity())
    }

    override suspend fun updateActivity(activity: Activity) {
        activityDao.updateActivity(activity.toEntity())
    }

    override suspend fun deleteActivity(activity: Activity) {
        activityDao.deleteActivity(activity.toEntity())
    }

    override suspend fun deleteActivityById(id: Long) {
        activityDao.deleteActivityById(id)
    }

    override suspend fun getActivityById(id: Long): Activity? {
        return activityDao.getActivityById(id)?.let { Activity.fromEntity(it) }
    }

    override fun getActivityByIdFlow(id: Long): Flow<Activity?> {
        return activityDao.getActivityByIdFlow(id).map { entity ->
            entity?.let { Activity.fromEntity(it) }
        }
    }

    override fun getAllActivities(): Flow<List<Activity>> {
        return activityDao.getAllActivities().map { entities ->
            entities.map { Activity.fromEntity(it) }
        }
    }

    // Location Points
    override suspend fun insertLocationPoint(locationPoint: LocationPoint): Long {
        return locationPointDao.insertLocationPoint(locationPoint.toEntity())
    }

    override suspend fun insertLocationPoints(locationPoints: List<LocationPoint>) {
        locationPointDao.insertLocationPoints(locationPoints.map { it.toEntity() })
    }

    override suspend fun getLocationPointsForActivity(activityId: Long): List<LocationPoint> {
        return locationPointDao.getLocationPointsForActivity(activityId).map {
            LocationPoint.fromEntity(it)
        }
    }

    override fun getLocationPointsForActivityFlow(activityId: Long): Flow<List<LocationPoint>> {
        return locationPointDao.getLocationPointsForActivityFlow(activityId).map { entities ->
            entities.map { LocationPoint.fromEntity(it) }
        }
    }

    // Sync
    override suspend fun getUnsyncedActivities(): List<Activity> {
        return activityDao.getUnsyncedActivities().map { Activity.fromEntity(it) }
    }

    override suspend fun markAsSynced(id: Long) {
        activityDao.markAsSynced(id)
    }

    // Statistics
    override fun getActivitiesForDay(startOfDay: Long, endOfDay: Long): Flow<List<Activity>> {
        return activityDao.getActivitiesForDay(startOfDay, endOfDay).map { entities ->
            entities.map { Activity.fromEntity(it) }
        }
    }

    override fun getActivitiesForWeek(startOfWeek: Long, endOfWeek: Long): Flow<List<Activity>> {
        return activityDao.getActivitiesForWeek(startOfWeek, endOfWeek).map { entities ->
            entities.map { Activity.fromEntity(it) }
        }
    }

    override fun getActivitiesForMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<Activity>> {
        return activityDao.getActivitiesForMonth(startOfMonth, endOfMonth).map { entities ->
            entities.map { Activity.fromEntity(it) }
        }
    }

    override suspend fun getTotalDistanceForPeriod(startTime: Long, endTime: Long): Double {
        return activityDao.getTotalDistanceForPeriod(startTime, endTime) ?: 0.0
    }

    override suspend fun getTotalDurationForPeriod(startTime: Long, endTime: Long): Long {
        return activityDao.getTotalDurationForPeriod(startTime, endTime) ?: 0L
    }

    override suspend fun getActivityCountForPeriod(startTime: Long, endTime: Long): Int {
        return activityDao.getActivityCountForPeriod(startTime, endTime)
    }

    override suspend fun getTotalCaloriesForPeriod(startTime: Long, endTime: Long): Int {
        return activityDao.getTotalCaloriesForPeriod(startTime, endTime) ?: 0
    }

    // Social feed
    override fun getAllActivitiesWithLocationPoints(): Flow<List<Pair<Activity, List<LocationPoint>>>> {
        return activityDao.getAllActivities().map { activityEntities ->
            activityEntities.map { entity ->
                val activity = Activity.fromEntity(entity)
                val locationPoints = locationPointDao.getLocationPointsForActivity(entity.id)
                    .map { LocationPoint.fromEntity(it) }
                Pair(activity, locationPoints)
            }
        }
    }

    override suspend fun updateActivityPublicStatus(activityId: Long, isPublic: Boolean) {
        activityDao.updateActivityPublicStatus(activityId, isPublic)
    }
}
