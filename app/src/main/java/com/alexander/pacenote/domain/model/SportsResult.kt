package com.alexander.pacenote.domain.model

data class SportsResult(
    val id: String,
    val name: String,
    val location: String,
    val durationMinutes: Int,
    val createdAtEpochMillis: Long,
    val storageType: StorageType,
)

data class SportsResultDraft(
    val name: String,
    val location: String,
    val durationMinutes: Int,
)

enum class StorageType {
    LOCAL,
    REMOTE,
}

enum class StorageFilter {
    ALL,
    LOCAL,
    REMOTE,
}
