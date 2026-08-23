package com.alexander.pacenote.domain.repository

import com.alexander.pacenote.domain.model.SportsResult
import com.alexander.pacenote.domain.model.SportsResultDraft
import com.alexander.pacenote.domain.model.StorageType
import kotlinx.coroutines.flow.Flow

interface SportsResultRepository {
    fun observeResults(): Flow<List<SportsResult>>
    suspend fun save(draft: SportsResultDraft, storageType: StorageType): SportsResult
    suspend fun refreshRemote()
}
