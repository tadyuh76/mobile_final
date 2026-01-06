package com.example.mobile_final.domain.model

import com.example.mobile_final.data.remote.dto.WeatherResponseDto

/**
 * Domain model for weather data associated with an activity.
 * Contains weather conditions at the time of the workout.
 */
data class WeatherData(
    val temperatureCelsius: Double,
    val humidity: Int,
    val weatherCode: Int,
    val windSpeedKmh: Double,
    val description: String
) {
    companion object {
        /**
         * Creates WeatherData from API response DTO.
         */
        fun fromDto(dto: WeatherResponseDto): WeatherData {
            val code = dto.current.weatherCode
            return WeatherData(
                temperatureCelsius = dto.current.temperature,
                humidity = dto.current.humidity,
                weatherCode = code,
                windSpeedKmh = dto.current.windSpeed,
                description = getWeatherDescription(code)
            )
        }

        /**
         * Maps WMO Weather interpretation codes to human-readable descriptions.
         * See: https://open-meteo.com/en/docs (WMO Weather interpretation codes)
         */
        private fun getWeatherDescription(code: Int): String {
            return when (code) {
                0 -> "Clear sky"
                1 -> "Mainly clear"
                2 -> "Partly cloudy"
                3 -> "Overcast"
                45, 48 -> "Foggy"
                51, 53, 55 -> "Drizzle"
                56, 57 -> "Freezing drizzle"
                61, 63, 65 -> "Rain"
                66, 67 -> "Freezing rain"
                71, 73, 75 -> "Snow"
                77 -> "Snow grains"
                80, 81, 82 -> "Rain showers"
                85, 86 -> "Snow showers"
                95 -> "Thunderstorm"
                96, 99 -> "Thunderstorm with hail"
                else -> "Unknown"
            }
        }
    }
}
