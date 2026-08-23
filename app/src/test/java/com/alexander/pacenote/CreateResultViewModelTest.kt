package com.alexander.pacenote

import app.cash.turbine.test
import com.alexander.pacenote.domain.model.SportsResult
import com.alexander.pacenote.domain.model.SportsResultDraft
import com.alexander.pacenote.domain.model.StorageType
import com.alexander.pacenote.domain.repository.SportsResultRepository
import com.alexander.pacenote.domain.usecase.SaveSportsResultUseCase
import com.alexander.pacenote.domain.validation.SportsResultValidator
import com.alexander.pacenote.presentation.create.CreateResultEvent
import com.alexander.pacenote.presentation.create.CreateResultState
import com.alexander.pacenote.presentation.create.CreateResultViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateResultViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state is empty local and idle`() {
        val state = viewModel(FakeCreateRepository()).state.value

        assertEquals(CreateResultState(), state)
        assertEquals(StorageType.LOCAL, state.storageType)
        assertFalse(state.isSaving)
    }

    @Test
    fun `field edits update values and clear their validation errors`() {
        val viewModel = viewModel(FakeCreateRepository())
        viewModel.submit()

        viewModel.onNameChanged("run")
        viewModel.onLocationChanged("prague")
        viewModel.onDurationChanged("30")

        assertEquals("run", viewModel.state.value.name)
        assertEquals("prague", viewModel.state.value.location)
        assertEquals("30", viewModel.state.value.durationText)
        assertNull(viewModel.state.value.nameError)
        assertNull(viewModel.state.value.locationError)
        assertNull(viewModel.state.value.durationError)
    }

    @Test
    fun `storage selection updates destination and clears save error`() {
        val repository = FakeCreateRepository(saveError = IllegalStateException("offline"))
        val viewModel = validViewModel(repository)
        viewModel.submit()
        assertEquals("couldn't save the result. try again.", viewModel.state.value.errorMessage)

        viewModel.onStorageSelected(StorageType.REMOTE)

        assertEquals(StorageType.REMOTE, viewModel.state.value.storageType)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `invalid submission exposes every field error without saving`() {
        val repository = FakeCreateRepository()
        val viewModel = viewModel(repository)

        viewModel.onNameChanged(" ")
        viewModel.onLocationChanged("")
        viewModel.onDurationChanged("0")
        viewModel.submit()

        assertEquals(0, repository.saveCalls)
        assertEquals("enter a workout name", viewModel.state.value.nameError)
        assertEquals("enter a location", viewModel.state.value.locationError)
        assertEquals("use a whole number from 1 to 1440", viewModel.state.value.durationError)
        assertFalse(viewModel.state.value.isSaving)
    }

    @Test
    fun `valid submission normalizes data preserves destination and emits success`() = runTest {
        val repository = FakeCreateRepository()
        val viewModel = viewModel(repository)
        viewModel.onNameChanged("  evening swim ")
        viewModel.onLocationChanged(" podoli ")
        viewModel.onDurationChanged("60")
        viewModel.onStorageSelected(StorageType.REMOTE)

        viewModel.events.test {
            viewModel.submit()
            val event = awaitItem() as CreateResultEvent.Saved

            assertEquals(StorageType.REMOTE, event.result.storageType)
            assertEquals(
                SportsResultDraft("evening swim", "podoli", 60),
                repository.saved.single().first,
            )
            assertEquals(StorageType.REMOTE, repository.saved.single().second)
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(CreateResultState(storageType = StorageType.REMOTE), viewModel.state.value)
    }

    @Test
    fun `save failure keeps the form and exposes retryable error`() {
        val repository = FakeCreateRepository(saveError = IllegalStateException("offline"))
        val viewModel = validViewModel(repository)

        viewModel.submit()

        assertEquals("cycling", viewModel.state.value.name)
        assertEquals("brdy", viewModel.state.value.location)
        assertEquals("120", viewModel.state.value.durationText)
        assertEquals("couldn't save the result. try again.", viewModel.state.value.errorMessage)
        assertFalse(viewModel.state.value.isSaving)
        assertNull(viewModel.state.value.nameError)
    }

    @Test
    fun `second submission is ignored while save is running`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeCreateRepository(saveGate = gate)
        val viewModel = validViewModel(repository)

        viewModel.submit()
        assertTrue(viewModel.state.value.isSaving)
        viewModel.submit()

        assertEquals(1, repository.saveCalls)
        gate.complete(Unit)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isSaving)
    }

    private fun validViewModel(repository: FakeCreateRepository): CreateResultViewModel =
        viewModel(repository).apply {
            onNameChanged("cycling")
            onLocationChanged("brdy")
            onDurationChanged("120")
        }

    private fun viewModel(repository: SportsResultRepository) = CreateResultViewModel(
        saveSportsResult = SaveSportsResultUseCase(repository),
        validator = SportsResultValidator(),
    )
}

private class FakeCreateRepository(
    private val saveError: Throwable? = null,
    private val saveGate: CompletableDeferred<Unit>? = null,
) : SportsResultRepository {
    val saved = mutableListOf<Pair<SportsResultDraft, StorageType>>()
    var saveCalls = 0

    override fun observeResults(): Flow<List<SportsResult>> = flowOf(emptyList())

    override suspend fun save(
        draft: SportsResultDraft,
        storageType: StorageType,
    ): SportsResult {
        saveCalls += 1
        saveGate?.await()
        saveError?.let { throw it }
        saved += draft to storageType
        return SportsResult(
            id = "saved",
            name = draft.name,
            location = draft.location,
            durationMinutes = draft.durationMinutes,
            createdAtEpochMillis = 1,
            storageType = storageType,
        )
    }

    override suspend fun refreshRemote() = Unit
}
