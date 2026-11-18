package com.ruhraga.tracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ruhraga.tracker.ui.components.BottomNavTab
import com.ruhraga.tracker.ui.components.RuhRagaBottomBar
import com.ruhraga.tracker.ui.components.RuhRagaTopBar
import com.ruhraga.tracker.ui.screens.ActiveWorkoutTimerScreen
import com.ruhraga.tracker.ui.screens.AnalyticsHeatmapScreen
import com.ruhraga.tracker.ui.screens.IbadahTrackerScreen
import com.ruhraga.tracker.ui.screens.OnboardingScreen
import com.ruhraga.tracker.ui.screens.TodayOverviewScreen
import com.ruhraga.tracker.ui.screens.WorkoutLoggerScreen
import com.ruhraga.tracker.ui.state.RuhRagaViewModel

private const val ROUTE_ONBOARDING = "onboarding"
private const val ROUTE_ACTIVE_WORKOUT_TIMER = "active_workout_timer"

@Composable
fun RuhRagaApp(
    viewModel: RuhRagaViewModel = viewModel()
) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val startRoute = if (state.userProfile.isOnboardingCompleted) BottomNavTab.TODAY.route else ROUTE_ONBOARDING
    val currentRoute = navBackStackEntry?.destination?.route ?: startRoute

    val isFullscreenRoute = currentRoute == ROUTE_ACTIVE_WORKOUT_TIMER || currentRoute == ROUTE_ONBOARDING

    val topBarSubtitle = when (currentRoute) {
        BottomNavTab.IBADAH.route -> "Ibadah"
        BottomNavTab.WORKOUT.route -> "Workout"
        BottomNavTab.PROGRESS.route -> "Progress"
        else -> "Today"
    }

    Scaffold(
        topBar = {
            if (!isFullscreenRoute) {
                RuhRagaTopBar(
                    subtitle = topBarSubtitle,
                    streakDays = state.streakDays,
                    isDarkTheme = state.isDarkTheme,
                    onToggleTheme = viewModel::toggleTheme,
                    onProfileClick = { navController.navigate(ROUTE_ONBOARDING) }
                )
            }
        },
        bottomBar = {
            if (!isFullscreenRoute) {
                RuhRagaBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { tab ->
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = if (isFullscreenRoute) Modifier else Modifier.padding(innerPadding)
        ) {
            composable(ROUTE_ONBOARDING) {
                OnboardingScreen(
                    initialProfile = state.userProfile,
                    onSaveProfile = { name, age, weightKg, heightCm ->
                        viewModel.saveUserProfile(name, age, weightKg, heightCm)
                        navController.navigate(BottomNavTab.TODAY.route) {
                            popUpTo(ROUTE_ONBOARDING) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(BottomNavTab.TODAY.route) {
                val navigateToTab: (BottomNavTab) -> Unit = { tab ->
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                TodayOverviewScreen(
                    state = state,
                    onToggleReminder = viewModel::toggleReminder,
                    onToggleAdhanSound = viewModel::toggleAdhanSound,
                    onResumeWorkout = { navController.navigate(ROUTE_ACTIVE_WORKOUT_TIMER) },
                    onQuickLogShalat = viewModel::logNextPendingPrayer,
                    onQuickAddReps = { viewModel.adjustReps(20) },
                    onNavigateToIbadah = { navigateToTab(BottomNavTab.IBADAH) },
                    onNavigateToWorkout = { navigateToTab(BottomNavTab.WORKOUT) }
                )
            }

            composable(BottomNavTab.IBADAH.route) {
                IbadahTrackerScreen(
                    state = state,
                    onSelectTab = viewModel::selectIbadahTab,
                    onTogglePrayerCompleted = viewModel::togglePrayerCompleted,
                    onTogglePrayerJamaah = viewModel::togglePrayerJamaah,
                    onToggleSunnah = viewModel::toggleSunnahItem,
                    onToggleTilawahComplete = viewModel::toggleTilawahComplete,
                    onTapTasbih = viewModel::tapTasbih
                )
            }

            composable(BottomNavTab.WORKOUT.route) {
                WorkoutLoggerScreen(
                    state = state,
                    onSelectExercise = viewModel::selectExercise,
                    onAdjustReps = viewModel::adjustReps,
                    onLogSetAndStartRest = viewModel::logSetAndStartRest,
                    onSkipRest = viewModel::skipRestTimer,
                    onOpenFullscreenTimer = { navController.navigate(ROUTE_ACTIVE_WORKOUT_TIMER) },
                    onSelectCardioMode = viewModel::selectCardioMode,
                    onLogMetabolicFinisher = viewModel::logMetabolicFinisher,
                    onAddPlanItem = viewModel::addWorkoutPlanItem,
                    onRemovePlanItem = viewModel::removeWorkoutPlanItem,
                    onFinishPlanSetup = viewModel::finishWorkoutPlanSetup,
                    onReopenPlanSetup = viewModel::reopenWorkoutPlanSetup,
                    onUpdatePlanSets = viewModel::updatePlanItemSets,
                    onUpdatePlanReps = viewModel::updatePlanItemReps,
                    onTogglePlanCompleted = viewModel::togglePlanItemCompleted
                )
            }

            composable(BottomNavTab.PROGRESS.route) {
                AnalyticsHeatmapScreen(
                    state = state,
                    onSelectPeriod = viewModel::selectAnalyticsPeriod,
                    onSelectMatrixDay = viewModel::selectMatrixDay
                )
            }

            composable(ROUTE_ACTIVE_WORKOUT_TIMER) {
                ActiveWorkoutTimerScreen(
                    state = state,
                    onBack = { navController.popBackStack() },
                    onToggleTimer = viewModel::toggleRestTimer,
                    onAdjustRestSeconds = viewModel::adjustRestSeconds,
                    onAdjustReps = viewModel::adjustActiveTimerReps,
                    onCompleteRestAndNextSet = viewModel::completeRestAndNextSet,
                    onToggleAudioCue = viewModel::toggleAudioCue
                )
            }
        }
    }
}
