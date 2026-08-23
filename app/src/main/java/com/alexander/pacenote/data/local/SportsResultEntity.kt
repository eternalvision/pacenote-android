package com.alexander.pacenote.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alexander.pacenote.domain.model.SportsResult
import com.alexander.pacenote.domain.model.StorageType

@Entity(tableName = "sports_results")
data class SportsResultEntity(
    @PrimaryKey val id: String,
    val name: String,
    val location: String,
    val durationMinutes: Int,
    val createdAtEpochMillis: Long,
)

fun SportsResultEntity.toDomain(): SportsResult = SportsResult(
    id = id,
    name = name,
    location = location,
    durationMinutes = durationMinutes,
    createdAtEpochMillis = createdAtEpochMillis,
    storageType = StorageType.LOCAL,
)

fun SportsResult.toEntity(): SportsResultEntity = SportsResultEntity(
    id = id,
    name = name,
    location = location,
    durationMinutes = durationMinutes,
    createdAtEpochMillis = createdAtEpochMillis,
)
