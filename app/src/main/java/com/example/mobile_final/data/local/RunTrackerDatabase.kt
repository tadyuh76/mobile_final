package com.example.mobile_final.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mobile_final.data.local.dao.ActivityDao
import com.example.mobile_final.data.local.dao.LocationPointDao
import com.example.mobile_final.data.local.dao.UserSettingsDao
import com.example.mobile_final.data.local.entity.ActivityEntity
import com.example.mobile_final.data.local.entity.LocationPointEntity
import com.example.mobile_final.data.local.entity.UserSettingsEntity

@Database(
    entities = [
        ActivityEntity::class,
        LocationPointEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RunTrackerDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun locationPointDao(): LocationPointDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        const val DATABASE_NAME = "run_tracker_database"
    }
}
