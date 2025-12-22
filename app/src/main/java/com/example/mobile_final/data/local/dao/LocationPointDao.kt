package com.example.mobile_final.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mobile_final.data.local.entity.LocationPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationPointDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationPoint(locationPoint: LocationPointEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationPoints(locationPoints: List<LocationPointEntity>)

    @Query("SELECT * FROM location_points WHERE activityId = :activityId ORDER BY timestamp ASC")
    suspend fun getLocationPointsForActivity(activityId: Long): List<LocationPointEntity>

    @Query("SELECT * FROM location_points WHERE activityId = :activityId ORDER BY timestamp ASC")
    fun getLocationPointsForActivityFlow(activityId: Long): Flow<List<LocationPointEntity>>

    @Query("DELETE FROM location_points WHERE activityId = :activityId")
    suspend fun deleteLocationPointsForActivity(activityId: Long)

    @Query("SELECT COUNT(*) FROM location_points WHERE activityId = :activityId")
    suspend fun getLocationPointCountForActivity(activityId: Long): Int
}
