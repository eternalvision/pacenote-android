package com.alexander.pacenote

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.alexander.pacenote.presentation.theme.BrandMark
import com.alexander.pacenote.presentation.theme.Midnight
import com.alexander.pacenote.presentation.theme.NoiseSurface
import com.alexander.pacenote.presentation.theme.PaceNoteBackdrop
import com.alexander.pacenote.presentation.theme.PaceNoteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class PaceNoteThemeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun brandMarkUsesLowercaseInitials() {
        composeRule.setContent { PaceNoteTheme { BrandMark() } }

        composeRule.onNodeWithText("pn").assertIsDisplayed()
    }

    @Test
    fun noiseSurfaceKeepsItsContentAccessible() {
        composeRule.setContent {
            PaceNoteTheme {
                NoiseSurface(modifier = Modifier.size(120.dp), seed = 7, noiseAlpha = 0.08f) {
                    Text("textured content")
                }
            }
        }

        composeRule.onNodeWithText("textured content").assertIsDisplayed()
    }

    @Test
    fun backdropKeepsItsContentAccessible() {
        composeRule.setContent {
            PaceNoteTheme {
                PaceNoteBackdrop { Text("backdrop content") }
            }
        }

        composeRule.onNodeWithText("backdrop content").assertIsDisplayed()
    }

    @Test
    fun staticThemeUsesMonochromeMidnightBackground() {
        var background: Color? = null
        composeRule.setContent {
            PaceNoteTheme(darkTheme = true, dynamicColor = false) {
                background = MaterialTheme.colorScheme.background
                Text("static theme")
            }
        }

        composeRule.onNodeWithText("static theme").assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(Midnight, background) }
    }

    @Test
    fun dynamicThemePathProvidesAUsableColorScheme() {
        var background: Color? = null
        composeRule.setContent {
            PaceNoteTheme(darkTheme = true, dynamicColor = true) {
                background = MaterialTheme.colorScheme.background
                Text("dynamic theme")
            }
        }

        composeRule.onNodeWithText("dynamic theme").assertIsDisplayed()
        composeRule.runOnIdle { assertNotNull(background) }
    }
}
