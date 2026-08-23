package com.alexander.pacenote

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class PaceNoteAppTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun applicationStartsOnOverviewWithTopLevelNavigation() {
        composeRule.onNodeWithText("overview").assertIsDisplayed()
        composeRule.onNodeWithText("home").assertExists()
        composeRule.onNodeWithText("new").assertExists()
    }

    @Test
    fun newDestinationOpensTheCreationFlow() {
        composeRule.onNodeWithText("new").performClick()

        composeRule.onNodeWithText("new result").assertIsDisplayed()
        composeRule.onNodeWithText("workout name").assertExists()
    }

    @Test
    fun topLevelNavigationCanReturnHome() {
        composeRule.onNodeWithText("new").performClick()
        composeRule.onNodeWithText("home").performClick()

        composeRule.onNodeWithText("overview").assertIsDisplayed()
    }

    @Test
    fun invalidFormSubmissionKeepsTheUserOnTheForm() {
        composeRule.onNodeWithText("new").performClick()
        composeRule.onNodeWithText("save result").performScrollTo().performClick()

        composeRule.onNodeWithText("enter a workout name").assertExists()
        composeRule.onNodeWithText("enter a location").assertExists()
        composeRule.onNodeWithText("use a whole number from 1 to 1440").assertExists()
        composeRule.onNodeWithText("new result").assertIsDisplayed()
    }

    @Test
    fun validLocalResultIsSavedAndShownOnOverview() {
        composeRule.onNodeWithText("new").performClick()
        composeRule.onNodeWithText("workout name").performTextInput("integration run")
        composeRule.onNodeWithText("location").performTextInput("prague")
        composeRule.onNodeWithText("duration").performTextInput("37")
        composeRule.onNodeWithText("save result").performScrollTo().performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("integration run")
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }
        composeRule.onNodeWithText("overview").assertIsDisplayed()
        composeRule.onNodeWithText("integration run").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("add result").assertExists()
    }
}
