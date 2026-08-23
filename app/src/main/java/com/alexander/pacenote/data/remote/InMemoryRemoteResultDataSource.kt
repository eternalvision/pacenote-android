package com.alexander.pacenote.data.remote

import com.alexander.pacenote.data.source.RemoteResultDataSource
import com.alexander.pacenote.domain.model.SportsResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryRemoteResultDataSource(
    private val networkDelayMillis: Long = 350,
) : RemoteResultDataSource {
    private val results = MutableStateFlow<List<SportsResult>>(emptyList())
    private val mutex = Mutex()

    override fun observeResults(): Flow<List<SportsResult>> = results

    override suspend fun save(result: SportsResult) {
        simulateNetwork()
        mutex.withLock {
            results.update { current ->
                (current.filterNot { it.id == result.id } + result)
                    .sortedByDescending { it.createdAtEpochMillis }
            }
        }
    }

    override suspend fun refresh() {
        simulateNetwork()
    }

    private suspend fun simulateNetwork() {
        if (networkDelayMillis > 0) delay(networkDelayMillis)
    }
}
