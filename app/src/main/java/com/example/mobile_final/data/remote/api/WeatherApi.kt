package com.example.mobile_final.data.remote.api

import com.example.mobile_final.data.remote.dto.WeatherResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API interface for Open-Meteo Weather API.
 * Provides current weather data based on GPS coordinates.
 */
interface WeatherApi {

    @GET("forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = CURRENT_WEATHER_PARAMS
    ): WeatherResponseDto

    companion object {
        const val BASE_URL = "https://api.open-meteo.com/v1/"

        // Weather parameters to fetch: temperature, humidity, weather code, wind speed
        private const val CURRENT_WEATHER_PARAMS =
            "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m"
    }
}
