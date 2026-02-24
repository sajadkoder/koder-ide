package com.koder.ide.domain.repository

import com.koder.ide.domain.model.ProjectFile
import kotlinx.coroutines.flow.Flow

interface FileRepository {
    suspend fun getFiles(path: String): List<ProjectFile>
    suspend fun readFile(path: String): String
    suspend fun writeFile(path: String, content: String): Boolean
    suspend fun createFile(path: String, name: String): ProjectFile?
    suspend fun createDirectory(path: String, name: String): ProjectFile?
    suspend fun delete(path: String): Boolean
    suspend fun rename(path: String, newName: String): Boolean
    suspend fun copy(source: String, destination: String): Boolean
    suspend fun move(source: String, destination: String): Boolean
    fun watchDirectory(path: String): Flow<List<ProjectFile>>
    fun getExternalStoragePath(): String
}
