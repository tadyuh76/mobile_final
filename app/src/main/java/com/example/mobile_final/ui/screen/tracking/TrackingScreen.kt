package com.example.mobile_final.ui.screen.tracking

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile_final.data.local.entity.ActivityType
import com.example.mobile_final.service.TrackingState
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    onNavigateBack: () -> Unit,
    onActivitySaved: (Long) -> Unit,
    viewModel: TrackingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val trackingState by viewModel.trackingState.collectAsState()
    val locationPoints by viewModel.locationPoints.collectAsState()
    val selectedActivityType by viewModel.selectedActivityType.collectAsState()

    var hasLocationPermission by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = permissions[Manifest.permission.POST_NOTIFICATIONS] == true
        }
    }

    // Check permissions on launch
    LaunchedEffect(Unit) {
        val fineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        hasLocationPermission = fineLocation
        hasNotificationPermission = notification

        if (!fineLocation || !notification) {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    // Bind/unbind service
    DisposableEffect(Unit) {
        viewModel.bindService()
        onDispose {
            viewModel.unbindService()
        }
    }

    val mapViewportState = rememberMapViewportState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Activity") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (trackingState.isTracking) {
                            viewModel.stopTracking()
                        }
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mapbox Map
            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapViewportState = mapViewportState,
                style = { MapStyle(style = "mapbox://styles/mapbox/streets-v12") }
            ) {
                // Enable location component
                MapEffect(Unit) { mapView ->
                    mapView.location.updateSettings {
                        enabled = true
                        locationPuck = createDefault2DPuck(withBearing = true)
                    }

                    // Center on user location initially
                    mapView.location.addOnIndicatorPositionChangedListener { point ->
                        if (!trackingState.isTracking) {
                            mapViewportState.flyTo(
                                com.mapbox.maps.CameraOptions.Builder()
                                    .center(point)
                                    .zoom(16.0)
                                    .build()
                            )
                        }
                    }
                }

                // Draw route polyline
                if (locationPoints.size >= 2) {
                    val points = locationPoints.map {
                        Point.fromLngLat(it.longitude, it.latitude)
                    }
                    PolylineAnnotation(
                        points = points,
                        lineColorInt = android.graphics.Color.parseColor("#4CAF50"),
                        lineWidth = 5.0
                    )
                }
            }

            // Stats Card at top
            if (trackingState.isTracking) {
                StatsCard(
                    trackingState = trackingState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                )
            }

            // Controls at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Activity Type Selector (only show when not tracking)
                if (!trackingState.isTracking) {
                    ActivityTypeSelector(
                        selectedType = selectedActivityType,
                        onTypeSelected = { viewModel.setActivityType(it) },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Control Buttons
                ControlButtons(
                    isTracking = trackingState.isTracking,
                    isPaused = trackingState.isPaused,
                    hasPermission = hasLocationPermission && hasNotificationPermission,
                    onStart = { viewModel.startTracking() },
                    onPause = { viewModel.pauseTracking() },
                    onResume = { viewModel.resumeTracking() },
                    onStop = {
                        val activityId = viewModel.stopTracking()
                        if (activityId != -1L) {
                            onActivitySaved(activityId)
                        } else {
                            onNavigateBack()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun StatsCard(
    trackingState: TrackingState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Timer
            Text(
                text = formatDuration(trackingState.durationMillis),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            if (trackingState.isPaused) {
                Text(
                    text = "PAUSED",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Distance",
                    value = String.format("%.2f", trackingState.distanceMeters / 1000),
                    unit = "km"
                )
                StatItem(
                    label = "Pace",
                    value = formatPace(trackingState.avgPace),
                    unit = "/km"
                )
                StatItem(
                    label = "Calories",
                    value = "${trackingState.calories}",
                    unit = "kcal"
                )
                StatItem(
                    label = "Steps",
                    value = "${trackingState.steps}",
                    unit = ""
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    unit: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActivityTypeSelector(
    selectedType: ActivityType,
    onTypeSelected: (ActivityType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActivityType.entries.forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    label = { Text(type.displayName) },
                    leadingIcon = {
                        Icon(
                            imageVector = getActivityIcon(type),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ControlButtons(
    isTracking: Boolean,
    isPaused: Boolean,
    hasPermission: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isTracking) {
            // Stop button
            FloatingActionButton(
                onClick = onStop,
                containerColor = MaterialTheme.colorScheme.error
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop"
                )
            }

            // Pause/Resume button
            LargeFloatingActionButton(
                onClick = if (isPaused) onResume else onPause,
                shape = CircleShape,
                containerColor = if (isPaused) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                }
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "Resume" else "Pause",
                    modifier = Modifier.size(36.dp)
                )
            }
        } else {
            // Start button
            LargeFloatingActionButton(
                onClick = onStart,
                shape = CircleShape,
                containerColor = if (hasPermission) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start",
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

private fun getActivityIcon(type: ActivityType): ImageVector {
    return when (type) {
        ActivityType.RUNNING -> Icons.AutoMirrored.Filled.DirectionsRun
        ActivityType.WALKING -> Icons.Default.DirectionsWalk
        ActivityType.CYCLING -> Icons.Default.DirectionsBike
    }
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

private fun formatPace(secondsPerKm: Double): String {
    if (secondsPerKm <= 0 || secondsPerKm.isNaN() || secondsPerKm.isInfinite()) {
        return "--:--"
    }
    val minutes = (secondsPerKm / 60).toInt()
    val seconds = (secondsPerKm % 60).toInt()
    return String.format("%d:%02d", minutes, seconds)
}
