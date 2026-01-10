package com.example.mobile_final.di

import android.content.Context
import androidx.room.Room
import com.example.mobile_final.data.local.LegItDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): LegItDatabase {
        return Room.databaseBuilder(
            context,
            LegItDatabase::class.java,
            LegItDatabase.DATABASE_NAME
        )
            .addMigrations(
                LegItDatabase.MIGRATION_1_2,
                LegItDatabase.MIGRATION_2_3
            )
            .fallbackToDestructiveMigration() // Fallback for future migrations if needed
            .build()
    }

    @Provides
    @Singleton
    fun provideActivityDao(database: LegItDatabase): ActivityDao {
        return database.activityDao()
    }

    @Provides
    @Singleton
    fun provideLocationPointDao(database: LegItDatabase): LocationPointDao {
        return database.locationPointDao()
    }

    @Provides
    @Singleton
    fun provideUserSettingsDao(database: LegItDatabase): UserSettingsDao {
        return database.userSettingsDao()
    }
}
