package com.koder.ide.data.repository

import com.koder.ide.domain.model.*
import com.koder.ide.domain.repository.GitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import java.io.File

import javax.inject.Inject

class GitRepositoryImpl @Inject constructor() : GitRepository {

    override suspend fun isRepository(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Git.open(File(path)).use { true }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getStatus(path: String): GitStatus = withContext(Dispatchers.IO) {
        try {
            Git.open(File(path)).use { git ->
                val status = git.status().call()
                GitStatus(
                    branch = git.repository.branch,
                    isRepository = true,
                    stagedChanges = status.modified.toList() + status.added.toList(),
                    unstagedChanges = status.changed.toList() + status.missing.toList(),
                    untrackedFiles = status.untracked.toList(),
                    hasConflicts = false
                )
            }
        } catch (e: Exception) {
            GitStatus(isRepository = false)
        }
    }

    override suspend fun getBranches(path: String): List<GitBranch> = withContext(Dispatchers.IO) {
        try {
            Git.open(File(path)).use { git ->
                val branches = mutableListOf<GitBranch>()
                val currentBranch = git.repository.branch
                
                git.branchList().call().forEach { ref ->
                    val name = ref.name.removePrefix("refs/heads/")
                    branches.add(GitBranch(
                        name = name,
                        isCurrent = name == currentBranch
                    ))
                }
                branches
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getCurrentBranch(path: String): String = withContext(Dispatchers.IO) {
        try {
            Git.open(File(path)).use { it.repository.branch }
        } catch (e: Exception) {
            ""
        }
    }

    override suspend fun getCommits(path: String, limit: Int): List<GitCommit> = withContext(Dispatchers.IO) {
        try {
            Git.open(File(path)).use { git ->
                val commits = mutableListOf<GitCommit>()
                git.log().setMaxCount(limit).call().forEach { commit ->
                    commits.add(GitCommit(
                        hash = commit.name,
                        shortHash = commit.name.take(7),
                        message = commit.fullMessage,
                        author = commit.authorIdent.name,
                        date = commit.commitTime.toLong() * 1000
                    ))
                }
                commits
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun init(path: String): GitResult = withContext(Dispatchers.IO) {
        try {
            Git.init().setDirectory(File(path)).call().close()
            GitResult.Success("Repository initialized")
        } catch (e: Exception) {
            GitResult.Error(e.message ?: "Failed to initialize repository")
        }
    }

    override suspend fun add(path: String, files: List<String>): GitResult = withContext(Dispatchers.IO) {
        try {
            Git.open(File(path)).use { git ->
                val addCmd = git.add()
                if (files.isEmpty()) {
                    addCmd.addFilepattern(".").call()
                } else {
                    files.forEach { addCmd.addFilepattern(it).call() }
                }
                GitResult.Success("Files staged")
            }
        } catch (e: Exception) {
            GitResult.Error(e.message ?: "Failed to stage files")
        }
    }

    override suspend fun commit(path: String, message: String): GitResult = withContext(Dispatchers.IO) {
        try {
            Git.open(File(path)).use { git ->
                git.commit().setMessage(message).call()
                GitResult.Success("Committed successfully")
            }
        } catch (e: Exception) {
            GitResult.Error(e.message ?: "Failed to commit")
        }
    }

    override suspend fun push(path: String): GitResult = withContext(Dispatchers.IO) {
        try {
            Git.open(File(path)).use { git ->
                git.push().call()
                GitResult.Success("Pushed successfully")
            }
        } catch (e: Exception) {
            GitResult.Error(e.message ?: "Failed to push")
        }
    }

    override suspend fun pull(path: String): GitResult = withContext(Dispatchers.IO) {
        try {
            Git.open(File(path)).use { git ->
                git.pull().call()
                GitResult.Success("Pulled successfully")
            }
        } catch (e: Exception) {
            GitResult.Error(e.message ?: "Failed to pull")
        }
    }

    override suspend fun checkout(path: String, branch: String): GitResult = withContext(Dispatchers.IO) {
        try {
            Git.open(File(path)).use { git ->
                git.checkout().setName(branch).call()
                GitResult.Success("Switched to branch: $branch")
            }
        } catch (e: Exception) {
            GitResult.Error(e.message ?: "Failed to checkout")
        }
    }

    override suspend fun createBranch(path: String, branch: String): GitResult = withContext(Dispatchers.IO) {
        try {
            Git.open(File(path)).use { git ->
                git.branchCreate().setName(branch).call()
                GitResult.Success("Branch created: $branch")
            }
        } catch (e: Exception) {
            GitResult.Error(e.message ?: "Failed to create branch")
        }
    }

    override suspend fun deleteBranch(path: String, branch: String): GitResult = withContext(Dispatchers.IO) {
        try {
            Git.open(File(path)).use { git ->
                git.branchDelete().setBranchNames(branch).call()
                GitResult.Success("Branch deleted: $branch")
            }
        } catch (e: Exception) {
            GitResult.Error(e.message ?: "Failed to delete branch")
        }
    }

    override suspend fun getRemoteUrl(path: String): String? = withContext(Dispatchers.IO) {
        try {
            Git.open(File(path)).use { git ->
                git.repository.config.getString("remote", "origin", "url")
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun clone(url: String, path: String): GitResult = withContext(Dispatchers.IO) {
        try {
            Git.cloneRepository()
                .setURI(url)
                .setDirectory(File(path))
                .call()
                .close()
            GitResult.Success("Cloned successfully")
        } catch (e: Exception) {
            GitResult.Error(e.message ?: "Failed to clone")
        }
    }

    override suspend fun getDiff(path: String, file: String?): String = withContext(Dispatchers.IO) {
        try {
            Git.open(File(path)).use { git ->
                val output = java.io.ByteArrayOutputStream()
                git.diff().setOutputStream(output).call()
                output.toString()
            }
        } catch (e: Exception) {
            ""
        }
    }

    override suspend fun getLog(path: String, limit: Int): List<GitCommit> = getCommits(path, limit)
}
