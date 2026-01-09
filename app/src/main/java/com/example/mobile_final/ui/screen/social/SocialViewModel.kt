package com.example.mobile_final.ui.screen.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_final.domain.repository.AuthRepository
import com.example.mobile_final.domain.repository.SocialActivity
import com.example.mobile_final.domain.repository.SocialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SocialUiState(
    val activities: List<SocialActivity> = emptyList(),
    val currentUserId: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val socialRepository: SocialRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocialUiState())
    val uiState: StateFlow<SocialUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
        loadPublicActivities()
    }

    private fun loadCurrentUser() {
        val currentUser = authRepository.getCurrentUser()
        _uiState.value = _uiState.value.copy(currentUserId = currentUser?.uid)
    }

    private fun loadPublicActivities() {
        viewModelScope.launch {
            try {
                socialRepository.getPublicActivities().collect { activities ->
                    _uiState.value = _uiState.value.copy(
                        activities = activities,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load activities"
                )
            }
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadPublicActivities()
    }
}
