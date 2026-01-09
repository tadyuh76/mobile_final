package com.example.mobile_final.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobile_final.data.local.entity.ActivityType
import com.example.mobile_final.domain.model.Activity
import com.example.mobile_final.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivityCard(
    activity: Activity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeOfDayColor = getTimeOfDayColor(activity.startTime)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Activity Type Icon - icon based on type, color based on time of day
            ActivityTypeIcon(
                activityType = activity.type,
                timeOfDayColor = timeOfDayColor,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Main Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title and Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = FormatUtils.getTimeOfDay(activity.startTime),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = timeOfDayColor
                        )
                        Text(
                            text = activity.type.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Sync status indicator
                        Icon(
                            imageVector = if (activity.isSynced) Icons.Default.CloudDone else Icons.Default.Cloud,
                            contentDescription = if (activity.isSynced) "Synced to cloud" else "Syncing...",
                            modifier = Modifier.size(12.dp),
                            tint = if (activity.isSynced)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Text(
                            text = FormatUtils.formatRelativeDate(activity.startTime),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Stats Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatChip(
                        icon = Icons.Default.Straighten,
                        value = FormatUtils.formatDistance(activity.distanceMeters)
                    )
                    StatChip(
                        icon = Icons.Default.Schedule,
                        value = FormatUtils.formatDurationLong(activity.durationSeconds)
                    )
                    StatChip(
                        icon = Icons.Default.LocalFireDepartment,
                        value = "${activity.caloriesBurned} kcal"
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityTypeIcon(
    activityType: ActivityType,
    timeOfDayColor: Color,
    modifier: Modifier = Modifier
) {
    val icon = when (activityType) {
        ActivityType.RUNNING -> Icons.AutoMirrored.Filled.DirectionsRun
        ActivityType.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
        ActivityType.CYCLING -> Icons.AutoMirrored.Filled.DirectionsBike
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = timeOfDayColor.copy(alpha = 0.15f)
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = activityType.displayName,
            modifier = Modifier
                .padding(10.dp)
                .size(28.dp),
            tint = timeOfDayColor
        )
    }
}

@Composable
private fun StatChip(
    icon: ImageVector,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun getTimeOfDayColor(timestamp: Long): Color {
    val sdf = SimpleDateFormat("HH", Locale.getDefault())
    val hour = sdf.format(Date(timestamp)).toInt()

    return when {
        hour < 6 -> Color(0xFF5E35B1)      // Night - Deep Purple
        hour < 12 -> Color(0xFFFFB300)    // Morning - Amber
        hour < 17 -> Color(0xFFFF6F00)    // Afternoon - Deep Orange
        hour < 21 -> Color(0xFFE91E63)    // Evening - Pink
        else -> Color(0xFF5E35B1)         // Night - Deep Purple
    }
}
