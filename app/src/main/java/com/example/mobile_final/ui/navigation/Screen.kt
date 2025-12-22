package com.example.mobile_final.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Tracking : Screen("tracking")
    data object History : Screen("history")
    data object Detail : Screen("detail/{activityId}") {
        fun createRoute(activityId: Long) = "detail/$activityId"
    }
    data object Stats : Screen("stats")
    data object Settings : Screen("settings")
}
