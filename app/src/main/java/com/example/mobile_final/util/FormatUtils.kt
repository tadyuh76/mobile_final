package com.example.mobile_final.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object FormatUtils {

    fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }

    fun formatDurationLong(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes} min"
            else -> "${seconds} sec"
        }
    }

    fun formatDistance(meters: Double, useMetric: Boolean = true): String {
        return if (useMetric) {
            if (meters >= 1000) {
                String.format("%.2f km", meters / 1000)
            } else {
                String.format("%.0f m", meters)
            }
        } else {
            val miles = meters / 1609.344
            String.format("%.2f mi", miles)
        }
    }

    fun formatDistanceShort(meters: Double): String {
        return String.format("%.2f", meters / 1000)
    }

    fun formatPace(secondsPerKm: Double): String {
        if (secondsPerKm <= 0 || secondsPerKm.isNaN() || secondsPerKm.isInfinite()) {
            return "--:--"
        }
        val minutes = (secondsPerKm / 60).toInt()
        val seconds = (secondsPerKm % 60).toInt()
        return String.format("%d:%02d", minutes, seconds)
    }

    fun formatPaceWithUnit(secondsPerKm: Double, useMetric: Boolean = true): String {
        val pace = formatPace(secondsPerKm)
        return if (useMetric) "$pace /km" else "$pace /mi"
    }

    fun formatSpeed(metersPerSecond: Float): String {
        val kmPerHour = metersPerSecond * 3.6
        return String.format("%.1f km/h", kmPerHour)
    }

    fun formatCalories(calories: Int): String {
        return "$calories kcal"
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDayOfWeek(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatRelativeDate(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                "$minutes min ago"
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff)
                "$hours hours ago"
            }
            diff < TimeUnit.DAYS.toMillis(2) -> "Yesterday"
            diff < TimeUnit.DAYS.toMillis(7) -> formatDayOfWeek(timestamp)
            else -> formatDate(timestamp)
        }
    }

    fun getTimeOfDay(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH", Locale.getDefault())
        val hour = sdf.format(Date(timestamp)).toInt()

        return when {
            hour < 6 -> "Night"
            hour < 12 -> "Morning"
            hour < 17 -> "Afternoon"
            hour < 21 -> "Evening"
            else -> "Night"
        }
    }
}
