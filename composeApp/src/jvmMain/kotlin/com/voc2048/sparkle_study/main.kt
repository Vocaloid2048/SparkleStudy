package com.voc2048.sparkle_study

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SparkleStudy",
        state = WindowState(width = 420.dp, height = 960.dp)
    ) {
        App(ContextFactory())
    }
}