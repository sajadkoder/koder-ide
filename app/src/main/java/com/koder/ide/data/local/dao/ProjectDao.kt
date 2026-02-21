package com.koder.ide.data.local.dao

import androidx.room.*
import com.koder.ide.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY lastOpenedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Query("SELECT * FROM projects WHERE path = :path")
    suspend fun getProjectByPath(path: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects ORDER BY lastOpenedAt DESC LIMIT :limit")
    fun getRecentProjects(limit: Int = 10): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    @Query("UPDATE projects SET lastOpenedAt = :timestamp, openCount = openCount + 1 WHERE id = :id")
    suspend fun updateLastOpened(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE projects SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Transaction
    suspend fun upsertProject(project: ProjectEntity): Long {
        val existing = getProjectByPath(project.path)
        return if (existing != null) {
            updateProject(project.copy(id = existing.id))
            existing.id
        } else {
            insertProject(project)
        }
    }
}
