package com.alexander.pacenote

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alexander.pacenote.data.local.PaceNoteDatabase
import com.alexander.pacenote.data.local.RoomLocalResultDataSource
import com.alexander.pacenote.data.local.SportsResultDao
import com.alexander.pacenote.data.local.SportsResultEntity
import com.alexander.pacenote.domain.model.SportsResult
import com.alexander.pacenote.domain.model.StorageType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDatabaseTest {
    private lateinit var database: PaceNoteDatabase
    private lateinit var dao: SportsResultDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PaceNoteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.sportsResultDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun newDatabaseStartsEmpty() = runTest {
        assertTrue(dao.observeAll().first().isEmpty())
    }

    @Test
    fun insertedEntityIsObservedWithEveryField() = runTest {
        val entity = entity("result-1", "tempo", "letná", 42, 100)

        dao.insert(entity)

        assertEquals(entity, dao.observeAll().first().single())
    }

    @Test
    fun newerResultsAreReturnedFirst() = runTest {
        dao.insert(entity("older", "older", "a", 20, 100))
        dao.insert(entity("newer", "newer", "b", 30, 200))

        assertEquals(listOf("newer", "older"), dao.observeAll().first().map { it.id })
    }

    @Test
    fun equalTimestampsUseDescendingIdAsStableTieBreaker() = runTest {
        dao.insert(entity("a", "first", "a", 20, 100))
        dao.insert(entity("z", "second", "b", 30, 100))

        assertEquals(listOf("z", "a"), dao.observeAll().first().map { it.id })
    }

    @Test
    fun duplicateIdReplacesPreviousRecord() = runTest {
        dao.insert(entity("same", "before", "a", 20, 100))
        dao.insert(entity("same", "after", "b", 50, 200))

        val stored = dao.observeAll().first().single()
        assertEquals("after", stored.name)
        assertEquals(50, stored.durationMinutes)
        assertEquals(200, stored.createdAtEpochMillis)
    }

    @Test
    fun localDataSourcePersistsAndMapsDomainResult() = runTest {
        val source = RoomLocalResultDataSource(dao)
        val input = SportsResult(
            id = "domain",
            name = "long run",
            location = "stromovka",
            durationMinutes = 75,
            createdAtEpochMillis = 300,
            storageType = StorageType.REMOTE,
        )

        source.save(input)

        assertEquals(
            input.copy(storageType = StorageType.LOCAL),
            source.observeResults().first().single(),
        )
    }

    private fun entity(
        id: String,
        name: String,
        location: String,
        duration: Int,
        createdAt: Long,
    ) = SportsResultEntity(
        id = id,
        name = name,
        location = location,
        durationMinutes = duration,
        createdAtEpochMillis = createdAt,
    )
}
