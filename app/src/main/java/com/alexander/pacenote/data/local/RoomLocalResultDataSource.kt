package com.alexander.pacenote.data.local

import com.alexander.pacenote.data.source.LocalResultDataSource
import com.alexander.pacenote.domain.model.SportsResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomLocalResultDataSource @Inject constructor(
    private val dao: SportsResultDao,
) : LocalResultDataSource {
    override fun observeResults(): Flow<List<SportsResult>> =
        dao.observeAll().map { entities -> entities.map(SportsResultEntity::toDomain) }

    override suspend fun save(result: SportsResult) {
        dao.insert(result.toEntity())
    }
}
