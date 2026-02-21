package com.koder.ide.data.local.database

import androidx.room.AutoMigration
import androidx.room.Database
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
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun recentFileDao(): RecentFileDao

    companion object {
        const val DATABASE_NAME = "koder_database"
    }
}
