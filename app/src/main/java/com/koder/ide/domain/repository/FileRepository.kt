package com.koder.ide.domain.repository

import java.io.File

interface FileRepository {
    suspend fun exists(path: String): Boolean
    suspend fun isDirectory(path: String): Boolean
    suspend fun isFile(path: String): Boolean
    suspend fun canRead(path: String): Boolean
    suspend fun canWrite(path: String): Boolean
    suspend fun createFile(path: String, content: String = ""): Result<File>
    suspend fun createDirectory(path: String): Result<File>
    suspend fun delete(path: String): Result<Unit>
    suspend fun copy(source: String, destination: String): Result<File>
    suspend fun move(source: String, destination: String): Result<File>
    suspend fun rename(path: String, newName: String): Result<File>
    suspend fun readText(path: String): Result<String>
    suspend fun writeText(path: String, content: String): Result<Unit>
    suspend fun appendText(path: String, content: String): Result<Unit>
    suspend fun listFiles(path: String): Result<List<File>>
    suspend fun getFileSize(path: String): Long
    suspend fun getLastModified(path: String): Long
    suspend fun getMimeType(path: String): String
    suspend fun getFileExtension(path: String): String
    suspend fun getFileName(path: String): String
    suspend fun getParentPath(path: String): String?
    suspend fun resolve(basePath: String, relativePath: String): String
    suspend fun isChildOf(parentPath: String, childPath: String): Boolean
}
