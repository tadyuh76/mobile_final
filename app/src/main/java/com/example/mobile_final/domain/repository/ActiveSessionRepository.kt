package com.example.mobile_final.domain.repository

import com.example.mobile_final.service.TrackingState
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository for accessing the current active tracking session state.
 * This allows any part of the app to observe whether tracking is in progress.
 */
interface ActiveSessionRepository {
    val trackingState: StateFlow<TrackingState>
    val isTracking: StateFlow<Boolean>

    fun updateTrackingState(state: TrackingState)
}
