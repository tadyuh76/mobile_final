package com.example.mobile_final.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = false
)
abstract class RunTrackerDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun locationPointDao(): LocationPointDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        const val DATABASE_NAME = "run_tracker_database"

        /**
         * Migration from version 1 to 2: Add weather columns to activities table.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE activities ADD COLUMN weatherTemperature REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE activities ADD COLUMN weatherHumidity INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE activities ADD COLUMN weatherCode INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE activities ADD COLUMN weatherWindSpeed REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE activities ADD COLUMN weatherDescription TEXT DEFAULT NULL")
            }
        }
    }
}
