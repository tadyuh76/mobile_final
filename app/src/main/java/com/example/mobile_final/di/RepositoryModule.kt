package com.example.mobile_final.di

import com.example.mobile_final.data.repository.ActivityRepositoryImpl
import com.example.mobile_final.data.repository.SettingsRepositoryImpl
import com.example.mobile_final.domain.repository.ActivityRepository
import com.example.mobile_final.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindActivityRepository(
        activityRepositoryImpl: ActivityRepositoryImpl
    ): ActivityRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository
}
