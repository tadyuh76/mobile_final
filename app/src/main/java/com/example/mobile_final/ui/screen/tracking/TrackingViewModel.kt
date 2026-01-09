package com.example.mobile_final.ui.screen.tracking

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_final.data.local.entity.ActivityType
import com.example.mobile_final.domain.model.LocationPoint
import com.example.mobile_final.service.TrackingService
import com.example.mobile_final.service.TrackingState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackingViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var trackingService: TrackingService? = null
    private var isBound = false

    private val _trackingState = MutableStateFlow(TrackingState())
    val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    private val _locationPoints = MutableStateFlow<List<LocationPoint>>(emptyList())
    val locationPoints: StateFlow<List<LocationPoint>> = _locationPoints.asStateFlow()

    private val _selectedActivityType = MutableStateFlow(ActivityType.RUNNING)
    val selectedActivityType: StateFlow<ActivityType> = _selectedActivityType.asStateFlow()

    private val _isServiceBound = MutableStateFlow(false)
    val isServiceBound: StateFlow<Boolean> = _isServiceBound.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TrackingService.LocalBinder
            trackingService = binder.getService()
            isBound = true
            _isServiceBound.value = true

            // Collect service state
            viewModelScope.launch {
                trackingService?.trackingState?.collect { state ->
                    _trackingState.value = state
                }
            }
            viewModelScope.launch {
                trackingService?.locationPoints?.collect { points ->
                    _locationPoints.value = points
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            trackingService = null
            isBound = false
            _isServiceBound.value = false
        }
    }

    fun bindService() {
        Intent(context, TrackingService::class.java).also { intent ->
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbindService() {
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
            _isServiceBound.value = false
        }
    }

    fun setActivityType(type: ActivityType) {
        _selectedActivityType.value = type
    }

    fun startTracking() {
        val intent = Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_START
            putExtra(TrackingService.EXTRA_ACTIVITY_TYPE, _selectedActivityType.value.name.lowercase())
        }
        context.startForegroundService(intent)

        // Bind to service after starting
        bindService()
    }

    fun pauseTracking() {
        Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_PAUSE
        }.also { context.startService(it) }
    }

    fun resumeTracking() {
        Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_RESUME
        }.also { context.startService(it) }
    }

    fun stopTracking(): Long {
        val activityId = trackingService?.getActivityId() ?: -1L

        Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_STOP
        }.also { context.startService(it) }

        return activityId
    }

    fun stopTrackingAsync(onActivitySaved: (Long) -> Unit) {
        viewModelScope.launch {
            // Prepare for stop and get activity ID
            val activityId = trackingService?.stopAndWaitForCompletion() ?: -1L

            // Send stop intent to service
            Intent(context, TrackingService::class.java).apply {
                action = TrackingService.ACTION_STOP
            }.also { context.startService(it) }

            // Wait for the service to fully save the activity before navigating
            trackingService?.awaitStopCompletion()

            onActivitySaved(activityId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        unbindService()
    }
}
