package com.koder.ide.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GitStatus(
    val branch: String,
    val ahead: Int = 0,
    val behind: Int = 0,
    val staged: List<GitFile> = emptyList(),
    val unstaged: List<GitFile> = emptyList(),
    val untracked: List<GitFile> = emptyList(),
    val conflicts: List<GitFile> = emptyList()
) : Parcelable {
    val hasChanges: Boolean
        get() = staged.isNotEmpty() || unstaged.isNotEmpty() || untracked.isNotEmpty() || conflicts.isNotEmpty()
    
    val totalChanges: Int
        get() = staged.size + unstaged.size + untracked.size + conflicts.size
}

@Parcelize
data class GitFile(
    val path: String,
    val status: GitFileStatus
) : Parcelable

enum class GitFileStatus {
    ADDED,
    MODIFIED,
    DELETED,
    RENAMED,
    COPIED,
    UNTRACKED,
    CONFLICTED,
    IGNORED
}

@Parcelize
data class GitCommit(
    val id: String,
    val shortId: String,
    val message: String,
    val author: String,
    val authorEmail: String,
    val date: Long,
    val parentIds: List<String> = emptyList()
) : Parcelable

@Parcelize
data class GitBranch(
    val name: String,
    val isCurrent: Boolean = false,
    val isRemote: Boolean = false,
    val upstream: String? = null,
    val lastCommit: GitCommit? = null
) : Parcelable

@Parcelize
data class GitRemote(
    val name: String,
    val url: String,
    val pushUrl: String? = null
) : Parcelable

@Parcelize
data class GitLogEntry(
    val commit: GitCommit,
    val files: List<GitFile> = emptyList()
) : Parcelable
