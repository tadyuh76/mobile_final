package com.example.mobile_final.ui.screen.detail

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile_final.data.local.entity.ActivityType
import com.example.mobile_final.domain.model.Activity
import com.example.mobile_final.domain.model.LocationPoint
import com.example.mobile_final.util.FormatUtils
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.plugin.viewport.data.OverviewViewportStateOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                viewModel.deleteActivity()
                showDeleteDialog = false
                onNavigateBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    uiState.activity?.let {
                        Text("${it.type.displayName} Activity")
                    } ?: Text("Activity Detail")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            uiState.activity != null -> {
                DetailContent(
                    activity = uiState.activity!!,
                    locationPoints = uiState.locationPoints,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun DetailContent(
    activity: Activity,
    locationPoints: List<LocationPoint>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Map with route
        RouteMap(
            locationPoints = locationPoints,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        // Activity Info Header
        ActivityHeader(
            activity = activity,
            modifier = Modifier.padding(16.dp)
        )

        // Stats Grid
        StatsGrid(
            activity = activity,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun RouteMap(
    locationPoints: List<LocationPoint>,
    modifier: Modifier = Modifier
) {
    val mapViewportState = rememberMapViewportState()

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(0.dp)
    ) {
        if (locationPoints.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No route data available",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapViewportState = mapViewportState,
                style = { MapStyle(style = "mapbox://styles/mapbox/outdoors-v12") }
            ) {
                // Fit map to route bounds
                MapEffect(locationPoints) { mapView ->
                    if (locationPoints.isNotEmpty()) {
                        val points = locationPoints.map {
                            Point.fromLngLat(it.longitude, it.latitude)
                        }

                        // Calculate bounds
                        val minLat = locationPoints.minOf { it.latitude }
                        val maxLat = locationPoints.maxOf { it.latitude }
                        val minLng = locationPoints.minOf { it.longitude }
                        val maxLng = locationPoints.maxOf { it.longitude }

                        val centerLat = (minLat + maxLat) / 2
                        val centerLng = (minLng + maxLng) / 2

                        // Calculate zoom based on bounds
                        val latDiff = maxLat - minLat
                        val lngDiff = maxLng - minLng
                        val maxDiff = maxOf(latDiff, lngDiff)
                        val zoom = when {
                            maxDiff > 0.1 -> 11.0
                            maxDiff > 0.05 -> 12.0
                            maxDiff > 0.01 -> 14.0
                            maxDiff > 0.005 -> 15.0
                            else -> 16.0
                        }

                        mapViewportState.flyTo(
                            CameraOptions.Builder()
                                .center(Point.fromLngLat(centerLng, centerLat))
                                .zoom(zoom)
                                .padding(EdgeInsets(50.0, 50.0, 50.0, 50.0))
                                .build()
                        )
                    }
                }

                // Draw route polyline
                if (locationPoints.size >= 2) {
                    val points = locationPoints.map {
                        Point.fromLngLat(it.longitude, it.latitude)
                    }
                    PolylineAnnotation(
                        points = points
                    ) {
                        lineColor = androidx.compose.ui.graphics.Color(0xFF4CAF50)
                        lineWidth = 4.0
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityHeader(
    activity: Activity,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Activity Type Icon
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Icon(
                imageVector = getActivityIcon(activity.type),
                contentDescription = null,
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = "${FormatUtils.getTimeOfDay(activity.startTime)} ${activity.type.displayName}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = FormatUtils.formatDateTime(activity.startTime),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatsGrid(
    activity: Activity,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Distance & Duration
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = Icons.Default.Straighten,
                label = "Distance",
                value = FormatUtils.formatDistance(activity.distanceMeters),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.Schedule,
                label = "Duration",
                value = FormatUtils.formatDuration(activity.durationSeconds),
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Pace & Calories
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = Icons.Default.Speed,
                label = "Avg Pace",
                value = FormatUtils.formatPaceWithUnit(activity.avgPaceSecondsPerKm.toDouble()),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.LocalFireDepartment,
                label = "Calories",
                value = FormatUtils.formatCalories(activity.caloriesBurned),
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Steps (if available)
        if (activity.stepCount > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = Icons.Default.TrendingUp,
                    label = "Steps",
                    value = "${activity.stepCount}",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Activity") },
        text = { Text("Are you sure you want to delete this activity? This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getActivityIcon(type: ActivityType): ImageVector {
    return when (type) {
        ActivityType.RUNNING -> Icons.AutoMirrored.Filled.DirectionsRun
        ActivityType.WALKING -> Icons.Default.DirectionsWalk
        ActivityType.CYCLING -> Icons.Default.DirectionsBike
    }
}
