package com.voc2048.sparkle_study.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.voc2048.sparkle_study.database.CoinTransactionEntity
import com.voc2048.sparkle_study.database.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    @Query("UPDATE users SET coins = coins + :amount WHERE id = :userId")
    suspend fun updateCoins(userId: String, amount: Int)

    @Transaction
    suspend fun earnCoins(userId: String, amount: Int, type: String, description: String?, refId: String?, transactionDao: CoinTransactionDao) {
        updateCoins(userId, amount)
        transactionDao.insertTransaction(
            CoinTransactionEntity(
                amount = amount,
                type = type,
                description = description,
                referenceId = refId,
                createdAt = 123456789L // Should use a proper timestamp provider
            )
        )
    }
}
