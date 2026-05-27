package com.voc2048.sparkle_study.types

/**
 * 匿名漂流瓶數據類，用於社交鼓勵。
 */
data class DriftBottle(
    val id: String,
    val senderId: String?,          // 匿名發送者 (可為 null，表示系統自動產生或完全匿名)
    val content: String,            // 漂流瓶鼓勵文字內容
    val sendTime: Long,             // 發送時間
    val isRead: Boolean,            // 是否已被接收者閱讀
    val isFlaggedByAI: Boolean,     // 是否被 AI 情感分析與敏感詞過濾機制標記
    val predefinedSentenceId: String? // 若用戶選擇的是標準句子，則記錄其 ID
)
