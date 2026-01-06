package com.example.mobile_final.data.repository

import com.example.mobile_final.domain.repository.ActiveSessionRepository
import com.example.mobile_final.service.TrackingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ActiveSessionRepository that maintains a global tracking state.
 * This is updated by TrackingService and observed by any UI component (e.g., HomeScreen).
 */
@Singleton
class ActiveSessionRepositoryImpl @Inject constructor() : ActiveSessionRepository {

    private val _trackingState = MutableStateFlow(TrackingState())
    override val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    override fun updateTrackingState(state: TrackingState) {
        _trackingState.value = state
        _isTracking.value = state.isTracking
    }
}
