package com.voc2048.sparkle_study.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.voc2048.sparkle_study.database.dao.*

@Database(
    entities = [
        UserEntity::class,
        InventoryEntity::class,
        FocusSessionEntity::class,
        PlantEntity::class,
        DailyTaskEntity::class,
        CoinTransactionEntity::class
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun coinTransactionDao(): CoinTransactionDao
    abstract fun focusDao(): FocusDao
    abstract fun plantDao(): PlantDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun dailyTaskDao(): DailyTaskDao
}

// In KMP, we need a factory for RoomDatabase
interface AppDatabaseConstructor : androidx.room.RoomDatabaseConstructor<AppDatabase>
