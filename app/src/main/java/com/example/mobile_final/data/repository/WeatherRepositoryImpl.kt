package com.example.mobile_final.data.repository

import com.example.mobile_final.data.remote.api.WeatherApi
import com.example.mobile_final.di.IoDispatcher
import com.example.mobile_final.domain.model.WeatherData
import com.example.mobile_final.domain.repository.WeatherRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of WeatherRepository that fetches data from Open-Meteo API.
 * Uses Retrofit for network calls and runs on IO dispatcher.
 */
@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : WeatherRepository {

    override suspend fun getWeatherForLocation(
        latitude: Double,
        longitude: Double
    ): Result<WeatherData> = withContext(ioDispatcher) {
        try {
            val response = weatherApi.getCurrentWeather(
                latitude = latitude,
                longitude = longitude
            )
            Result.success(WeatherData.fromDto(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
