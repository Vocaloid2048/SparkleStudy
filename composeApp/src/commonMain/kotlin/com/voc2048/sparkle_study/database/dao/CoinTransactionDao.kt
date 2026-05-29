package com.voc2048.sparkle_study.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.voc2048.sparkle_study.database.CoinTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoinTransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: CoinTransactionEntity)

    @Query("SELECT * FROM coin_transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<CoinTransactionEntity>>

    @Query("SELECT * FROM coin_transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<CoinTransactionEntity>
}
