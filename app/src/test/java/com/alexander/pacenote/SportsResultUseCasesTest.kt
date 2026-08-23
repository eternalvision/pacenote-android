package com.alexander.pacenote

import app.cash.turbine.test
import com.alexander.pacenote.domain.model.SportsResult
import com.alexander.pacenote.domain.model.SportsResultDraft
import com.alexander.pacenote.domain.model.StorageFilter
import com.alexander.pacenote.domain.model.StorageType
import com.alexander.pacenote.domain.repository.SportsResultRepository
import com.alexander.pacenote.domain.usecase.ObserveSportsResultsUseCase
import com.alexander.pacenote.domain.usecase.SaveSportsResultUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class SportsResultUseCasesTest {
    @Test
    fun `save use case passes the exact draft and storage type`() = runTest {
        val repository = UseCaseRepository()
        val draft = SportsResultDraft("run", "prague", 45)

        SaveSportsResultUseCase(repository)(draft, StorageType.REMOTE)

        assertEquals(draft, repository.savedDraft)
        assertEquals(StorageType.REMOTE, repository.savedStorage)
    }

    @Test
    fun `save use case returns the repository result`() = runTest {
        val expected = result("saved", StorageType.LOCAL)
        val repository = UseCaseRepository(saveResult = expected)

        val actual = SaveSportsResultUseCase(repository)(
            SportsResultDraft("run", "prague", 45),
            StorageType.LOCAL,
        )

        assertSame(expected, actual)
    }

    @Test
    fun `all filter preserves repository order and members`() = runTest {
        val values = listOf(result("remote", StorageType.REMOTE), result("local", StorageType.LOCAL))

        ObserveSportsResultsUseCase(UseCaseRepository(values))(StorageFilter.ALL).test {
            assertEquals(values, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `local filter emits only local results`() = runTest {
        val repository = UseCaseRepository(
            listOf(result("local", StorageType.LOCAL), result("remote", StorageType.REMOTE)),
        )

        ObserveSportsResultsUseCase(repository)(StorageFilter.LOCAL).test {
            assertEquals(listOf("local"), awaitItem().map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `remote filter reacts to repository updates`() = runTest {
        val repository = UseCaseRepository()

        ObserveSportsResultsUseCase(repository)(StorageFilter.REMOTE).test {
            assertEquals(emptyList<SportsResult>(), awaitItem())
            repository.emit(
                listOf(result("local", StorageType.LOCAL), result("remote", StorageType.REMOTE)),
            )
            assertEquals(listOf("remote"), awaitItem().map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observation errors propagate to the collector`() = runTest {
        val expected = IllegalStateException("database unavailable")
        val repository = UseCaseRepository(observeError = expected)

        ObserveSportsResultsUseCase(repository)(StorageFilter.ALL).test {
            assertSame(expected, awaitError())
        }
    }

    private fun result(id: String, storageType: StorageType) = SportsResult(
        id = id,
        name = "run",
        location = "prague",
        durationMinutes = 30,
        createdAtEpochMillis = 100,
        storageType = storageType,
    )
}

private class UseCaseRepository(
    initial: List<SportsResult> = emptyList(),
    private val saveResult: SportsResult = SportsResult(
        id = "default",
        name = "run",
        location = "prague",
        durationMinutes = 30,
        createdAtEpochMillis = 100,
        storageType = StorageType.LOCAL,
    ),
    private val observeError: Throwable? = null,
) : SportsResultRepository {
    private val results = MutableStateFlow(initial)
    var savedDraft: SportsResultDraft? = null
    var savedStorage: StorageType? = null

    override fun observeResults(): Flow<List<SportsResult>> = observeError?.let { error ->
        flow { throw error }
    } ?: results

    override suspend fun save(
        draft: SportsResultDraft,
        storageType: StorageType,
    ): SportsResult {
        savedDraft = draft
        savedStorage = storageType
        return saveResult
    }

    override suspend fun refreshRemote() = Unit

    fun emit(value: List<SportsResult>) {
        results.value = value
    }
}
