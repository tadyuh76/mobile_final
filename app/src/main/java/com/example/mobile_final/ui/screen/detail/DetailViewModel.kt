package com.example.mobile_final.ui.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_final.domain.model.Activity
import com.example.mobile_final.domain.model.LocationPoint
import com.example.mobile_final.domain.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val activity: Activity? = null,
    val locationPoints: List<LocationPoint> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val activityId: Long = savedStateHandle.get<Long>("activityId") ?: -1L

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadActivityDetails()
    }

    private fun loadActivityDetails() {
        if (activityId == -1L) {
            _uiState.value = DetailUiState(
                isLoading = false,
                error = "Activity not found"
            )
            return
        }

        viewModelScope.launch {
            try {
                val activity = activityRepository.getActivityById(activityId)
                val locationPoints = activityRepository.getLocationPointsForActivity(activityId)

                _uiState.value = DetailUiState(
                    activity = activity,
                    locationPoints = locationPoints,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = DetailUiState(
                    isLoading = false,
                    error = e.message ?: "Failed to load activity"
                )
            }
        }
    }

    fun deleteActivity() {
        viewModelScope.launch {
            _uiState.value.activity?.let {
                activityRepository.deleteActivity(it)
            }
        }
    }
}
