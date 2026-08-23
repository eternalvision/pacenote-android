package com.alexander.pacenote.di

import android.content.Context
import androidx.room.Room
import com.alexander.pacenote.data.local.PaceNoteDatabase
import com.alexander.pacenote.data.local.RoomLocalResultDataSource
import com.alexander.pacenote.data.local.SportsResultDao
import com.alexander.pacenote.data.remote.InMemoryRemoteResultDataSource
import com.alexander.pacenote.data.repository.DefaultSportsResultRepository
import com.alexander.pacenote.data.source.LocalResultDataSource
import com.alexander.pacenote.data.source.RemoteResultDataSource
import com.alexander.pacenote.domain.repository.SportsResultRepository
import com.alexander.pacenote.domain.util.IdGenerator
import com.alexander.pacenote.domain.util.TimeProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.UUID
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindingsModule {
    @Binds
    @Singleton
    abstract fun bindLocalDataSource(
        implementation: RoomLocalResultDataSource,
    ): LocalResultDataSource

    @Binds
    @Singleton
    abstract fun bindRepository(
        implementation: DefaultSportsResultRepository,
    ): SportsResultRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PaceNoteDatabase =
        Room.databaseBuilder(
            context,
            PaceNoteDatabase::class.java,
            "pace-note.db",
        ).build()

    @Provides
    fun provideSportsResultDao(database: PaceNoteDatabase): SportsResultDao =
        database.sportsResultDao()

    @Provides
    @Singleton
    fun provideRemoteDataSource(): RemoteResultDataSource =
        InMemoryRemoteResultDataSource(networkDelayMillis = 350)

    @Provides
    fun provideIdGenerator(): IdGenerator = IdGenerator { UUID.randomUUID().toString() }

    @Provides
    fun provideTimeProvider(): TimeProvider = TimeProvider(System::currentTimeMillis)
}
