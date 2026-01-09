package com.example.mobile_final.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

@Composable
fun SocialPostCard(
    activity: Activity,
    locationPoints: List<LocationPoint>,
    userDisplayName: String? = null,
    isOwnActivity: Boolean = true,
    onShareClick: (() -> Unit)? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Post Header
            PostHeader(
                activity = activity,
                userDisplayName = userDisplayName,
                isOwnActivity = isOwnActivity,
                onShareClick = onShareClick
            )

            // Stats Banner
            FeaturedStatsBanner(activity)

            Spacer(modifier = Modifier.height(12.dp))

            // Route Map
            RouteMapPreview(
                locationPoints = locationPoints,
                activityType = activity.type,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PostHeader(
    activity: Activity,
    userDisplayName: String?,
    isOwnActivity: Boolean,
    onShareClick: (() -> Unit)?
) {
    val timeOfDayColor = getTimeOfDayColor(activity.startTime)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Activity Avatar - icon based on activity type, color based on time of day
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = timeOfDayColor.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = getActivityIcon(activity.type),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = timeOfDayColor
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and User
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${FormatUtils.getTimeOfDay(activity.startTime)} ${activity.type.displayName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = userDisplayName ?: "You",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Share button (only for own activities)
        if (isOwnActivity && onShareClick != null) {
            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = if (activity.isPublic) Icons.Default.IosShare else Icons.Default.IosShare,
                    contentDescription = if (activity.isPublic) "Unshare" else "Share",
                    tint = if (activity.isPublic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FeaturedStatsBanner(activity: Activity) {
    val activityColor = getActivityTypeColor(activity.type)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = activityColor.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FeaturedStat(
                value = FormatUtils.formatDistance(activity.distanceMeters),
                label = "Distance",
                color = activityColor
            )

            Box(
                modifier = Modifier
                    .height(60.dp)
                    .width(1.dp)
                    .background(activityColor.copy(alpha = 0.3f))
            )

            FeaturedStat(
                value = FormatUtils.formatDuration(activity.durationSeconds),
                label = "Duration",
                color = activityColor
            )

            Box(
                modifier = Modifier
                    .height(60.dp)
                    .width(1.dp)
                    .background(activityColor.copy(alpha = 0.3f))
            )

            FeaturedStat(
                value = FormatUtils.formatPace(activity.avgPaceSecondsPerKm.toDouble()),
                label = "Pace",
                color = activityColor
            )
        }
    }
}

@Composable
private fun FeaturedStat(
    value: String,
    label: String,
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun RouteMapPreview(
    locationPoints: List<LocationPoint>,
    activityType: ActivityType,
    modifier: Modifier = Modifier
) {
    val mapViewportState = rememberMapViewportState()
    val polylineColor = getActivityTypeColor(activityType)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (locationPoints.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = getActivityIcon(activityType),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = polylineColor.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No route recorded",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                MapboxMap(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    mapViewportState = mapViewportState,
                    style = { MapStyle(style = "mapbox://styles/mapbox/outdoors-v12") }
                ) {
                    MapEffect(locationPoints) { mapView ->
                        if (locationPoints.isNotEmpty()) {
                            val minLat = locationPoints.minOf { it.latitude }
                            val maxLat = locationPoints.maxOf { it.latitude }
                            val minLng = locationPoints.minOf { it.longitude }
                            val maxLng = locationPoints.maxOf { it.longitude }

                            val centerLat = (minLat + maxLat) / 2
                            val centerLng = (minLng + maxLng) / 2

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
                                    .padding(EdgeInsets(20.0, 20.0, 20.0, 20.0))
                                    .build()
                            )
                        }
                    }

                    if (locationPoints.size >= 2) {
                        val points = locationPoints.map {
                            Point.fromLngLat(it.longitude, it.latitude)
                        }
                        PolylineAnnotation(
                            points = points
                        ) {
                            lineColor = polylineColor
                            lineWidth = 5.0
                        }
                    }
                }
            }
        }
    }
}

private fun getActivityIcon(type: ActivityType): ImageVector {
    return when (type) {
        ActivityType.RUNNING -> Icons.AutoMirrored.Filled.DirectionsRun
        ActivityType.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
        ActivityType.CYCLING -> Icons.AutoMirrored.Filled.DirectionsBike
    }
}

@Composable
private fun getActivityTypeColor(type: ActivityType): Color {
    return when (type) {
        ActivityType.RUNNING -> Color(0xFF4CAF50)    // Green for running
        ActivityType.WALKING -> Color(0xFF2196F3)    // Blue for walking
        ActivityType.CYCLING -> Color(0xFFFF9800)    // Orange for cycling
    }
}

private fun getTimeOfDayColor(timestamp: Long): Color {
    val calendar = java.util.Calendar.getInstance().apply {
        timeInMillis = timestamp
    }
    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)

    return when {
        hour < 6 -> Color(0xFF5E35B1)      // Night - Deep Purple
        hour < 12 -> Color(0xFFFFB300)     // Morning - Amber
        hour < 17 -> Color(0xFFFF6F00)     // Afternoon - Deep Orange
        hour < 21 -> Color(0xFFE91E63)     // Evening - Pink
        else -> Color(0xFF5E35B1)          // Night - Deep Purple
    }
}
