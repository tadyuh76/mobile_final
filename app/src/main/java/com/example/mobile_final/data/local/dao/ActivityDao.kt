package com.example.mobile_final.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mobile_final.data.local.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity): Long

    @Update
    suspend fun updateActivity(activity: ActivityEntity)

    @Delete
    suspend fun deleteActivity(activity: ActivityEntity)

    @Query("SELECT * FROM activities ORDER BY startTime DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getActivityById(id: Long): ActivityEntity?

    @Query("SELECT * FROM activities WHERE id = :id")
    fun getActivityByIdFlow(id: Long): Flow<ActivityEntity?>

    @Query("SELECT * FROM activities WHERE isSynced = 0")
    suspend fun getUnsyncedActivities(): List<ActivityEntity>

    @Query("UPDATE activities SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun deleteActivityById(id: Long)

    // Statistics queries
    @Query("""
        SELECT * FROM activities
        WHERE startTime >= :startOfDay AND startTime < :endOfDay
        ORDER BY startTime DESC
    """)
    fun getActivitiesForDay(startOfDay: Long, endOfDay: Long): Flow<List<ActivityEntity>>

    @Query("""
        SELECT * FROM activities
        WHERE startTime >= :startOfWeek AND startTime < :endOfWeek
        ORDER BY startTime DESC
    """)
    fun getActivitiesForWeek(startOfWeek: Long, endOfWeek: Long): Flow<List<ActivityEntity>>

    @Query("""
        SELECT * FROM activities
        WHERE startTime >= :startOfMonth AND startTime < :endOfMonth
        ORDER BY startTime DESC
    """)
    fun getActivitiesForMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<ActivityEntity>>

    @Query("""
        SELECT SUM(distanceMeters) FROM activities
        WHERE startTime >= :startTime AND startTime < :endTime
    """)
    suspend fun getTotalDistanceForPeriod(startTime: Long, endTime: Long): Double?

    @Query("""
        SELECT SUM(durationSeconds) FROM activities
        WHERE startTime >= :startTime AND startTime < :endTime
    """)
    suspend fun getTotalDurationForPeriod(startTime: Long, endTime: Long): Long?

    @Query("""
        SELECT COUNT(*) FROM activities
        WHERE startTime >= :startTime AND startTime < :endTime
    """)
    suspend fun getActivityCountForPeriod(startTime: Long, endTime: Long): Int

    @Query("""
        SELECT SUM(caloriesBurned) FROM activities
        WHERE startTime >= :startTime AND startTime < :endTime
    """)
    suspend fun getTotalCaloriesForPeriod(startTime: Long, endTime: Long): Int?
}
