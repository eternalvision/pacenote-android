package com.alexander.pacenote

import com.alexander.pacenote.domain.validation.SportsResultValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SportsResultValidatorTest {
    private val validator = SportsResultValidator()

    @Test
    fun `valid input is normalized and parsed`() {
        val result = validator.validate(
            name = "  morning run  ",
            location = "  stromovka  ",
            durationText = " 45 ",
        )

        assertTrue(result.isValid)
        assertEquals("morning run", result.normalizedName)
        assertEquals("stromovka", result.normalizedLocation)
        assertEquals(45, result.durationMinutes)
        assertNull(result.nameError)
        assertNull(result.locationError)
        assertNull(result.durationError)
    }

    @Test
    fun `blank name is rejected independently`() {
        val result = validator.validate(name = " \n ", location = "prague", durationText = "30")

        assertFalse(result.isValid)
        assertEquals("enter a workout name", result.nameError)
        assertNull(result.locationError)
        assertNull(result.durationError)
    }

    @Test
    fun `blank location is rejected independently`() {
        val result = validator.validate(name = "cycling", location = "\t", durationText = "30")

        assertFalse(result.isValid)
        assertNull(result.nameError)
        assertEquals("enter a location", result.locationError)
        assertNull(result.durationError)
    }

    @Test
    fun `non integer duration formats are rejected`() {
        listOf("", "abc", "1.5", "+", "20 min").forEach { value ->
            val result = validator.validate("cycling", "prague", value)

            assertFalse("expected '$value' to be rejected", result.isValid)
            assertEquals(0, result.durationMinutes)
            assertEquals("use a whole number from 1 to 1440", result.durationError)
        }
    }

    @Test
    fun `duration boundaries are accepted`() {
        listOf("1" to 1, "1440" to 1440).forEach { (value, expected) ->
            val result = validator.validate("cycling", "prague", value)

            assertTrue("expected '$value' to be accepted", result.isValid)
            assertEquals(expected, result.durationMinutes)
            assertNull(result.durationError)
        }
    }

    @Test
    fun `duration values outside boundaries are rejected`() {
        listOf("0", "-1", "1441", "999999999999999999999").forEach { value ->
            val result = validator.validate("cycling", "prague", value)

            assertFalse("expected '$value' to be rejected", result.isValid)
            assertEquals("use a whole number from 1 to 1440", result.durationError)
        }
    }
}
