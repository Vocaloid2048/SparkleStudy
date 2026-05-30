package com.voc2048.sparkle_study.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voc2048.sparkle_study.ui.viewmodels.StudyViewModel
import com.voc2048.sparkle_study.ui.viewmodels.TimerSwitchBehavior
import com.voc2048.sparkle_study.utils.Preferences
import com.voc2048.sparkle_study.utils.SparkleColorScheme
import compose.icons.FeatherIcons
import compose.icons.feathericons.*

sealed class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object StudyRoom : MainTab("自修室", FeatherIcons.Users, FeatherIcons.Users)
    object Garden : MainTab("花園", FeatherIcons.Wind, FeatherIcons.Wind)
    object Dashboard : MainTab("儀表板", FeatherIcons.PieChart, FeatherIcons.PieChart)
    object Timer : MainTab("計時器", FeatherIcons.Clock, FeatherIcons.Clock)
    object Settings : MainTab("設定", FeatherIcons.Settings, FeatherIcons.Settings)
}

@Composable
fun MainScreen(viewModel: StudyViewModel = viewModel { StudyViewModel() }) {
    val prefs = remember { Preferences() }
    var selectedTab by remember { mutableStateOf<MainTab>(MainTab.Dashboard) }
    val isTimerRunning by viewModel.isRunning.collectAsState()
    
    var pendingTab by remember { mutableStateOf<MainTab?>(null) }
    var showNavConfirmDialog by remember { mutableStateOf(false) }

    // Init Global Settings
    LaunchedEffect(Unit) {
        TimerSettings.switchBehavior = TimerSwitchBehavior.valueOf(prefs.timerSwitchBehavior)
    }

    if (showNavConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showNavConfirmDialog = false },
            title = { Text("暫停計時？") },
            text = { Text("切換頁面將會暫停當前的計時。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.pauseTimer()
                    pendingTab?.let { selectedTab = it }
                    showNavConfirmDialog = false
                }) { Text("確認") }
            },
            dismissButton = {
                TextButton(onClick = { showNavConfirmDialog = false }) { Text("取消") }
            }
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SparkleColorScheme.background,
                tonalElevation = 8.dp
            ) {
                val tabs = listOf(
                    MainTab.StudyRoom,
                    MainTab.Garden,
                    MainTab.Dashboard,
                    MainTab.Timer,
                    MainTab.Settings
                )
                
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { 
                            if (isTimerRunning && selectedTab == MainTab.Timer && tab != MainTab.Timer) {
                                if (TimerSettings.switchBehavior == TimerSwitchBehavior.CONFIRM) {
                                    pendingTab = tab
                                    showNavConfirmDialog = true
                                } else {
                                    viewModel.pauseTimer()
                                    selectedTab = tab
                                }
                            } else {
                                selectedTab = tab 
                            }
                        },
                        icon = {
                            Icon(
                                if (selectedTab == tab) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SparkleColorScheme.primary,
                            selectedTextColor = SparkleColorScheme.primary,
                            unselectedIconColor = SparkleColorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = SparkleColorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            indicatorColor = SparkleColorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
            when (selectedTab) {
                MainTab.StudyRoom -> StudyRoomScreen()
                MainTab.Garden -> GardenScreen() 
                MainTab.Dashboard -> DashboardScreen(viewModel)
                MainTab.Timer -> FocusTimerScreen(viewModel)
                MainTab.Settings -> SettingsScreen()
            }
        }
    }
}
