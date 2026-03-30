package ovo.sypw.autoglm4android.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ovo.sypw.autoglm4android.data.local.preferences.SettingsPreferences
import ovo.sypw.autoglm4android.data.local.room.AppDatabase
import ovo.sypw.autoglm4android.data.local.room.TaskHistoryDao
import ovo.sypw.autoglm4android.data.repository.SettingsRepositoryImpl
import ovo.sypw.autoglm4android.data.repository.TaskRepositoryImpl
import ovo.sypw.autoglm4android.domain.repository.SettingsRepository
import ovo.sypw.autoglm4android.domain.repository.TaskRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideTaskHistoryDao(database: AppDatabase): TaskHistoryDao {
        return database.taskHistoryDao()
    }

    @Provides
    @Singleton
    fun provideSettingsPreferences(@ApplicationContext context: Context): SettingsPreferences {
        return SettingsPreferences(context)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        settingsPreferences: SettingsPreferences
    ): SettingsRepository {
        return SettingsRepositoryImpl(settingsPreferences)
    }

    @Provides
    @Singleton
    fun provideTaskRepository(
        taskHistoryDao: TaskHistoryDao
    ): TaskRepository {
        return TaskRepositoryImpl(taskHistoryDao)
    }
}
