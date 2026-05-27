package com.voc2048.sparkle_study.types

/**
 * 專注時段數據類，包含番茄鐘配置與紀錄。
 */
data class FocusSession(
    val id: String,
    val userId: String,
    val focusDurationMinutes: Int,  // 專注時長 (分鐘)
    val breakDurationMinutes: Int,  // 休息時長 (分鐘)
    val startTime: Long,            // 開始時間戳記
    val endTime: Long?,             // 結束時間戳記 (null 表示未完成或中途放棄)
    val ambientSound: String?,      // 所選白噪音/環境音效名稱
    val isCompleted: Boolean,       // 是否成功完成
    val verifiedPassed: Boolean     // 是否通過隨機防作弊互動校驗
)
