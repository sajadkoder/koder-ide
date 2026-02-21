package com.koder.ide.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.koder.ide.data.local.dao.ProjectDao
import com.koder.ide.data.local.dao.RecentFileDao
import com.koder.ide.data.local.database.AppDatabase
import com.koder.ide.data.repository.EditorRepositoryImpl
import com.koder.ide.data.repository.FileRepositoryImpl
import com.koder.ide.data.repository.ProjectRepositoryImpl
import com.koder.ide.data.repository.SettingsRepositoryImpl
import com.koder.ide.domain.repository.EditorRepository
import com.koder.ide.domain.repository.FileRepository
import com.koder.ide.domain.repository.ProjectRepository
import com.koder.ide.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("settings") }
        )
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideProjectDao(database: AppDatabase): ProjectDao {
        return database.projectDao()
    }

    @Provides
    fun provideRecentFileDao(database: AppDatabase): RecentFileDao {
        return database.recentFileDao()
    }

    @Provides
    @Singleton
    fun provideFileRepository(
        @ApplicationContext context: Context
    ): FileRepository {
        return FileRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideEditorRepository(
        @ApplicationContext context: Context
    ): EditorRepository {
        return EditorRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideProjectRepository(
        projectDao: ProjectDao,
        recentFileDao: RecentFileDao,
        @ApplicationContext context: Context
    ): ProjectRepository {
        return ProjectRepositoryImpl(projectDao, recentFileDao, context)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        dataStore: DataStore<Preferences>
    ): SettingsRepository {
        return SettingsRepositoryImpl(dataStore)
    }
}
