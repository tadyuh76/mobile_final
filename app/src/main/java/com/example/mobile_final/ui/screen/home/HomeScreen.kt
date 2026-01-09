package com.example.mobile_final.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile_final.data.local.entity.ActivityType
import com.example.mobile_final.service.TrackingState
import com.example.mobile_final.ui.component.SocialPostCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartTracking: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToDetail: (Long) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeTrackingState by viewModel.activeTrackingState.collectAsState()
    val activitiesWithLocations by viewModel.activitiesWithLocations.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    var activityToShare by remember { mutableStateOf<Pair<Long, Boolean>?>(null) }

    // Refresh stats when screen becomes visible (including when navigating back)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadStats()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Share confirmation dialog
    activityToShare?.let { (activityId, isCurrentlyPublic) ->
        ShareConfirmationDialog(
            isSharing = isCurrentlyPublic,
            onConfirm = {
                viewModel.toggleActivityPublic(activityId, !isCurrentlyPublic)
                activityToShare = null
            },
            onDismiss = { activityToShare = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("My Activities")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onStartTracking,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Start Activity"
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.loadStats() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Active Session Card (shown when tracking is in progress)
                if (activeTrackingState.isTracking) {
                    item {
                        AnimatedVisibility(
                            visible = activeTrackingState.isTracking,
                            enter = fadeIn() + slideInVertically(
                                initialOffsetY = { -40 },
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                            )
                        ) {
                            ActiveSessionCard(
                                trackingState = activeTrackingState,
                                onClick = onStartTracking,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                            )
                        }
                    }
                } else {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Empty state when no activities
                if (activitiesWithLocations.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Activities Yet",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Start your first activity to see it here!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Activity Feed
                items(
                    items = activitiesWithLocations,
                    key = { (activity, _) -> activity.id }
                ) { (activity, locationPoints) ->
                    SocialPostCard(
                        activity = activity,
                        locationPoints = locationPoints,
                        userDisplayName = null,
                        isOwnActivity = true,
                        showUserAvatar = false,
                        onShareClick = {
                            activityToShare = Pair(activity.id, activity.isPublic)
                        },
                        onClick = {
                            onNavigateToDetail(activity.id)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}


@Composable
private fun ActiveSessionCard(
    trackingState: TrackingState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulsing animation for the "LIVE" indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with LIVE indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = getActivityIcon(trackingState.activityType),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Active ${trackingState.activityType.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // LIVE indicator with pulse
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .alpha(pulseAlpha)
                            .background(
                                color = if (trackingState.isPaused)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = if (trackingState.isPaused) "PAUSED" else "LIVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (trackingState.isPaused)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timer display
            Text(
                text = formatDurationMillis(trackingState.durationMillis),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActiveSessionStat(
                    label = "Distance",
                    value = String.format("%.2f km", trackingState.distanceMeters / 1000)
                )
                ActiveSessionStat(
                    label = "Pace",
                    value = formatPace(trackingState.avgPace)
                )
                ActiveSessionStat(
                    label = "Steps",
                    value = "${trackingState.steps}"
                )
                ActiveSessionStat(
                    label = "Calories",
                    value = "${trackingState.calories}"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tap to view hint
            Text(
                text = "Tap to view",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun ActiveSessionStat(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

private fun getActivityIcon(type: ActivityType): ImageVector {
    return when (type) {
        ActivityType.RUNNING -> Icons.AutoMirrored.Filled.DirectionsRun
        ActivityType.WALKING -> Icons.Default.DirectionsWalk
        ActivityType.CYCLING -> Icons.Default.DirectionsBike
    }
}

private fun formatDurationMillis(millis: Long): String {
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
    return String.format("%d:%02d/km", minutes, seconds)
}

@Composable
private fun ShareConfirmationDialog(
    isSharing: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isSharing) "Unshare Activity" else "Share Activity") },
        text = {
            Text(
                if (isSharing) {
                    "Remove this activity from the public social feed? You can share it again later."
                } else {
                    "Share this activity to the public social feed? Other users will be able to see your route, stats, and display name."
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    if (isSharing) "Unshare" else "Share",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
