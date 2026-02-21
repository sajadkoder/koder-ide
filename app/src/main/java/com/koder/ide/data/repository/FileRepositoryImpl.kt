package com.koder.ide.data.repository

import android.content.Context
import com.koder.ide.domain.repository.FileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FileRepository {

    override suspend fun exists(path: String): Boolean {
        return withContext(Dispatchers.IO) { File(path).exists() }
    }

    override suspend fun isDirectory(path: String): Boolean {
        return withContext(Dispatchers.IO) { File(path).isDirectory }
    }

    override suspend fun isFile(path: String): Boolean {
        return withContext(Dispatchers.IO) { File(path).isFile }
    }

    override suspend fun canRead(path: String): Boolean {
        return withContext(Dispatchers.IO) { File(path).canRead() }
    }

    override suspend fun canWrite(path: String): Boolean {
        return withContext(Dispatchers.IO) { File(path).canWrite() }
    }

    override suspend fun createFile(path: String, content: String): Result<File> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(path)
                file.parentFile?.mkdirs()
                if (content.isNotEmpty()) {
                    file.writeText(content)
                } else {
                    file.createNewFile()
                }
                Result.success(file)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun createDirectory(path: String): Result<File> {
        return withContext(Dispatchers.IO) {
            try {
                val dir = File(path)
                if (dir.mkdirs()) {
                    Result.success(dir)
                } else {
                    Result.failure(Exception("Failed to create directory"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun delete(path: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(path)
                val success = if (file.isDirectory) file.deleteRecursively() else file.delete()
                if (success) Result.success(Unit) else Result.failure(Exception("Failed to delete"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun copy(source: String, destination: String): Result<File> {
        return withContext(Dispatchers.IO) {
            try {
                val srcFile = File(source)
                val destFile = File(destination)
                srcFile.copyRecursively(destFile, overwrite = true)
                Result.success(destFile)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun move(source: String, destination: String): Result<File> {
        return withContext(Dispatchers.IO) {
            try {
                val srcFile = File(source)
                val destFile = File(destination)
                if (srcFile.renameTo(destFile)) {
                    Result.success(destFile)
                } else {
                    Result.failure(Exception("Failed to move file"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun rename(path: String, newName: String): Result<File> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(path)
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

    override suspend fun readText(path: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(path)
                Result.success(file.readText())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun writeText(path: String, content: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(path)
                file.writeText(content)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun appendText(path: String, content: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(path)
                file.appendText(content)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun listFiles(path: String): Result<List<File>> {
        return withContext(Dispatchers.IO) {
            try {
                val dir = File(path)
                if (dir.isDirectory) {
                    Result.success(dir.listFiles()?.toList() ?: emptyList())
                } else {
                    Result.failure(Exception("Not a directory"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getFileSize(path: String): Long {
        return withContext(Dispatchers.IO) { File(path).length() }
    }

    override suspend fun getLastModified(path: String): Long {
        return withContext(Dispatchers.IO) { File(path).lastModified() }
    }

    override suspend fun getMimeType(path: String): String {
        return withContext(Dispatchers.IO) {
            val extension = File(path).extension.lowercase()
            when (extension) {
                "txt" -> "text/plain"
                "html", "htm" -> "text/html"
                "css" -> "text/css"
                "js" -> "application/javascript"
                "json" -> "application/json"
                "xml" -> "application/xml"
                "java" -> "text/x-java"
                "kt", "kts" -> "text/x-kotlin"
                "py" -> "text/x-python"
                "c" -> "text/x-c"
                "cpp", "h" -> "text/x-c++"
                "md" -> "text/markdown"
                "pdf" -> "application/pdf"
                "png" -> "image/png"
                "jpg", "jpeg" -> "image/jpeg"
                "gif" -> "image/gif"
                "svg" -> "image/svg+xml"
                "zip" -> "application/zip"
                else -> "application/octet-stream"
            }
        }
    }

    override suspend fun getFileExtension(path: String): String {
        return File(path).extension
    }

    override suspend fun getFileName(path: String): String {
        return File(path).name
    }

    override suspend fun getParentPath(path: String): String? {
        return File(path).parent
    }

    override suspend fun resolve(basePath: String, relativePath: String): String {
        return File(basePath).resolve(relativePath).absolutePath
    }

    override suspend fun isChildOf(parentPath: String, childPath: String): Boolean {
        val parent = File(parentPath).canonicalPath
        val child = File(childPath).canonicalPath
        return child.startsWith(parent)
    }
}
