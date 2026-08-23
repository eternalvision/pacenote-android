package com.alexander.pacenote

import com.alexander.pacenote.domain.model.SportsResult
import com.alexander.pacenote.domain.model.SportsResultDraft
import com.alexander.pacenote.domain.model.StorageFilter
import com.alexander.pacenote.domain.model.StorageType
import com.alexander.pacenote.domain.repository.SportsResultRepository
import com.alexander.pacenote.domain.usecase.ObserveSportsResultsUseCase
import com.alexander.pacenote.presentation.results.ResultsViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ResultsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial observation exposes all results and completes loading`() {
        val repository = FakeResultsRepository(
            listOf(result("local", StorageType.LOCAL), result("remote", StorageType.REMOTE)),
        )

        val state = viewModel(repository).state.value

        assertFalse(state.isLoading)
        assertEquals(StorageFilter.ALL, state.filter)
        assertEquals(listOf("local", "remote"), state.results.map { it.id })
        assertNull(state.errorMessage)
    }

    @Test
    fun `filter selection updates the visible result subset`() {
        val repository = FakeResultsRepository(
            listOf(result("local", StorageType.LOCAL), result("remote", StorageType.REMOTE)),
        )
        val viewModel = viewModel(repository)

        viewModel.onFilterSelected(StorageFilter.REMOTE)

        assertEquals(StorageFilter.REMOTE, viewModel.state.value.filter)
        assertEquals(listOf("remote"), viewModel.state.value.results.map { it.id })
    }

    @Test
    fun `selecting active filter avoids a redundant subscription`() {
        val repository = FakeResultsRepository(emptyList())
        val viewModel = viewModel(repository)
        assertEquals(1, repository.observeCalls)

        viewModel.onFilterSelected(StorageFilter.ALL)

        assertEquals(1, repository.observeCalls)
    }

    @Test
    fun `repository updates remain reactive under the selected filter`() {
        val repository = FakeResultsRepository(emptyList())
        val viewModel = viewModel(repository)
        viewModel.onFilterSelected(StorageFilter.LOCAL)

        repository.emit(
            listOf(result("remote", StorageType.REMOTE), result("local", StorageType.LOCAL)),
        )

        assertEquals(listOf("local"), viewModel.state.value.results.map { it.id })
    }

    @Test
    fun `observation failure stops loading and exposes recovery error`() {
        val viewModel = viewModel(
            FakeResultsRepository(emptyList(), observeError = IllegalStateException("room failed")),
        )

        assertFalse(viewModel.state.value.isLoading)
        assertEquals("couldn't load results.", viewModel.state.value.errorMessage)
        assertEquals(emptyList<SportsResult>(), viewModel.state.value.results)
    }

    @Test
    fun `refresh is single flight and clears its progress state`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeResultsRepository(emptyList(), refreshGate = gate)
        val viewModel = viewModel(repository)

        viewModel.refresh()
        assertTrue(viewModel.state.value.isRefreshing)
        viewModel.refresh()
        assertEquals(1, repository.refreshCalls)

        gate.complete(Unit)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isRefreshing)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `refresh failure keeps results and exposes lowercase error`() {
        val repository = FakeResultsRepository(
            listOf(result("kept", StorageType.LOCAL)),
            refreshError = IllegalStateException("offline"),
        )
        val viewModel = viewModel(repository)

        viewModel.refresh()

        assertFalse(viewModel.state.value.isRefreshing)
        assertEquals(listOf("kept"), viewModel.state.value.results.map { it.id })
        assertEquals("couldn't refresh remote results.", viewModel.state.value.errorMessage)
    }

    @Test
    fun `retry restarts observation and remote refresh`() {
        val repository = FakeResultsRepository(emptyList())
        val viewModel = viewModel(repository)

        viewModel.retry()

        assertEquals(2, repository.observeCalls)
        assertEquals(1, repository.refreshCalls)
    }

    private fun viewModel(repository: SportsResultRepository) = ResultsViewModel(
        observeSportsResults = ObserveSportsResultsUseCase(repository),
        repository = repository,
    )

    private fun result(id: String, storageType: StorageType) = SportsResult(
        id = id,
        name = "workout",
        location = "prague",
        durationMinutes = 30,
        createdAtEpochMillis = 1,
        storageType = storageType,
    )
}

private class FakeResultsRepository(
    initial: List<SportsResult>,
    private val observeError: Throwable? = null,
    private val refreshError: Throwable? = null,
    private val refreshGate: CompletableDeferred<Unit>? = null,
) : SportsResultRepository {
    private val results = MutableStateFlow(initial)
    var observeCalls = 0
    var refreshCalls = 0

    override fun observeResults(): Flow<List<SportsResult>> {
        observeCalls += 1
        return observeError?.let { error -> flow { throw error } } ?: results
    }

    override suspend fun save(
        draft: SportsResultDraft,
        storageType: StorageType,
    ): SportsResult = error("not used")

    override suspend fun refreshRemote() {
        refreshCalls += 1
        refreshGate?.await()
        refreshError?.let { throw it }
    }

    fun emit(value: List<SportsResult>) {
        results.value = value
    }
}
