package com.alexander.pacenote

import com.alexander.pacenote.data.remote.InMemoryRemoteResultDataSource
import com.alexander.pacenote.domain.model.SportsResult
import com.alexander.pacenote.domain.model.StorageType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryRemoteResultDataSourceTest {
    @Test
    fun `new source starts empty`() = runTest {
        val source = InMemoryRemoteResultDataSource(networkDelayMillis = 0)

        assertEquals(emptyList<SportsResult>(), source.observeResults().first())
    }

    @Test
    fun `saved result is emitted`() = runTest {
        val source = InMemoryRemoteResultDataSource(networkDelayMillis = 0)
        val result = result("remote", 10)

        source.save(result)

        assertEquals(listOf(result), source.observeResults().first())
    }

    @Test
    fun `results are sorted newest first`() = runTest {
        val source = InMemoryRemoteResultDataSource(networkDelayMillis = 0)

        source.save(result("old", 1))
        source.save(result("new", 3))
        source.save(result("middle", 2))

        assertEquals(listOf("new", "middle", "old"), source.observeResults().first().map { it.id })
    }

    @Test
    fun `saving the same id replaces the previous value`() = runTest {
        val source = InMemoryRemoteResultDataSource(networkDelayMillis = 0)

        source.save(result("same", 1, name = "first"))
        source.save(result("same", 2, name = "second"))

        val saved = source.observeResults().first()
        assertEquals(1, saved.size)
        assertEquals("second", saved.single().name)
        assertEquals(2L, saved.single().createdAtEpochMillis)
    }

    @Test
    fun `configured network delay applies to save and refresh`() = runTest {
        val source = InMemoryRemoteResultDataSource(networkDelayMillis = 350)
        val save = async { source.save(result("delayed", 1)) }
        runCurrent()

        assertFalse(save.isCompleted)
        advanceTimeBy(350)
        runCurrent()
        assertTrue(save.isCompleted)

        val refreshStarted = currentTime
        source.refresh()
        assertEquals(350L, currentTime - refreshStarted)
    }

    @Test
    fun `concurrent saves keep every unique result`() = runTest {
        val source = InMemoryRemoteResultDataSource(networkDelayMillis = 1)

        val writes = (1L..20L).map { index ->
            async { source.save(result("id-$index", index)) }
        }
        advanceUntilIdle()
        writes.awaitAll()

        val saved = source.observeResults().first()
        assertEquals(20, saved.size)
        assertEquals("id-20", saved.first().id)
        assertEquals("id-1", saved.last().id)
    }

    private fun result(id: String, createdAt: Long, name: String = "run") = SportsResult(
        id = id,
        name = name,
        location = "prague",
        durationMinutes = 30,
        createdAtEpochMillis = createdAt,
        storageType = StorageType.REMOTE,
    )
}
