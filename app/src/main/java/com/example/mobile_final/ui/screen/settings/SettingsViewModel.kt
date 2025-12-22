package com.example.mobile_final.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_final.data.local.entity.ActivityType
import com.example.mobile_final.domain.model.UserSettings
import com.example.mobile_final.domain.repository.SettingsRepository
import com.example.mobile_final.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showWeightDialog: Boolean = false,
    val showThemeDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _uiState.value = _uiState.value.copy(
                    settings = settings,
                    isLoading = false
                )
            }
        }
    }

    fun updateActivityType(activityType: ActivityType) {
        val currentSettings = _uiState.value.settings
        val newSettings = currentSettings.copy(preferredActivityType = activityType)
        saveSettings(newSettings)
    }

    fun updateUseMetricUnits(useMetric: Boolean) {
        val currentSettings = _uiState.value.settings
        val newSettings = currentSettings.copy(useMetricUnits = useMetric)
        saveSettings(newSettings)
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        val currentSettings = _uiState.value.settings
        val newSettings = currentSettings.copy(notificationsEnabled = enabled)
        saveSettings(newSettings)
    }

    fun updateWeight(weight: Float) {
        val currentSettings = _uiState.value.settings
        val newSettings = currentSettings.copy(weight = weight)
        saveSettings(newSettings)
        hideWeightDialog()
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        val currentSettings = _uiState.value.settings
        val newSettings = currentSettings.copy(themeMode = themeMode)
        saveSettings(newSettings)
        hideThemeDialog()
    }

    fun showWeightDialog() {
        _uiState.value = _uiState.value.copy(showWeightDialog = true)
    }

    fun hideWeightDialog() {
        _uiState.value = _uiState.value.copy(showWeightDialog = false)
    }

    fun showThemeDialog() {
        _uiState.value = _uiState.value.copy(showThemeDialog = true)
    }

    fun hideThemeDialog() {
        _uiState.value = _uiState.value.copy(showThemeDialog = false)
    }

    private fun saveSettings(settings: UserSettings) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            settingsRepository.updateSettings(settings)
            _uiState.value = _uiState.value.copy(
                settings = settings,
                isSaving = false
            )
        }
    }
}
