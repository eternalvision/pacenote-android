package com.alexander.pacenote.data.repository

import com.alexander.pacenote.data.source.LocalResultDataSource
import com.alexander.pacenote.data.source.RemoteResultDataSource
import com.alexander.pacenote.domain.model.SportsResult
import com.alexander.pacenote.domain.model.SportsResultDraft
import com.alexander.pacenote.domain.model.StorageType
import com.alexander.pacenote.domain.repository.SportsResultRepository
import com.alexander.pacenote.domain.util.IdGenerator
import com.alexander.pacenote.domain.util.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class DefaultSportsResultRepository @Inject constructor(
    private val localDataSource: LocalResultDataSource,
    private val remoteDataSource: RemoteResultDataSource,
    private val idGenerator: IdGenerator,
    private val timeProvider: TimeProvider,
) : SportsResultRepository {
    override fun observeResults(): Flow<List<SportsResult>> = combine(
        localDataSource.observeResults(),
        remoteDataSource.observeResults(),
    ) { local, remote ->
        (local + remote).sortedWith(
            compareByDescending<SportsResult> { it.createdAtEpochMillis }
                .thenByDescending { it.id },
        )
    }

    override suspend fun save(
        draft: SportsResultDraft,
        storageType: StorageType,
    ): SportsResult {
        val result = SportsResult(
            id = idGenerator.generate(),
            name = draft.name,
            location = draft.location,
            durationMinutes = draft.durationMinutes,
            createdAtEpochMillis = timeProvider.nowEpochMillis(),
            storageType = storageType,
        )

        when (storageType) {
            StorageType.LOCAL -> localDataSource.save(result)
            StorageType.REMOTE -> remoteDataSource.save(result)
        }

        return result
    }

    override suspend fun refreshRemote() = remoteDataSource.refresh()
}
