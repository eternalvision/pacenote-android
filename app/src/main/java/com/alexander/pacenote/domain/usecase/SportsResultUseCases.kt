package com.alexander.pacenote.domain.usecase

import com.alexander.pacenote.domain.model.SportsResult
import com.alexander.pacenote.domain.model.SportsResultDraft
import com.alexander.pacenote.domain.model.StorageFilter
import com.alexander.pacenote.domain.model.StorageType
import com.alexander.pacenote.domain.repository.SportsResultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SaveSportsResultUseCase @Inject constructor(
    private val repository: SportsResultRepository,
) {
    suspend operator fun invoke(
        draft: SportsResultDraft,
        storageType: StorageType,
    ): SportsResult = repository.save(draft, storageType)
}

class ObserveSportsResultsUseCase @Inject constructor(
    private val repository: SportsResultRepository,
) {
    operator fun invoke(filter: StorageFilter): Flow<List<SportsResult>> =
        repository.observeResults().map { results ->
            when (filter) {
                StorageFilter.ALL -> results
                StorageFilter.LOCAL -> results.filter { it.storageType == StorageType.LOCAL }
                StorageFilter.REMOTE -> results.filter { it.storageType == StorageType.REMOTE }
            }
        }
}
