package com.voc2048.sparkle_study.types

import kotlinx.serialization.Serializable

/**
 * 應用程式基本資訊。
 */
@Serializable
data class AppInfo(
    val version: String,
    val buildNumber: Int,
    val packageName: String,
    val installTime: Long
)
