package com.voc2048.sparkle_study.types

enum class TaskType {
    FOCUS_TIME,      // 專注時數達標
    WATER_FLOWER,    // 灌溉花卉
    SEND_BOTTLE,     // 發送漂流瓶
    ENCOURAGE_OTHERS // 點讚他人
}

/**
 * 每日任務數據類。
 */
data class DailyTask(
    val id: String,
    val title: String,
    val type: TaskType,
    val targetValue: Int,       // 目標值 (如專注 60 分鐘)
    val currentValue: Int,      // 當前進度
    val coinReward: Int,        // 代幣獎勵
    val nutrientReward: Int,    // 養分獎勵
    val isCompleted: Boolean,
    val isClaimed: Boolean      // 是否已領取獎勵
)
