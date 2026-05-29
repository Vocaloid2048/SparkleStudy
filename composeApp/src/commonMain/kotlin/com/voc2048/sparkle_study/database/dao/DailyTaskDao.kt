package com.voc2048.sparkle_study.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.voc2048.sparkle_study.database.DailyTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyTaskDao {
    @Query("SELECT * FROM daily_tasks WHERE dateKey = :dateKey")
    fun getTasksForDate(dateKey: String): Flow<List<DailyTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTask(task: DailyTaskEntity)

    @Query("UPDATE daily_tasks SET currentProgress = :progress WHERE taskId = :taskId")
    suspend fun updateProgress(taskId: String, progress: Int)
}
