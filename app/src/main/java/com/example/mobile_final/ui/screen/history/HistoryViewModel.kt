package com.example.mobile_final.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_final.domain.model.Activity
import com.example.mobile_final.domain.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val activities: List<Activity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState(isLoading = true))
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadActivities()
    }

    private fun loadActivities() {
        // Cancel any existing collection to avoid duplicates
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            activityRepository.getAllActivities().collect { activities ->
                _uiState.value = HistoryUiState(
                    activities = activities,
                    isLoading = false
                )
            }
        }
    }

    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            activityRepository.deleteActivity(activity)
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadActivities()
    }
}
