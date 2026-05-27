package com.voc2048.sparkle_study.ui.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object FocusTimer : Screen("focus_timer")
    object Garden : Screen("garden")
    object AvatarCustomization : Screen("avatar_customization")
    object DriftBottle : Screen("drift_bottle")
    object StudyRoom : Screen("study_room")
    object Dashboard : Screen("dashboard")
    object Settings : Screen("settings")
    object Login : Screen("login")
}
