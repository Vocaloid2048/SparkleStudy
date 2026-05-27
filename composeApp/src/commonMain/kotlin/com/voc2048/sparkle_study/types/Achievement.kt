package com.voc2048.sparkle_study.types

/**
 * 成就系統數據類。
 */
data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val iconPath: String,
    val isUnlocked: Boolean,
    val unlockTime: Long?,
    val milestoneValue: Long,   // 解鎖所需的數值 (如累計 1000 分鐘)
    val currentProgress: Long   // 當前數值
)
