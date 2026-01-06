package com.example.mobile_final.domain.repository

import com.example.mobile_final.domain.model.WeatherData

/**
 * Repository interface for fetching weather data.
 */
interface WeatherRepository {
    /**
     * Fetches current weather for the given GPS coordinates.
     *
     * @param latitude GPS latitude
     * @param longitude GPS longitude
     * @return Result containing WeatherData on success, or exception on failure
     */
    suspend fun getWeatherForLocation(latitude: Double, longitude: Double): Result<WeatherData>
}
