package com.example.mobile_final.di

import com.example.mobile_final.data.repository.ActiveSessionRepositoryImpl
import com.example.mobile_final.data.repository.ActivityRepositoryImpl
import com.example.mobile_final.data.repository.AuthRepositoryImpl
import com.example.mobile_final.data.repository.SettingsRepositoryImpl
import com.example.mobile_final.data.repository.SocialRepositoryImpl
import com.example.mobile_final.data.repository.SyncRepositoryImpl
import com.example.mobile_final.data.repository.UserDataRepositoryImpl
import com.example.mobile_final.data.repository.WeatherRepositoryImpl
import com.example.mobile_final.domain.repository.ActiveSessionRepository
import com.example.mobile_final.domain.repository.ActivityRepository
import com.example.mobile_final.domain.repository.AuthRepository
import com.example.mobile_final.domain.repository.SettingsRepository
import com.example.mobile_final.domain.repository.SocialRepository
import com.example.mobile_final.domain.repository.SyncRepository
import com.example.mobile_final.domain.repository.UserDataRepository
import com.example.mobile_final.domain.repository.WeatherRepository
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

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(
        syncRepositoryImpl: SyncRepositoryImpl
    ): SyncRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        weatherRepositoryImpl: WeatherRepositoryImpl
    ): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindActiveSessionRepository(
        activeSessionRepositoryImpl: ActiveSessionRepositoryImpl
    ): ActiveSessionRepository

    @Binds
    @Singleton
    abstract fun bindSocialRepository(
        socialRepositoryImpl: SocialRepositoryImpl
    ): SocialRepository

    @Binds
    @Singleton
    abstract fun bindUserDataRepository(
        userDataRepositoryImpl: UserDataRepositoryImpl
    ): UserDataRepository
}
