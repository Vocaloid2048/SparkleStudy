package com.voc2048.sparkle_study.types

/**
 * 花卉種植系統相關的狀態與枚舉定義。
 */
enum class FlowerPeriod(val days: Int) {
    SHORT(3),      // 3 日週期
    MEDIUM(7),     // 7 日週期
    LONG(21)       // 21 日週期
}

enum class FlowerStatus {
    SEED,          // 種子狀態
    SPROUT,        // 萌芽狀態
    GROWING,       // 成長中
    BLOOMING,      // 盛開中
    DORMANT        // 休眠狀態 (若兩天未登錄則觸發)
}

data class Flower(
    val id: String,
    val name: String,
    val period: FlowerPeriod,
    val currentStatus: FlowerStatus,
    val growthProgress: Float,       // 成長進度 (0.0f 到 1.0f)
    val plantedTime: Long,           // 種植時間戳記
    val lastWateredTime: Long,       // 上次灌溉/施肥養分時間戳記
    val isCollected: Boolean         // 是否已收入圖鑑/展示牆
)
