package com.alexander.pacenote

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.alexander.pacenote.domain.model.SportsResult
import com.alexander.pacenote.domain.model.StorageFilter
import com.alexander.pacenote.domain.model.StorageType
import com.alexander.pacenote.presentation.create.CreateResultScreen
import com.alexander.pacenote.presentation.create.CreateResultState
import com.alexander.pacenote.presentation.results.ResultsScreen
import com.alexander.pacenote.presentation.results.ResultsState
import com.alexander.pacenote.presentation.theme.PaceNoteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PaceNoteScreensTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun createScreenShowsRequiredFieldsAndStorageChoices() {
        setCreateContent(CreateResultState())

        composeRule.onNodeWithText("workout name").assertIsDisplayed()
        composeRule.onNodeWithText("location").assertIsDisplayed()
        composeRule.onNodeWithText("duration").assertIsDisplayed()
        composeRule.onNodeWithText("this device").assertIsDisplayed()
        composeRule.onNodeWithText("remote demo").assertIsDisplayed()
        composeRule.onNodeWithText("save result").assertExists()
    }

    @Test
    fun createScreenRendersEnteredValuesInFormAndPreview() {
        setCreateContent(
            CreateResultState(
                name = "hill repeats",
                location = "letná",
                durationText = "48",
            ),
        )

        composeRule.onAllNodesWithText("hill repeats").assertCountEquals(2)
        composeRule.onNodeWithText("letná").assertExists()
        composeRule.onAllNodesWithText("48").assertCountEquals(2)
        composeRule.onNodeWithText("local").assertExists()
    }

    @Test
    fun createScreenShowsEveryValidationAndSaveError() {
        setCreateContent(
            CreateResultState(
                nameError = "enter a workout name.",
                locationError = "enter a location.",
                durationError = "use whole minutes only.",
                errorMessage = "couldn't save the result. try again.",
            ),
        )

        composeRule.onNodeWithText("enter a workout name.").assertExists()
        composeRule.onNodeWithText("enter a location.").assertExists()
        composeRule.onNodeWithText("use whole minutes only.").assertExists()
        composeRule.onNodeWithText("couldn't save the result. try again.").assertExists()
    }

    @Test
    fun createScreenSendsRemoteStorageSelection() {
        var selected: StorageType? = null
        setCreateContent(
            state = CreateResultState(),
            onStorageSelected = { selected = it },
        )

        composeRule.onNodeWithText("remote demo").performClick()

        composeRule.runOnIdle { assertEquals(StorageType.REMOTE, selected) }
    }

    @Test
    fun createScreenSendsSubmitAction() {
        var submitted = false
        setCreateContent(
            state = CreateResultState(),
            onSubmit = { submitted = true },
        )

        composeRule.onNodeWithText("save result").performClick()

        composeRule.runOnIdle { assertTrue(submitted) }
    }

    @Test
    fun createScreenDisablesSubmitWhileSaving() {
        setCreateContent(CreateResultState(isSaving = true))

        composeRule.onNodeWithText("saving…").assertExists().assertIsNotEnabled()
    }

    @Test
    fun emptyResultsScreenOffersAPathToCreateTheFirstResult() {
        var added = false
        setResultsContent(
            state = ResultsState(isLoading = false),
            onAdd = { added = true },
        )

        composeRule.onNodeWithText("no results yet").assertIsDisplayed()
        composeRule.onNodeWithText("add your first session to start the overview.").assertExists()
        composeRule.onNodeWithText("add result").performClick()
        composeRule.runOnIdle { assertTrue(added) }
    }

    @Test
    fun filteredEmptyResultsExplainWhyNothingIsShown() {
        setResultsContent(
            ResultsState(
                filter = StorageFilter.REMOTE,
                isLoading = false,
            ),
        )

        composeRule.onNodeWithText("no results match this filter.").assertExists()
    }

    @Test
    fun loadingResultsScreenShowsProgressCopy() {
        setResultsContent(ResultsState(isLoading = true))

        composeRule.onNodeWithText("updating results").assertIsDisplayed()
    }

    @Test
    fun errorResultsScreenSendsRetryAction() {
        var retried = false
        setResultsContent(
            state = ResultsState(
                isLoading = false,
                errorMessage = "network failed",
            ),
            onRetry = { retried = true },
        )

        composeRule.onNodeWithText("couldn’t load results").assertIsDisplayed()
        composeRule.onNodeWithText("try again").performClick()
        composeRule.runOnIdle { assertTrue(retried) }
    }

    @Test
    fun resultCardsAndSummaryDistinguishBothStorageTypes() {
        setResultsContent(
            ResultsState(
                results = listOf(
                    result("local-1", "tempo run", 30, StorageType.LOCAL, 2_000),
                    result("remote-1", "pool session", 60, StorageType.REMOTE, 1_000),
                ),
                isLoading = false,
            ),
        )

        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("tempo run").assertIsDisplayed()
        composeRule.onNodeWithText("pool session").assertIsDisplayed()
        composeRule.onNodeWithText("90").assertExists()
        composeRule.onNodeWithText("02").assertExists()
        composeRule.onNodeWithText("1/1").assertExists()
        composeRule.onAllNodesWithText("local").assertCountEquals(2)
        composeRule.onAllNodesWithText("remote").assertCountEquals(2)
    }

    @Test
    fun resultsScreenSendsEveryFilterSelection() {
        val selected = mutableListOf<StorageFilter>()
        setResultsContent(
            state = ResultsState(isLoading = false),
            onFilterSelected = selected::add,
        )

        composeRule.onNodeWithText("all").performClick()
        composeRule.onNodeWithText("local").performClick()
        composeRule.onNodeWithText("remote").performClick()

        composeRule.runOnIdle {
            assertEquals(StorageFilter.entries, selected)
        }
    }

    @Test
    fun populatedResultsScreenSendsRefreshAndAddActions() {
        var refreshed = false
        var added = false
        setResultsContent(
            state = ResultsState(
                results = listOf(result("one", "steady run", 35, StorageType.LOCAL, 10)),
                isLoading = false,
            ),
            onRefresh = { refreshed = true },
            onAdd = { added = true },
        )

        composeRule.onNodeWithContentDescription("refresh").performClick()
        composeRule.onNodeWithContentDescription("add result").performClick()

        composeRule.runOnIdle {
            assertTrue(refreshed)
            assertTrue(added)
        }
    }

    private fun setCreateContent(
        state: CreateResultState,
        onStorageSelected: (StorageType) -> Unit = {},
        onSubmit: () -> Unit = {},
    ) {
        composeRule.setContent {
            PaceNoteTheme {
                CreateResultScreen(
                    state = state,
                    onNameChanged = {},
                    onLocationChanged = {},
                    onDurationChanged = {},
                    onStorageSelected = onStorageSelected,
                    onSubmit = onSubmit,
                )
            }
        }
    }

    private fun setResultsContent(
        state: ResultsState,
        onFilterSelected: (StorageFilter) -> Unit = {},
        onRefresh: () -> Unit = {},
        onRetry: () -> Unit = {},
        onAdd: () -> Unit = {},
    ) {
        composeRule.setContent {
            PaceNoteTheme {
                ResultsScreen(
                    state = state,
                    onFilterSelected = onFilterSelected,
                    onRefresh = onRefresh,
                    onRetry = onRetry,
                    onAdd = onAdd,
                )
            }
        }
    }

    private fun result(
        id: String,
        name: String,
        duration: Int,
        storageType: StorageType,
        createdAt: Long,
    ) = SportsResult(
        id = id,
        name = name,
        location = "prague",
        durationMinutes = duration,
        createdAtEpochMillis = createdAt,
        storageType = storageType,
    )
}
