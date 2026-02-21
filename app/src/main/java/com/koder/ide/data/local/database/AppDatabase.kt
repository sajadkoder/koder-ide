package com.koder.ide.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.koder.ide.data.local.dao.ProjectDao
import com.koder.ide.data.local.dao.RecentFileDao
import com.koder.ide.data.local.entity.ProjectEntity
import com.koder.ide.data.local.entity.RecentFileEntity

@Database(
    entities = [
        ProjectEntity::class,
        RecentFileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun recentFileDao(): RecentFileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build().also { INSTANCE = it }
            }
        }

        const val DATABASE_NAME = "koder_database"
    }
}
