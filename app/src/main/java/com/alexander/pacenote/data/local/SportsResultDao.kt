package com.alexander.pacenote.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SportsResultDao {
    @Query("SELECT * FROM sports_results ORDER BY createdAtEpochMillis DESC, id DESC")
    fun observeAll(): Flow<List<SportsResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: SportsResultEntity)
}
