package com.alexander.pacenote.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SportsResultEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class PaceNoteDatabase : RoomDatabase() {
    abstract fun sportsResultDao(): SportsResultDao
}
