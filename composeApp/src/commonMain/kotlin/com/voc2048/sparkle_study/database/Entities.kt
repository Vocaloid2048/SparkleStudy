package com.voc2048.sparkle_study.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val coins: Int,
    val nutrient: Int,
    val loginStreak: Int,
    val totalFocusMinutes: Long,
    val lastSyncAt: Long
)

@Entity(tableName = "inventory")
data class InventoryEntity(
    @PrimaryKey val itemId: String,
    val category: String,
    val isEquipped: Boolean,
    val unlockedAt: Long
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val remoteId: String? = null,
    val startTime: Long,
    val duration: Int,
    val isCompleted: Boolean,
    val verifiedPassed: Boolean,
    val isSynced: Boolean = false
)

@Entity(tableName = "plants")
data class PlantEntity(
    @PrimaryKey val plantId: String,
    val typeName: String,
    val status: String, // SEED, GROWING, BLOOMING, DORMANT
    val growthProgress: Float,
    val plantedAt: Long,
    val lastWatered: Long
)

@Entity(tableName = "daily_tasks")
data class DailyTaskEntity(
    @PrimaryKey val taskId: String,
    val taskType: String,
    val currentProgress: Int,
    val targetValue: Int,
    val isClaimed: Boolean,
    val dateKey: String // YYYY-MM-DD
)

@Entity(tableName = "coin_transactions")
data class CoinTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Int,
    val type: String,
    val description: String?,
    val referenceId: String?,
    val createdAt: Long,
    val isSynced: Boolean = false
)
