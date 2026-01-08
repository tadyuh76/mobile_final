package com.example.mobile_final.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mobile_final.ui.screen.detail.DetailScreen
import com.example.mobile_final.ui.screen.history.HistoryScreen
import com.example.mobile_final.ui.screen.home.HomeScreen
import com.example.mobile_final.ui.screen.profile.ProfileScreen
import com.example.mobile_final.ui.screen.settings.SettingsScreen
import com.example.mobile_final.ui.screen.social.SocialScreen
import com.example.mobile_final.ui.screen.stats.StatsScreen
import com.example.mobile_final.ui.screen.tracking.TrackingScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onStartTracking = {
                    navController.navigate(Screen.Tracking.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToStats = {
                    navController.navigate(Screen.Stats.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToDetail = { activityId ->
                    navController.navigate(Screen.Detail.createRoute(activityId))
                }
            )
        }

        composable(Screen.Tracking.route) {
            TrackingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onActivitySaved = { activityId ->
                    navController.navigate(Screen.Detail.createRoute(activityId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(Screen.Social.route) {
            SocialScreen(
                onNavigateToDetail = { activityId ->
                    navController.navigate(Screen.Detail.createRoute(activityId))
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onActivityClick = { activityId ->
                    navController.navigate(Screen.Detail.createRoute(activityId))
                }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("activityId") {
                    type = NavType.LongType
                }
            )
        ) {
            DetailScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
