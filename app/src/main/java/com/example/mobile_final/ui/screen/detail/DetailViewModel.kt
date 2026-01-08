package com.example.mobile_final.ui.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_final.domain.model.Activity
import com.example.mobile_final.domain.model.LocationPoint
import com.example.mobile_final.domain.repository.ActivityRepository
import com.example.mobile_final.domain.repository.AuthRepository
import com.example.mobile_final.domain.repository.SocialRepository
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
    val error: String? = null,
    val isSharingInProgress: Boolean = false
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val socialRepository: SocialRepository,
    private val authRepository: AuthRepository,
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

    fun toggleShareActivity() {
        val currentActivity = _uiState.value.activity ?: return
        val currentLocationPoints = _uiState.value.locationPoints

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSharingInProgress = true)

            try {
                val newPublicStatus = !currentActivity.isPublic

                // Update local database first
                activityRepository.updateActivityPublicStatus(currentActivity.id, newPublicStatus)

                // Sync with Firestore
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    if (newPublicStatus) {
                        // Publishing to social feed
                        val result = socialRepository.publishActivity(
                            userId = currentUser.uid,
                            userDisplayName = currentUser.displayName,
                            activity = currentActivity.copy(isPublic = true),
                            locationPoints = currentLocationPoints
                        )
                        if (result.isFailure) {
                            // Revert local change if Firestore sync fails
                            activityRepository.updateActivityPublicStatus(currentActivity.id, false)
                            _uiState.value = _uiState.value.copy(
                                error = "Failed to share activity: ${result.exceptionOrNull()?.message}",
                                isSharingInProgress = false
                            )
                            return@launch
                        }
                    } else {
                        // Unpublishing from social feed
                        val result = socialRepository.unpublishActivity(
                            userId = currentUser.uid,
                            activityStartTime = currentActivity.startTime
                        )
                        if (result.isFailure) {
                            // Revert local change if Firestore sync fails
                            activityRepository.updateActivityPublicStatus(currentActivity.id, true)
                            _uiState.value = _uiState.value.copy(
                                error = "Failed to unshare activity: ${result.exceptionOrNull()?.message}",
                                isSharingInProgress = false
                            )
                            return@launch
                        }
                    }
                }

                // Reload activity to get updated state
                loadActivityDetails()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update sharing status: ${e.message}",
                    isSharingInProgress = false
                )
            }
        }
    }
}
