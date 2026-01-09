package com.example.mobile_final.ui.screen.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_final.domain.repository.AuthRepository
import com.example.mobile_final.domain.repository.SocialActivity
import com.example.mobile_final.domain.repository.SocialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SocialUiState(
    val activities: List<SocialActivity> = emptyList(),
    val currentUserId: String? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocialUiState())
    val uiState: StateFlow<SocialUiState> = _uiState.asStateFlow()

    private var collectJob: Job? = null

    init {
        loadCurrentUser()
        loadPublicActivities()
    }

    private fun loadCurrentUser() {
        val currentUser = authRepository.getCurrentUser()
        _uiState.value = _uiState.value.copy(currentUserId = currentUser?.uid)
    }

    private fun loadPublicActivities() {
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            socialRepository.getPublicActivities()
                .onStart {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message ?: "Failed to load activities"
                    )
                }
                .collect { activities ->
                    _uiState.value = _uiState.value.copy(
                        activities = activities,
                        isLoading = false,
                        isRefreshing = false,
                        error = null
                    )
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            // Brief delay to show refresh indicator, then reset
            // The Firestore listener already provides real-time updates
            delay(500)
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }
}
