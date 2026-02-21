package com.koder.ide.data.repository

import android.content.Context
import android.util.Log
import com.koder.ide.data.local.dao.ProjectDao
import com.koder.ide.data.local.dao.RecentFileDao
import com.koder.ide.data.local.entity.ProjectEntity
import com.koder.ide.data.local.entity.RecentFileEntity
import com.koder.ide.domain.model.FileNode
import com.koder.ide.domain.model.Project
import com.koder.ide.domain.model.RecentFile
import com.koder.ide.domain.model.SearchResult
import com.koder.ide.domain.repository.ProjectRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import javax.inject.Inject

class ProjectRepositoryImpl @Inject constructor(
    private val projectDao: ProjectDao,
    private val recentFileDao: RecentFileDao,
    @ApplicationContext private val context: Context
) : ProjectRepository {

    override suspend fun getProjects(): Flow<List<Project>> {
        return projectDao.getAllProjects().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getProject(id: Long): Project? {
        return projectDao.getProjectById(id)?.toDomain()
    }

    override suspend fun getProjectByPath(path: String): Project? {
        return projectDao.getProjectByPath(path)?.toDomain()
    }

    override suspend fun createProject(project: Project): Result<Project> {
        return try {
            val id = projectDao.insertProject(project.toEntity())
            Result.success(project.copy(id = id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProject(project: Project): Result<Unit> {
        return try {
            projectDao.updateProject(project.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProject(project: Project): Result<Unit> {
        return try {
            projectDao.deleteProject(project.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecentFiles(projectId: Long?): Flow<List<RecentFile>> {
        return (projectId?.let { recentFileDao.getRecentFilesByProject(it) }
            ?: recentFileDao.getAllRecentFiles()).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addRecentFile(file: RecentFile): Result<Unit> {
        return try {
            recentFileDao.upsertRecentFile(file.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRecentFile(file: RecentFile): Result<Unit> {
        return try {
            recentFileDao.updateRecentFile(file.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeRecentFile(file: RecentFile): Result<Unit> {
        return try {
            recentFileDao.deleteRecentFile(file.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearRecentFiles(): Result<Unit> {
        return try {
            recentFileDao.deleteAllRecentFiles()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFileTree(rootPath: String, maxDepth: Int): FileNode {
        return withContext(Dispatchers.IO) {
            buildFileTree(File(rootPath), 0, maxDepth)
        }
    }

    private fun buildFileTree(file: File, depth: Int, maxDepth: Int): FileNode {
        val node = FileNode(
            file = file,
            name = file.name,
            isDirectory = file.isDirectory,
            depth = depth
        )

        if (file.isDirectory && depth < maxDepth) {
            file.listFiles()
                ?.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() })
                ?.forEach { childFile ->
                    node.children.add(buildFileTree(childFile, depth + 1, maxDepth))
                }
        }

        return node
    }

    override suspend fun searchInProject(
        projectPath: String,
        query: String,
        fileExtensions: List<String>?,
        caseSensitive: Boolean,
        regex: Boolean,
        wholeWord: Boolean
    ): List<SearchResult> {
        return withContext(Dispatchers.IO) {
            val results = mutableListOf<SearchResult>()
            val projectDir = File(projectPath)

            if (!projectDir.exists() || !projectDir.isDirectory) {
                return@withContext emptyList()
            }

            val pattern = if (regex) {
                Regex(query, if (caseSensitive) emptySet() else setOf(RegexOption.IGNORE_CASE))
            } else {
                val escapedQuery = Regex.escape(query)
                val wordPattern = if (wholeWord) "\\b$escapedQuery\\b" else escapedQuery
                Regex(wordPattern, if (caseSensitive) emptySet() else setOf(RegexOption.IGNORE_CASE))
            }

            searchInDirectory(projectDir, pattern, fileExtensions, results)
            results
        }
    }

    private fun searchInDirectory(
        directory: File,
        pattern: Regex,
        fileExtensions: List<String>?,
        results: MutableList<SearchResult>
    ) {
        directory.listFiles()?.forEach { file ->
            when {
                file.isDirectory && !file.name.startsWith(".") -> {
                    searchInDirectory(file, pattern, fileExtensions, results)
                }
                file.isFile && !isBinaryFile(file) -> {
                    val extension = file.extension.lowercase()
                    if (fileExtensions == null || extension in fileExtensions.map { it.lowercase() }) {
                        searchInFile(file, pattern, results)
                    }
                }
            }
        }
    }

    private fun searchInFile(file: File, pattern: Regex, results: MutableList<SearchResult>) {
        try {
            BufferedReader(InputStreamReader(file.inputStream())).use { reader ->
                var line: String?
                var lineNumber = 0
                while (reader.readLine().also { line = it } != null) {
                    lineNumber++
                    line?.let { currentLine ->
                        pattern.findAll(currentLine).forEach { match ->
                            val startColumn = match.range.first
                            val contextStart = maxOf(0, startColumn - 30)
                            val contextEnd = minOf(currentLine.length, match.range.last + 30)
                            val contextBefore = currentLine.substring(contextStart, startColumn)
                            val contextAfter = currentLine.substring(
                                minOf(match.range.last + 1, currentLine.length),
                                contextEnd
                            )

                            results.add(
                                SearchResult(
                                    filePath = file.absolutePath,
                                    line = lineNumber,
                                    column = startColumn + 1,
                                    matchText = match.value,
                                    contextBefore = contextBefore,
                                    contextAfter = contextAfter
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ProjectRepository", "Error searching file: ${file.path}", e)
        }
    }

    private fun isBinaryFile(file: File): Boolean {
        val binaryExtensions = setOf(
            "png", "jpg", "jpeg", "gif", "bmp", "ico",
            "mp3", "mp4", "wav", "avi", "mkv",
            "zip", "tar", "gz", "rar", "7z",
            "pdf", "exe", "dll", "so", "dylib"
        )
        return file.extension.lowercase() in binaryExtensions
    }

    override suspend fun createFile(parent: File, name: String, isDirectory: Boolean): Result<File> {
        return withContext(Dispatchers.IO) {
            try {
                val newFile = File(parent, name)
                if (isDirectory) {
                    if (newFile.mkdirs()) Result.success(newFile)
                    else Result.failure(Exception("Failed to create directory"))
                } else {
                    if (newFile.createNewFile()) Result.success(newFile)
                    else Result.failure(Exception("Failed to create file"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteFile(file: File): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val success = if (file.isDirectory) {
                    file.deleteRecursively()
                } else {
                    file.delete()
                }
                if (success) Result.success(Unit)
                else Result.failure(Exception("Failed to delete: ${file.path}"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun copyFile(source: File, destination: File): Result<File> {
        return withContext(Dispatchers.IO) {
            try {
                source.copyRecursively(destination, overwrite = true)
                Result.success(destination)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun moveFile(source: File, destination: File): Result<File> {
        return withContext(Dispatchers.IO) {
            try {
                if (source.renameTo(destination)) {
                    Result.success(destination)
                } else {
                    Result.failure(Exception("Failed to move file"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun renameFile(file: File, newName: String): Result<File> {
        return withContext(Dispatchers.IO) {
            try {
                val newFile = File(file.parentFile, newName)
                if (file.renameTo(newFile)) {
                    Result.success(newFile)
                } else {
                    Result.failure(Exception("Failed to rename file"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

private fun ProjectEntity.toDomain() = Project(
    id = id,
    name = name,
    path = path,
    description = description,
    language = language,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isFavorite = isFavorite,
    lastOpenedAt = lastOpenedAt,
    openCount = openCount
)

private fun Project.toEntity() = ProjectEntity(
    id = id,
    name = name,
    path = path,
    description = description,
    language = language,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isFavorite = isFavorite,
    lastOpenedAt = lastOpenedAt,
    openCount = openCount
)

private fun RecentFileEntity.toDomain() = RecentFile(
    id = id,
    projectId = projectId,
    path = path,
    name = name,
    language = language,
    lastOpenedAt = lastOpenedAt,
    cursorPosition = cursorPosition,
    scrollPosition = scrollPosition
)

private fun RecentFile.toEntity() = RecentFileEntity(
    id = id,
    projectId = projectId,
    path = path,
    name = name,
    language = language,
    lastOpenedAt = lastOpenedAt,
    cursorPosition = cursorPosition,
    scrollPosition = scrollPosition
)
