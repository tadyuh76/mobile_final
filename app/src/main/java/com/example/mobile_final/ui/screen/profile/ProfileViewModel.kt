package com.example.mobile_final.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_final.domain.repository.ActivityRepository
import com.example.mobile_final.domain.repository.AuthRepository
import com.example.mobile_final.domain.repository.BackupInfo
import com.example.mobile_final.domain.repository.SyncRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SyncStatus(
    val syncedCount: Int = 0,
    val totalCount: Int = 0
) {
    val isSynced: Boolean get() = syncedCount == totalCount && totalCount > 0
    val syncPercentage: Int get() = if (totalCount > 0) (syncedCount * 100 / totalCount) else 0
}

data class ProfileUiState(
    val user: FirebaseUser? = null,
    val isSignedIn: Boolean = false,
    val isLoading: Boolean = false,
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val backupInfo: BackupInfo? = null,
    val syncStatus: SyncStatus = SyncStatus(),
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
        loadSyncStatus()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.value = _uiState.value.copy(
                    user = user,
                    isSignedIn = user != null
                )
                if (user != null) {
                    loadBackupInfo()
                    loadSyncStatus()
                }
            }
        }
    }

    private fun loadSyncStatus() {
        viewModelScope.launch {
            val syncedCount = activityRepository.getSyncedActivityCount()
            val totalCount = activityRepository.getTotalActivityCount()
            _uiState.value = _uiState.value.copy(
                syncStatus = SyncStatus(syncedCount, totalCount)
            )
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            authRepository.signInWithGoogle(idToken).fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        isSignedIn = true,
                        isLoading = false,
                        message = "Signed in successfully"
                    )
                    loadBackupInfo()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Sign in failed"
                    )
                }
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = _uiState.value.copy(
                user = null,
                isSignedIn = false,
                backupInfo = null,
                message = "Signed out successfully"
            )
        }
    }

    fun backupData() {
        val userId = _uiState.value.user?.uid ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBackingUp = true, error = null)

            syncRepository.backupActivities(userId).fold(
                onSuccess = { count ->
                    _uiState.value = _uiState.value.copy(
                        isBackingUp = false,
                        message = "Backed up $count activities"
                    )
                    loadBackupInfo()
                    loadSyncStatus()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isBackingUp = false,
                        error = exception.message ?: "Backup failed"
                    )
                }
            )
        }
    }

    fun restoreData() {
        val userId = _uiState.value.user?.uid ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRestoring = true, error = null)

            syncRepository.restoreActivities(userId).fold(
                onSuccess = { count ->
                    _uiState.value = _uiState.value.copy(
                        isRestoring = false,
                        message = "Restored $count activities"
                    )
                    loadSyncStatus()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isRestoring = false,
                        error = exception.message ?: "Restore failed"
                    )
                }
            )
        }
    }

    private fun loadBackupInfo() {
        val userId = _uiState.value.user?.uid ?: return

        viewModelScope.launch {
            syncRepository.getBackupInfo(userId).fold(
                onSuccess = { info ->
                    _uiState.value = _uiState.value.copy(backupInfo = info)
                },
                onFailure = { /* Ignore */ }
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(error = message)
    }
}
