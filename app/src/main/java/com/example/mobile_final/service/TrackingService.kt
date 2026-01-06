package com.example.mobile_final.service

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.mobile_final.MainActivity
import com.example.mobile_final.R
import com.example.mobile_final.RunTrackerApp
import com.example.mobile_final.data.local.entity.ActivityType
import com.example.mobile_final.domain.model.Activity
import com.example.mobile_final.domain.model.LocationPoint
import com.example.mobile_final.domain.model.WeatherData
import com.example.mobile_final.domain.repository.ActiveSessionRepository
import com.example.mobile_final.domain.repository.ActivityRepository
import com.example.mobile_final.domain.repository.WeatherRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.example.mobile_final.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import android.util.Log
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class TrackingService : LifecycleService(), SensorEventListener {

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var activityRepository: ActivityRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var weatherRepository: WeatherRepository

    @Inject
    lateinit var activeSessionRepository: ActiveSessionRepository

    private val binder = LocalBinder()

    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    private var initialStepCount: Int = -1

    private var currentActivityId: Long = -1L
    private var activityIdDeferred: CompletableDeferred<Long>? = null
    private var lastLocation: Location? = null
    private var userWeightKg: Float = 70f

    // Weather data for the current activity
    private var activityWeatherData: WeatherData? = null
    private var hasAttemptedWeatherFetch: Boolean = false

    // Tracking state
    private val _trackingState = MutableStateFlow(TrackingState())
    val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    private val _locationPoints = MutableStateFlow<List<LocationPoint>>(emptyList())
    val locationPoints: StateFlow<List<LocationPoint>> = _locationPoints.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private var startTime: Long = 0L
    private var pausedTime: Long = 0L
    private var totalPausedDuration: Long = 0L

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (_isTracking.value && !_isPaused.value) {
                result.lastLocation?.let { location ->
                    addLocationPoint(location)
                }
            }
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): TrackingService = this@TrackingService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START -> {
                val activityType = intent.getStringExtra(EXTRA_ACTIVITY_TYPE) ?: "running"
                startTracking(ActivityType.fromString(activityType))
            }
            ACTION_PAUSE -> pauseTracking()
            ACTION_RESUME -> resumeTracking()
            ACTION_STOP -> stopTracking()
        }

        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startTracking(activityType: ActivityType) {
        if (_isTracking.value) return

        _isTracking.value = true
        _isPaused.value = false
        startTime = System.currentTimeMillis()
        totalPausedDuration = 0L
        initialStepCount = -1
        lastLocation = null
        activityIdDeferred = CompletableDeferred()

        _trackingState.value = TrackingState(
            activityType = activityType,
            isTracking = true
        )
        publishTrackingState()
        _locationPoints.value = emptyList()

        // Reset weather data for new activity
        activityWeatherData = null
        hasAttemptedWeatherFetch = false

        // Load user weight and create activity in database
        lifecycleScope.launch {
            // Load user's weight from settings
            val settings = settingsRepository.getSettingsOnce()
            userWeightKg = settings.weight

            val activity = Activity(
                type = activityType,
                startTime = startTime
            )
            currentActivityId = activityRepository.insertActivity(activity)
            activityIdDeferred?.complete(currentActivityId)
        }

        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification())

        // Start location updates
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_UPDATE_INTERVAL
        ).apply {
            setMinUpdateIntervalMillis(FASTEST_LOCATION_INTERVAL)
            setMinUpdateDistanceMeters(MIN_DISTANCE_METERS)
        }.build()

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        // Start step counter
        stepCounterSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Start timer updates
        startTimerUpdates()
    }

    private fun pauseTracking() {
        if (!_isTracking.value || _isPaused.value) return

        _isPaused.value = true
        pausedTime = System.currentTimeMillis()

        _trackingState.value = _trackingState.value.copy(isPaused = true)
        publishTrackingState()
        updateNotification()
    }

    private fun resumeTracking() {
        if (!_isTracking.value || !_isPaused.value) return

        _isPaused.value = false
        totalPausedDuration += System.currentTimeMillis() - pausedTime

        _trackingState.value = _trackingState.value.copy(isPaused = false)
        publishTrackingState()
        updateNotification()
    }

    private fun stopTracking() {
        if (!_isTracking.value) return

        _isTracking.value = false
        _isPaused.value = false

        // Stop location updates
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)

        // Stop step counter
        sensorManager?.unregisterListener(this)

        // Finalize activity in database
        lifecycleScope.launch {
            if (currentActivityId != -1L) {
                val state = _trackingState.value
                val endTime = System.currentTimeMillis()
                val duration = calculateDuration()

                val activity = Activity(
                    id = currentActivityId,
                    type = state.activityType,
                    startTime = startTime,
                    endTime = endTime,
                    distanceMeters = state.distanceMeters,
                    durationSeconds = duration / 1000,
                    caloriesBurned = state.calories,
                    avgPaceSecondsPerKm = if (state.distanceMeters > 0) {
                        ((duration / 1000.0) / (state.distanceMeters / 1000.0)).roundToInt()
                    } else 0,
                    stepCount = state.steps,
                    // Include weather data captured at activity start
                    weatherTemperature = activityWeatherData?.temperatureCelsius,
                    weatherHumidity = activityWeatherData?.humidity,
                    weatherCode = activityWeatherData?.weatherCode,
                    weatherWindSpeed = activityWeatherData?.windSpeedKmh,
                    weatherDescription = activityWeatherData?.description
                )
                activityRepository.updateActivity(activity)
            }
        }

        _trackingState.value = TrackingState()
        publishTrackingState()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun addLocationPoint(location: Location) {
        // Fetch weather on first valid location point
        if (!hasAttemptedWeatherFetch) {
            hasAttemptedWeatherFetch = true
            fetchWeatherForLocation(location.latitude, location.longitude)
        }

        // Calculate distance first (doesn't need activity ID)
        lastLocation?.let { last ->
            val distance = last.distanceTo(location)
            if (distance > 1f) { // Only count if moved more than 1 meter
                val currentState = _trackingState.value
                val newDistance = currentState.distanceMeters + distance

                _trackingState.value = currentState.copy(
                    distanceMeters = newDistance,
                    currentSpeed = location.speed,
                    calories = calculateCalories(newDistance, currentState.activityType)
                )
            }
        }

        lastLocation = location

        // Save to database - wait for activity ID to be available
        lifecycleScope.launch {
            val activityId = activityIdDeferred?.await() ?: return@launch

            val point = LocationPoint(
                activityId = activityId,
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = if (location.hasAltitude()) location.altitude else null,
                timestamp = System.currentTimeMillis(),
                speedMps = if (location.hasSpeed()) location.speed else null
            )

            // Add to local list
            _locationPoints.value = _locationPoints.value + point

            activityRepository.insertLocationPoint(point)
        }

        updateNotification()
    }

    /**
     * Fetches weather data for the current location.
     * Non-blocking: failures are logged but don't interrupt tracking.
     */
    private fun fetchWeatherForLocation(latitude: Double, longitude: Double) {
        lifecycleScope.launch {
            weatherRepository.getWeatherForLocation(latitude, longitude)
                .onSuccess { weather ->
                    activityWeatherData = weather
                    Log.d(TAG, "Weather fetched: ${weather.description}, ${weather.temperatureCelsius}°C")
                }
                .onFailure { error ->
                    Log.w(TAG, "Failed to fetch weather: ${error.message}")
                    // Activity continues without weather data
                }
        }
    }

    private fun startTimerUpdates() {
        lifecycleScope.launch {
            while (_isTracking.value) {
                if (!_isPaused.value) {
                    val duration = calculateDuration()
                    val currentState = _trackingState.value

                    _trackingState.value = currentState.copy(
                        durationMillis = duration,
                        avgPace = if (currentState.distanceMeters > 0) {
                            (duration / 1000.0) / (currentState.distanceMeters / 1000.0)
                        } else 0.0
                    )
                    publishTrackingState()
                    updateNotification()
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    /**
     * Publishes the current tracking state to the ActiveSessionRepository
     * so it can be observed by other UI components (e.g., HomeScreen).
     */
    private fun publishTrackingState() {
        activeSessionRepository.updateTrackingState(_trackingState.value)
    }

    private fun calculateDuration(): Long {
        val currentTime = if (_isPaused.value) pausedTime else System.currentTimeMillis()
        return currentTime - startTime - totalPausedDuration
    }

    private fun calculateCalories(distanceMeters: Double, activityType: ActivityType): Int {
        // Simple calorie calculation based on activity type and distance
        // MET values: Running ~10, Walking ~3.5, Cycling ~7
        val metValue = when (activityType) {
            ActivityType.RUNNING -> 10.0
            ActivityType.WALKING -> 3.5
            ActivityType.CYCLING -> 7.0
        }
        val durationHours = calculateDuration() / 3600000.0
        return (metValue * userWeightKg * durationHours).roundToInt()
    }

    private fun createNotification(): android.app.Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val state = _trackingState.value
        val duration = formatDuration(state.durationMillis)
        val distance = String.format("%.2f km", state.distanceMeters / 1000)

        return NotificationCompat.Builder(this, RunTrackerApp.TRACKING_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Tracking ${state.activityType.displayName}")
            .setContentText("$duration • $distance")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    private fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / 60000) % 60
        val hours = millis / 3600000
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    // SensorEventListener
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                val totalSteps = it.values[0].toInt()
                if (initialStepCount == -1) {
                    initialStepCount = totalSteps
                }
                val sessionSteps = totalSteps - initialStepCount
                _trackingState.value = _trackingState.value.copy(steps = sessionSteps)
                publishTrackingState()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        sensorManager?.unregisterListener(this)
    }

    fun getActivityId(): Long = currentActivityId

    suspend fun getActivityIdAsync(): Long {
        return activityIdDeferred?.await() ?: -1L
    }

    companion object {
        private const val TAG = "TrackingService"

        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_ACTIVITY_TYPE = "EXTRA_ACTIVITY_TYPE"

        private const val NOTIFICATION_ID = 1
        private const val LOCATION_UPDATE_INTERVAL = 3000L // 3 seconds
        private const val FASTEST_LOCATION_INTERVAL = 2000L // 2 seconds
        private const val MIN_DISTANCE_METERS = 5f // 5 meters
    }
}

data class TrackingState(
    val activityType: ActivityType = ActivityType.RUNNING,
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val durationMillis: Long = 0L,
    val distanceMeters: Double = 0.0,
    val currentSpeed: Float = 0f,
    val avgPace: Double = 0.0, // seconds per km
    val steps: Int = 0,
    val calories: Int = 0
)
