package com.alexander.pacenote

import app.cash.turbine.test
import com.alexander.pacenote.data.repository.DefaultSportsResultRepository
import com.alexander.pacenote.data.source.LocalResultDataSource
import com.alexander.pacenote.data.source.RemoteResultDataSource
import com.alexander.pacenote.domain.model.SportsResult
import com.alexander.pacenote.domain.model.SportsResultDraft
import com.alexander.pacenote.domain.model.StorageType
import com.alexander.pacenote.domain.util.IdGenerator
import com.alexander.pacenote.domain.util.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultSportsResultRepositoryTest {
    @Test
    fun `saving local creates the complete result and writes only locally`() = runTest {
        val local = RecordingLocalDataSource()
        val remote = RecordingRemoteDataSource()
        val repository = repository(local, remote)

        val saved = repository.save(draft(), StorageType.LOCAL)

        assertEquals(expected(StorageType.LOCAL), saved)
        assertEquals(listOf(saved), local.saved)
        assertTrue(remote.saved.isEmpty())
    }

    @Test
    fun `saving remote creates the complete result and writes only remotely`() = runTest {
        val local = RecordingLocalDataSource()
        val remote = RecordingRemoteDataSource()
        val repository = repository(local, remote)

        val saved = repository.save(draft(), StorageType.REMOTE)

        assertEquals(expected(StorageType.REMOTE), saved)
        assertEquals(listOf(saved), remote.saved)
        assertTrue(local.saved.isEmpty())
    }

    @Test
    fun `results from both sources are merged newest first`() = runTest {
        val local = RecordingLocalDataSource(listOf(result("local", 100, StorageType.LOCAL)))
        val remote = RecordingRemoteDataSource(
            listOf(
                result("remote-new", 300, StorageType.REMOTE),
                result("remote-old", 50, StorageType.REMOTE),
            ),
        )

        repository(local, remote).observeResults().test {
            assertEquals(listOf("remote-new", "local", "remote-old"), awaitItem().map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `equal timestamps are ordered by descending id`() = runTest {
        val local = RecordingLocalDataSource(listOf(result("alpha", 100, StorageType.LOCAL)))
        val remote = RecordingRemoteDataSource(listOf(result("zulu", 100, StorageType.REMOTE)))

        repository(local, remote).observeResults().test {
            assertEquals(listOf("zulu", "alpha"), awaitItem().map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observer receives subsequent source updates`() = runTest {
        val local = RecordingLocalDataSource()
        val remote = RecordingRemoteDataSource()

        repository(local, remote).observeResults().test {
            assertEquals(emptyList<SportsResult>(), awaitItem())
            local.emit(result("local", 10, StorageType.LOCAL))
            assertEquals(listOf("local"), awaitItem().map { it.id })
            remote.emit(result("remote", 20, StorageType.REMOTE))
            assertEquals(listOf("remote", "local"), awaitItem().map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh is delegated exactly once to remote source`() = runTest {
        val remote = RecordingRemoteDataSource()
        val repository = repository(RecordingLocalDataSource(), remote)

        repository.refreshRemote()

        assertEquals(1, remote.refreshCalls)
    }

    @Test
    fun `storage failures propagate without writing the other destination`() = runTest {
        val expected = IllegalStateException("remote unavailable")
        val local = RecordingLocalDataSource()
        val remote = RecordingRemoteDataSource(saveError = expected)
        val repository = repository(local, remote)
        var actual: Throwable? = null

        try {
            repository.save(draft(), StorageType.REMOTE)
        } catch (throwable: Throwable) {
            actual = throwable
        }

        assertSame(expected, actual)
        assertTrue(local.saved.isEmpty())
        assertTrue(remote.saved.isEmpty())
    }

    private fun repository(
        local: RecordingLocalDataSource,
        remote: RecordingRemoteDataSource,
    ) = DefaultSportsResultRepository(
        localDataSource = local,
        remoteDataSource = remote,
        idGenerator = IdGenerator { "result-42" },
        timeProvider = TimeProvider { 1_700_000_000_000L },
    )

    private fun draft() = SportsResultDraft("morning run", "stromovka", 45)

    private fun expected(storageType: StorageType) = SportsResult(
        id = "result-42",
        name = "morning run",
        location = "stromovka",
        durationMinutes = 45,
        createdAtEpochMillis = 1_700_000_000_000L,
        storageType = storageType,
    )

    private fun result(id: String, createdAt: Long, storageType: StorageType) = SportsResult(
        id = id,
        name = "workout $id",
        location = "prague",
        durationMinutes = 30,
        createdAtEpochMillis = createdAt,
        storageType = storageType,
    )
}

private class RecordingLocalDataSource(
    initial: List<SportsResult> = emptyList(),
) : LocalResultDataSource {
    private val results = MutableStateFlow(initial)
    val saved = mutableListOf<SportsResult>()

    override fun observeResults(): Flow<List<SportsResult>> = results

    override suspend fun save(result: SportsResult) {
        saved += result
        results.value += result
    }

    fun emit(result: SportsResult) {
        results.value += result
    }
}

private class RecordingRemoteDataSource(
    initial: List<SportsResult> = emptyList(),
    private val saveError: Throwable? = null,
) : RemoteResultDataSource {
    private val results = MutableStateFlow(initial)
    val saved = mutableListOf<SportsResult>()
    var refreshCalls = 0

    override fun observeResults(): Flow<List<SportsResult>> = results

    override suspend fun save(result: SportsResult) {
        saveError?.let { throw it }
        saved += result
        results.value += result
    }

    override suspend fun refresh() {
        refreshCalls += 1
    }

    fun emit(result: SportsResult) {
        results.value += result
    }
}
