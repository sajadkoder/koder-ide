package com.koder.ide.data.repository

import android.os.Environment
import com.koder.ide.domain.model.ProjectFile
import com.koder.ide.domain.repository.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

import javax.inject.Inject

class FileRepositoryImpl @Inject constructor() : FileRepository {

    override suspend fun getFiles(path: String): List<ProjectFile> = withContext(Dispatchers.IO) {
        val directory = File(path)
        if (!directory.exists() || !directory.isDirectory) {
            return@withContext emptyList()
        }

        directory.listFiles()
            ?.filter { !it.name.startsWith(".") }
            ?.map { ProjectFile.fromFile(it) }
            ?.sortedWith(compareBy<ProjectFile> { !it.isDirectory }.thenBy { it.name.lowercase() })
            ?: emptyList()
    }

    override suspend fun readFile(path: String): String = withContext(Dispatchers.IO) {
        val file = File(path)
        if (file.exists() && file.isFile) {
            file.readText()
        } else {
            throw IllegalArgumentException("File does not exist: $path")
        }
    }

    override suspend fun writeFile(path: String, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun createFile(path: String, name: String): ProjectFile? = withContext(Dispatchers.IO) {
        try {
            val file = File(path, name)
            if (file.createNewFile()) {
                ProjectFile.fromFile(file)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createDirectory(path: String, name: String): ProjectFile? = withContext(Dispatchers.IO) {
        try {
            val dir = File(path, name)
            if (dir.mkdirs()) {
                ProjectFile.fromFile(dir)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun delete(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            File(path).deleteRecursively()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun rename(path: String, newName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            val newFile = File(file.parent, newName)
            file.renameTo(newFile)
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun copy(source: String, destination: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val srcFile = File(source)
            val destFile = File(destination)
            if (srcFile.isDirectory) {
                srcFile.copyRecursively(destFile, overwrite = true)
            } else {
                srcFile.copyTo(destFile, overwrite = true)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun move(source: String, destination: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val srcFile = File(source)
            val destFile = File(destination)
            srcFile.copyTo(destFile, overwrite = true)
            srcFile.deleteRecursively()
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun watchDirectory(path: String): Flow<List<ProjectFile>> = flow {
        while (true) {
            val files = getFiles(path)
            emit(files)
            kotlinx.coroutines.delay(2000)
        }
    }.flowOn(Dispatchers.IO)

    override fun getExternalStoragePath(): String {
        return Environment.getExternalStorageDirectory().absolutePath
    }
}
