package com.alexander.pacenote

import com.alexander.pacenote.data.local.SportsResultEntity
import com.alexander.pacenote.data.local.toDomain
import com.alexander.pacenote.data.local.toEntity
import com.alexander.pacenote.domain.model.SportsResult
import com.alexander.pacenote.domain.model.StorageType
import org.junit.Assert.assertEquals
import org.junit.Test

class SportsResultEntityTest {
    @Test
    fun `entity maps every persisted field to domain`() {
        val entity = entity()

        assertEquals("room-1", entity.toDomain().id)
        assertEquals("morning run", entity.toDomain().name)
        assertEquals("stromovka", entity.toDomain().location)
        assertEquals(35, entity.toDomain().durationMinutes)
        assertEquals(123L, entity.toDomain().createdAtEpochMillis)
    }

    @Test
    fun `entity always maps to local storage`() {
        assertEquals(StorageType.LOCAL, entity().toDomain().storageType)
    }

    @Test
    fun `local domain result maps every persisted field to entity`() {
        assertEquals(entity(), domain(StorageType.LOCAL).toEntity())
    }

    @Test
    fun `remote domain result does not leak storage marker into room entity`() {
        assertEquals(entity(), domain(StorageType.REMOTE).toEntity())
    }

    @Test
    fun `entity domain round trip is lossless`() {
        val original = SportsResultEntity(
            id = "číslo-2",
            name = "večerní běh",
            location = "praha 7",
            durationMinutes = 1_440,
            createdAtEpochMillis = Long.MAX_VALUE,
        )

        assertEquals(original, original.toDomain().toEntity())
    }

    private fun entity() = SportsResultEntity(
        id = "room-1",
        name = "morning run",
        location = "stromovka",
        durationMinutes = 35,
        createdAtEpochMillis = 123,
    )

    private fun domain(storageType: StorageType) = SportsResult(
        id = "room-1",
        name = "morning run",
        location = "stromovka",
        durationMinutes = 35,
        createdAtEpochMillis = 123,
        storageType = storageType,
    )
}
