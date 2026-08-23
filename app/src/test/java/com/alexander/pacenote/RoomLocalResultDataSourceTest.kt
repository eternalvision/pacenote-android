package com.alexander.pacenote

import com.alexander.pacenote.data.local.RoomLocalResultDataSource
import com.alexander.pacenote.data.local.SportsResultDao
import com.alexander.pacenote.data.local.SportsResultEntity
import com.alexander.pacenote.domain.model.SportsResult
import com.alexander.pacenote.domain.model.StorageType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class RoomLocalResultDataSourceTest {
    @Test
    fun `empty dao produces an empty domain list`() = runTest {
        val source = RoomLocalResultDataSource(FakeSportsResultDao())

        assertEquals(emptyList<SportsResult>(), source.observeResults().first())
    }

    @Test
    fun `dao entities are converted to local domain results`() = runTest {
        val dao = FakeSportsResultDao(listOf(entity("one", 10)))
        val result = RoomLocalResultDataSource(dao).observeResults().first().single()

        assertEquals("one", result.id)
        assertEquals(StorageType.LOCAL, result.storageType)
        assertEquals(10L, result.createdAtEpochMillis)
    }

    @Test
    fun `subsequent dao emissions remain observable`() = runTest {
        val dao = FakeSportsResultDao()
        val source = RoomLocalResultDataSource(dao)

        dao.emit(listOf(entity("later", 20)))

        assertEquals(listOf("later"), source.observeResults().first().map(SportsResult::id))
    }

    @Test
    fun `dao ordering is preserved by the adapter`() = runTest {
        val dao = FakeSportsResultDao(
            listOf(entity("newest", 30), entity("middle", 20), entity("oldest", 10)),
        )

        val ids = RoomLocalResultDataSource(dao).observeResults().first().map(SportsResult::id)

        assertEquals(listOf("newest", "middle", "oldest"), ids)
    }

    @Test
    fun `saving converts the domain result before inserting`() = runTest {
        val dao = FakeSportsResultDao()
        val source = RoomLocalResultDataSource(dao)

        source.save(domain("saved", StorageType.LOCAL))

        assertEquals(entity("saved", 100), dao.inserted.single())
    }

    @Test
    fun `dao insertion failures are propagated`() = runTest {
        val expected = IllegalStateException("disk full")
        val source = RoomLocalResultDataSource(FakeSportsResultDao(insertError = expected))
        var actual: Throwable? = null

        try {
            source.save(domain("failed", StorageType.LOCAL))
        } catch (throwable: Throwable) {
            actual = throwable
        }

        assertSame(expected, actual)
    }

    private fun entity(id: String, createdAt: Long) = SportsResultEntity(
        id = id,
        name = "run $id",
        location = "prague",
        durationMinutes = 40,
        createdAtEpochMillis = createdAt,
    )

    private fun domain(id: String, storageType: StorageType) = SportsResult(
        id = id,
        name = "run $id",
        location = "prague",
        durationMinutes = 40,
        createdAtEpochMillis = 100,
        storageType = storageType,
    )
}

private class FakeSportsResultDao(
    initial: List<SportsResultEntity> = emptyList(),
    private val insertError: Throwable? = null,
) : SportsResultDao {
    private val entities = MutableStateFlow(initial)
    val inserted = mutableListOf<SportsResultEntity>()

    override fun observeAll(): Flow<List<SportsResultEntity>> = entities

    override suspend fun insert(result: SportsResultEntity) {
        insertError?.let { throw it }
        inserted += result
        entities.value = entities.value.filterNot { it.id == result.id } + result
    }

    fun emit(value: List<SportsResultEntity>) {
        entities.value = value
    }
}
