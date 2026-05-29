package com.voc2048.sparkle_study.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.voc2048.sparkle_study.database.InventoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory")
    fun getAllItems(): Flow<List<InventoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryEntity)

    @Query("UPDATE inventory SET isEquipped = 0 WHERE category = :category")
    suspend fun unequipAllInCategory(category: String)

    @Query("UPDATE inventory SET isEquipped = 1 WHERE itemId = :itemId")
    suspend fun equipItem(itemId: String)
}
