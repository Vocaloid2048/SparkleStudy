package com.voc2048.sparkle_study

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "SparkleStudy",
    ) {
        App()
    }
}