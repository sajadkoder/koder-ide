package com.koder.ide.domain.repository

import com.koder.ide.domain.model.FileNode
import com.koder.ide.domain.model.Project
import com.koder.ide.domain.model.RecentFile
import com.koder.ide.domain.model.SearchResult
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ProjectRepository {
    suspend fun getProjects(): Flow<List<Project>>
    suspend fun getProject(id: Long): Project?
    suspend fun getProjectByPath(path: String): Project?
    suspend fun createProject(project: Project): Result<Project>
    suspend fun updateProject(project: Project): Result<Unit>
    suspend fun deleteProject(project: Project): Result<Unit>
    
    suspend fun getRecentFiles(projectId: Long? = null): Flow<List<RecentFile>>
    suspend fun addRecentFile(file: RecentFile): Result<Unit>
    suspend fun updateRecentFile(file: RecentFile): Result<Unit>
    suspend fun removeRecentFile(file: RecentFile): Result<Unit>
    suspend fun clearRecentFiles(): Result<Unit>
    
    suspend fun getFileTree(rootPath: String, maxDepth: Int = Int.MAX_VALUE): FileNode
    suspend fun searchInProject(
        projectPath: String,
        query: String,
        fileExtensions: List<String>? = null,
        caseSensitive: Boolean = false,
        regex: Boolean = false,
        wholeWord: Boolean = false
    ): List<SearchResult>
    
    suspend fun createFile(parent: File, name: String, isDirectory: Boolean): Result<File>
    suspend fun deleteFile(file: File): Result<Unit>
    suspend fun copyFile(source: File, destination: File): Result<File>
    suspend fun moveFile(source: File, destination: File): Result<File>
    suspend fun renameFile(file: File, newName: String): Result<File>
}
