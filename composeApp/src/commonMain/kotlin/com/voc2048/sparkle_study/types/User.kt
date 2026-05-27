package com.voc2048.sparkle_study.types

/**
 * 核心用戶數據類，用於記錄用戶帳號資訊與資產數據。
 */
data class User(
    val id: String,
    val username: String,
    val email: String,
    val coins: Int,               // 用於購買裝扮的代幣 (非課金)
    val nutrient: Int,            // 用於培養花卉的養分 (專注產出)
    val loginStreak: Int,         // 連續登入天數
    val lastLoginDate: Long,      // 上次登入時間戳記
    val totalFocusMinutes: Long   // 累計專注時間 (分鐘)
)
