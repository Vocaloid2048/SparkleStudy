package com.voc2048.sparkle_study.database

import androidx.room.Room
import androidx.room.RoomDatabase
import com.voc2048.sparkle_study.SparkleApp

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val appContext = SparkleApp.INSTANCE
    val dbFile = appContext.getDatabasePath("sparkle_study.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
