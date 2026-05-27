package com.voc2048.sparkle_study.types

import kotlinx.serialization.Serializable

/**
 * 設備硬體資訊數據類。
 */
@Serializable
data class DeviceInfo(
    val deviceModel: String = "Unknown",
    val deviceOSName: String = "Unknown",
    val deviceOSVersion: String = "Unknown",
)
