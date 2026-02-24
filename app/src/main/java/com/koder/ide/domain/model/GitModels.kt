package com.koder.ide.domain.model

data class GitStatus(
    val branch: String = "",
    val isRepository: Boolean = false,
    val stagedChanges: List<String> = emptyList(),
    val unstagedChanges: List<String> = emptyList(),
    val untrackedFiles: List<String> = emptyList(),
    val hasConflicts: Boolean = false
)

data class GitCommit(
    val hash: String,
    val shortHash: String,
    val message: String,
    val author: String,
    val date: Long
)

data class GitBranch(
    val name: String,
    val isCurrent: Boolean,
    val isRemote: Boolean = false
)

sealed class GitResult {
    data class Success(val message: String) : GitResult()
    data class Error(val message: String) : GitResult()
}
