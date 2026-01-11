package com.example.mobile_final.ui.screen.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_final.domain.model.Activity
import com.example.mobile_final.domain.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DailyStats(
    val dayOfWeek: String,
    val shortDay: String,
    val distanceKm: Double,
    val durationMinutes: Long,
    val calories: Int,
    val activityCount: Int
)

data class WeeklyStats(
    val weekNumber: Int,
    val startDate: Long,
    val distanceKm: Double,
    val durationMinutes: Long,
    val calories: Int,
    val activityCount: Int
)

data class StatsUiState(
    val selectedPeriod: StatsPeriod = StatsPeriod.WEEK,
    val dailyStats: List<DailyStats> = emptyList(),
    val weeklyStats: List<WeeklyStats> = emptyList(),
    val totalDistance: Double = 0.0,
    val totalDuration: Long = 0,
    val totalCalories: Int = 0,
    val totalActivities: Int = 0,
    val averageDistance: Double = 0.0,
    val averagePace: Double = 0.0,
    val isLoading: Boolean = true
)

enum class StatsPeriod {
    WEEK, MONTH
}

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun selectPeriod(period: StatsPeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (_uiState.value.selectedPeriod) {
                StatsPeriod.WEEK -> loadWeeklyStats()
                StatsPeriod.MONTH -> loadMonthlyStats()
            }
        }
    }

    private suspend fun loadWeeklyStats() {
        val calendar = Calendar.getInstance()
        val dailyStatsList = mutableListOf<DailyStats>()

        // Get stats for each day of the current week (Monday start)
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val fullDayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

        for (i in 0 until 7) {
            val startOfDay = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val endOfDay = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, -1)

            val distance = activityRepository.getTotalDistanceForPeriod(startOfDay, endOfDay)
            val duration = activityRepository.getTotalDurationForPeriod(startOfDay, endOfDay)
            val calories = activityRepository.getTotalCaloriesForPeriod(startOfDay, endOfDay)
            val count = activityRepository.getActivityCountForPeriod(startOfDay, endOfDay)

            dailyStatsList.add(
                DailyStats(
                    dayOfWeek = fullDayNames[i],
                    shortDay = dayNames[i],
                    distanceKm = distance / 1000.0,
                    durationMinutes = duration / 60,
                    calories = calories,
                    activityCount = count
                )
            )

            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Calculate totals for the week
        val totalDistance = dailyStatsList.sumOf { it.distanceKm }
        val totalDuration = dailyStatsList.sumOf { it.durationMinutes }
        val totalCalories = dailyStatsList.sumOf { it.calories }
        val totalActivities = dailyStatsList.sumOf { it.activityCount }
        val avgDistance = if (totalActivities > 0) totalDistance / totalActivities else 0.0
        val avgPace = if (totalDistance > 0) (totalDuration * 60.0) / totalDistance else 0.0

        _uiState.value = _uiState.value.copy(
            dailyStats = dailyStatsList,
            totalDistance = totalDistance,
            totalDuration = totalDuration,
            totalCalories = totalCalories,
            totalActivities = totalActivities,
            averageDistance = avgDistance,
            averagePace = avgPace,
            isLoading = false
        )
    }

    private suspend fun loadMonthlyStats() {
        val calendar = Calendar.getInstance()
        val weeklyStatsList = mutableListOf<WeeklyStats>()

        // Get stats for the last 4 weeks (Monday start)
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Go back 3 weeks to start from 4 weeks ago
        calendar.add(Calendar.WEEK_OF_YEAR, -3)

        for (i in 0 until 4) {
            val startOfWeek = calendar.timeInMillis
            val weekNumber = calendar.get(Calendar.WEEK_OF_YEAR)
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            val endOfWeek = calendar.timeInMillis

            val distance = activityRepository.getTotalDistanceForPeriod(startOfWeek, endOfWeek)
            val duration = activityRepository.getTotalDurationForPeriod(startOfWeek, endOfWeek)
            val calories = activityRepository.getTotalCaloriesForPeriod(startOfWeek, endOfWeek)
            val count = activityRepository.getActivityCountForPeriod(startOfWeek, endOfWeek)

            weeklyStatsList.add(
                WeeklyStats(
                    weekNumber = weekNumber,
                    startDate = startOfWeek,
                    distanceKm = distance / 1000.0,
                    durationMinutes = duration / 60,
                    calories = calories,
                    activityCount = count
                )
            )
        }

        // Calculate totals for the month
        val totalDistance = weeklyStatsList.sumOf { it.distanceKm }
        val totalDuration = weeklyStatsList.sumOf { it.durationMinutes }
        val totalCalories = weeklyStatsList.sumOf { it.calories }
        val totalActivities = weeklyStatsList.sumOf { it.activityCount }
        val avgDistance = if (totalActivities > 0) totalDistance / totalActivities else 0.0
        val avgPace = if (totalDistance > 0) (totalDuration * 60.0) / totalDistance else 0.0

        _uiState.value = _uiState.value.copy(
            weeklyStats = weeklyStatsList,
            totalDistance = totalDistance,
            totalDuration = totalDuration,
            totalCalories = totalCalories,
            totalActivities = totalActivities,
            averageDistance = avgDistance,
            averagePace = avgPace,
            isLoading = false
        )
    }
}
