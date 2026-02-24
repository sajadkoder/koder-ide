package com.koder.ide.domain.repository

import com.koder.ide.domain.model.*

interface GitRepository {
    suspend fun isRepository(path: String): Boolean
    suspend fun getStatus(path: String): GitStatus
    suspend fun getBranches(path: String): List<GitBranch>
    suspend fun getCurrentBranch(path: String): String
    suspend fun getCommits(path: String, limit: Int = 50): List<GitCommit>
    suspend fun init(path: String): GitResult
    suspend fun add(path: String, files: List<String>): GitResult
    suspend fun commit(path: String, message: String): GitResult
    suspend fun push(path: String): GitResult
    suspend fun pull(path: String): GitResult
    suspend fun checkout(path: String, branch: String): GitResult
    suspend fun createBranch(path: String, branch: String): GitResult
    suspend fun deleteBranch(path: String, branch: String): GitResult
    suspend fun getRemoteUrl(path: String): String?
    suspend fun clone(url: String, path: String): GitResult
    suspend fun getDiff(path: String, file: String? = null): String
    suspend fun getLog(path: String, limit: Int = 50): List<GitCommit>
}
