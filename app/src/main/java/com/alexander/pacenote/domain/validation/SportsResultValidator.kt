package com.alexander.pacenote.domain.validation

import javax.inject.Inject

data class SportsResultValidation(
    val normalizedName: String = "",
    val normalizedLocation: String = "",
    val durationMinutes: Int = 0,
    val nameError: String? = null,
    val locationError: String? = null,
    val durationError: String? = null,
) {
    val isValid: Boolean
        get() = nameError == null && locationError == null && durationError == null
}

class SportsResultValidator @Inject constructor() {
    fun validate(
        name: String,
        location: String,
        durationText: String,
    ): SportsResultValidation {
        val normalizedName = name.trim()
        val normalizedLocation = location.trim()
        val parsedDuration = durationText.trim().toIntOrNull()
        val durationMinutes = parsedDuration ?: 0

        return SportsResultValidation(
            normalizedName = normalizedName,
            normalizedLocation = normalizedLocation,
            durationMinutes = durationMinutes,
            nameError = if (normalizedName.isBlank()) "enter a workout name" else null,
            locationError = if (normalizedLocation.isBlank()) "enter a location" else null,
            durationError = if (
                parsedDuration != null && durationMinutes in MIN_DURATION_MINUTES..MAX_DURATION_MINUTES
            ) {
                null
            } else {
                "use a whole number from 1 to 1440"
            },
        )
    }

    private companion object {
        const val MIN_DURATION_MINUTES = 1
        const val MAX_DURATION_MINUTES = 24 * 60
    }
}
