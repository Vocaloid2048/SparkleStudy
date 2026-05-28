package com.voc2048.sparkle_study.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
fun MainScreen() {
    var selectedTab by remember { mutableStateOf<MainTab>(MainTab.Dashboard) }

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
                        onClick = { selectedTab = tab },
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
                MainTab.Dashboard -> DashboardScreen()
                MainTab.Timer -> FocusTimerScreen()
                MainTab.Settings -> SettingsScreen()
            }
        }
    }
}
