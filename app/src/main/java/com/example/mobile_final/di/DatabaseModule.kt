package com.example.mobile_final.di

import android.content.Context
import androidx.room.Room
import com.example.mobile_final.data.local.RunTrackerDatabase
import com.example.mobile_final.data.local.dao.ActivityDao
import com.example.mobile_final.data.local.dao.LocationPointDao
import com.example.mobile_final.data.local.dao.UserSettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RunTrackerDatabase {
        return Room.databaseBuilder(
            context,
            RunTrackerDatabase::class.java,
            RunTrackerDatabase.DATABASE_NAME
        )
            .addMigrations(RunTrackerDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration() // Fallback for future migrations if needed
            .build()
    }

    @Provides
    @Singleton
    fun provideActivityDao(database: RunTrackerDatabase): ActivityDao {
        return database.activityDao()
    }

    @Provides
    @Singleton
    fun provideLocationPointDao(database: RunTrackerDatabase): LocationPointDao {
        return database.locationPointDao()
    }

    @Provides
    @Singleton
    fun provideUserSettingsDao(database: RunTrackerDatabase): UserSettingsDao {
        return database.userSettingsDao()
    }
}
