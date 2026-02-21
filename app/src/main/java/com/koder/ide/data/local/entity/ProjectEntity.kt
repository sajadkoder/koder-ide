package com.koder.ide.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val path: String,
    val description: String = "",
    val language: String = "Unknown",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val lastOpenedAt: Long = 0,
    val openCount: Int = 0
)

@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long? = null,
    val path: String,
    val name: String,
    val language: String = "Plain Text",
    val lastOpenedAt: Long = System.currentTimeMillis(),
    val cursorPosition: Int = 0,
    val scrollPosition: Int = 0
)
