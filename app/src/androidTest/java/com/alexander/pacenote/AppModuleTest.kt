package com.alexander.pacenote

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alexander.pacenote.di.AppModule
import com.alexander.pacenote.domain.model.SportsResult
import com.alexander.pacenote.domain.model.StorageType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppModuleTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun databaseProviderCreatesAnOpenDatabase() {
        context.deleteDatabase("pace-note.db")
        val database = AppModule.provideDatabase(context)
        val writableDatabase = database.openHelper.writableDatabase

        assertTrue(writableDatabase.isOpen)

        database.close()
        context.deleteDatabase("pace-note.db")
    }

    @Test
    fun daoProviderReturnsTheDatabasesDao() {
        context.deleteDatabase("pace-note.db")
        val database = AppModule.provideDatabase(context)

        assertSame(database.sportsResultDao(), AppModule.provideSportsResultDao(database))

        database.close()
        context.deleteDatabase("pace-note.db")
    }

    @Test
    fun remoteProviderReturnsAWorkingCredentialFreeSource() = runTest {
        val remote = AppModule.provideRemoteDataSource()
        val result = SportsResult(
            id = "remote",
            name = "ride",
            location = "prague",
            durationMinutes = 50,
            createdAtEpochMillis = 100,
            storageType = StorageType.REMOTE,
        )

        assertTrue(remote.observeResults().first().isEmpty())
        remote.save(result)
        remote.refresh()

        assertEquals(result, remote.observeResults().first().single())
    }

    @Test
    fun idProviderGeneratesNonBlankUniqueValues() {
        val generator = AppModule.provideIdGenerator()
        val first = generator.generate()
        val second = generator.generate()

        assertFalse(first.isBlank())
        assertFalse(second.isBlank())
        assertNotEquals(first, second)
    }

    @Test
    fun timeProviderReturnsCurrentEpochWindow() {
        val before = System.currentTimeMillis()
        val actual = AppModule.provideTimeProvider().nowEpochMillis()
        val after = System.currentTimeMillis()

        assertTrue(actual in before..after)
    }
}
