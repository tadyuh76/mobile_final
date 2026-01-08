package com.example.mobile_final.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_final.domain.model.Activity
import com.example.mobile_final.domain.model.LocationPoint
import com.example.mobile_final.domain.repository.ActiveSessionRepository
import com.example.mobile_final.domain.repository.ActivityRepository
import com.example.mobile_final.service.TrackingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val todayDistanceKm: Double = 0.0,
    val todayDurationSeconds: Long = 0,
    val todayCalories: Int = 0,
    val weeklyDistanceKm: Double = 0.0,
    val weeklyActivityCount: Int = 0,
    val weeklyCalories: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val activeSessionRepository: ActiveSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Expose active tracking session state for the UI
    val activeTrackingState: StateFlow<TrackingState> = activeSessionRepository.trackingState

    // Expose activities with location points for social feed display
    val activitiesWithLocations: StateFlow<List<Pair<Activity, List<LocationPoint>>>> =
        activityRepository.getAllActivitiesWithLocationPoints()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    init {
        loadStats()
    }

    fun toggleActivityPublic(activityId: Long, isPublic: Boolean) {
        viewModelScope.launch {
            activityRepository.updateActivityPublicStatus(activityId, isPublic)
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val calendar = Calendar.getInstance()

            // Today's stats
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis

            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val endOfDay = calendar.timeInMillis

            val todayDistance = activityRepository.getTotalDistanceForPeriod(startOfDay, endOfDay)
            val todayDuration = activityRepository.getTotalDurationForPeriod(startOfDay, endOfDay)
            val todayCalories = activityRepository.getTotalCaloriesForPeriod(startOfDay, endOfDay)

            // Weekly stats
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfWeek = calendar.timeInMillis

            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            val endOfWeek = calendar.timeInMillis

            val weeklyDistance = activityRepository.getTotalDistanceForPeriod(startOfWeek, endOfWeek)
            val weeklyCount = activityRepository.getActivityCountForPeriod(startOfWeek, endOfWeek)
            val weeklyCalories = activityRepository.getTotalCaloriesForPeriod(startOfWeek, endOfWeek)

            _uiState.value = HomeUiState(
                todayDistanceKm = todayDistance / 1000.0,
                todayDurationSeconds = todayDuration,
                todayCalories = todayCalories,
                weeklyDistanceKm = weeklyDistance / 1000.0,
                weeklyActivityCount = weeklyCount,
                weeklyCalories = weeklyCalories,
                isLoading = false
            )
        }
    }
}
