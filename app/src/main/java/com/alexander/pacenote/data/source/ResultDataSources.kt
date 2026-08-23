package com.alexander.pacenote.data.source

import com.alexander.pacenote.domain.model.SportsResult
import kotlinx.coroutines.flow.Flow

interface LocalResultDataSource {
    fun observeResults(): Flow<List<SportsResult>>
    suspend fun save(result: SportsResult)
}

interface RemoteResultDataSource {
    fun observeResults(): Flow<List<SportsResult>>
    suspend fun save(result: SportsResult)
    suspend fun refresh()
}
