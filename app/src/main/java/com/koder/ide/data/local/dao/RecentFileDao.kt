package com.koder.ide.data.local.dao

import androidx.room.*
import com.koder.ide.data.local.entity.RecentFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentFileDao {
    @Query("SELECT * FROM recent_files ORDER BY lastOpenedAt DESC")
    fun getAllRecentFiles(): Flow<List<RecentFileEntity>>

    @Query("SELECT * FROM recent_files WHERE projectId = :projectId ORDER BY lastOpenedAt DESC")
    fun getRecentFilesByProject(projectId: Long): Flow<List<RecentFileEntity>>

    @Query("SELECT * FROM recent_files WHERE path = :path")
    suspend fun getRecentFileByPath(path: String): RecentFileEntity?

    @Query("SELECT * FROM recent_files ORDER BY lastOpenedAt DESC LIMIT :limit")
    fun getRecentFiles(limit: Int = 20): Flow<List<RecentFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentFile(file: RecentFileEntity): Long

    @Update
    suspend fun updateRecentFile(file: RecentFileEntity)

    @Delete
    suspend fun deleteRecentFile(file: RecentFileEntity)

    @Query("DELETE FROM recent_files WHERE path = :path")
    suspend fun deleteRecentFileByPath(path: String)

    @Query("DELETE FROM recent_files")
    suspend fun deleteAllRecentFiles()

    @Query("DELETE FROM recent_files WHERE projectId = :projectId")
    suspend fun deleteRecentFilesByProject(projectId: Long)

    @Transaction
    suspend fun upsertRecentFile(file: RecentFileEntity): Long {
        val existing = getRecentFileByPath(file.path)
        return if (existing != null) {
            updateRecentFile(file.copy(id = existing.id))
            existing.id
        } else {
            insertRecentFile(file)
        }
    }
}
