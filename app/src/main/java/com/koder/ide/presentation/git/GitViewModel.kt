package com.koder.ide.presentation.git

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koder.ide.domain.model.GitBranch
import com.koder.ide.domain.model.GitCommit
import com.koder.ide.domain.model.GitFile
import com.koder.ide.domain.model.GitFileStatus
import com.koder.ide.domain.model.GitStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File
import javax.inject.Inject

@HiltViewModel
class GitViewModel @Inject constructor(
) : ViewModel() {

    private val _uiState = MutableStateFlow(GitUiState())
    val uiState: StateFlow<GitUiState> = _uiState.asStateFlow()

    private var repository: Repository? = null
    private var git: Git? = null

    fun initializeRepository(projectPath: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val repoDir = File(projectPath, ".git")
                if (repoDir.exists()) {
                    repository = FileRepositoryBuilder()
                        .setGitDir(repoDir)
                        .readEnvironment()
                        .findGitDir()
                        .build()
                    git = Git(repository)
                    refresh()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Not a git repository"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val gitInstance = git ?: return@launch
            
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val status = withContext(Dispatchers.IO) {
                    val statusCmd = gitInstance.status().call()
                    
                    val staged = statusCmd.added.map { GitFile(it, GitFileStatus.ADDED) } +
                            statusCmd.changed.map { GitFile(it, GitFileStatus.MODIFIED) } +
                            statusCmd.removed.map { GitFile(it, GitFileStatus.DELETED) }
                    
                    val unstaged = statusCmd.modified.map { GitFile(it, GitFileStatus.MODIFIED) } +
                            statusCmd.missing.map { GitFile(it, GitFileStatus.DELETED) }
                    
                    val untracked = statusCmd.untracked.map { GitFile(it, GitFileStatus.UNTRACKED) }
                    
                    GitStatus(
                        branch = repository?.branch ?: "main",
                        staged = staged,
                        unstaged = unstaged,
                        untracked = untracked
                    )
                }
                
                val commits = withContext(Dispatchers.IO) {
                    gitInstance.log().setMaxCount(20).call().map { commit ->
                        GitCommit(
                            id = commit.id.name,
                            shortId = commit.id.abbreviate(7).name(),
                            message = commit.fullMessage,
                            author = commit.authorIdent.name,
                            authorEmail = commit.authorIdent.emailAddress,
                            date = commit.commitTime.toLong() * 1000
                        )
                    }
                }
                
                val branches = withContext(Dispatchers.IO) {
                    gitInstance.branchList().call().map { ref ->
                        GitBranch(
                            name = ref.name.removePrefix("refs/heads/"),
                            isCurrent = ref.name == repository?.fullBranch
                        )
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    status = status,
                    currentBranch = status.branch,
                    recentCommits = commits,
                    branches = branches,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun stageFile(file: GitFile) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    git?.add()?.addFilepattern(file.path)?.call()
                }
                refresh()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun unstageFile(file: GitFile) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    git?.reset()?.addPath(file.path)?.call()
                }
                refresh()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun discardFile(file: GitFile) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    git?.checkout()?.addPath(file.path)?.call()
                }
                refresh()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun commit(message: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    git?.commit()?.setMessage(message)?.call()
                }
                refresh()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun pull() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                withContext(Dispatchers.IO) {
                    git?.pull()?.call()
                }
                refresh()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun push() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                withContext(Dispatchers.IO) {
                    git?.push()?.call()
                }
                refresh()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun createBranch(name: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    git?.branchCreate()?.setName(name)?.call()
                }
                refresh()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun checkout(branchName: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    git?.checkout()?.setName(branchName)?.call()
                }
                refresh()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun viewCommit(commit: GitCommit) {
    }

    override fun onCleared() {
        super.onCleared()
        git?.close()
        repository?.close()
    }
}

data class GitUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val status: GitStatus? = null,
    val currentBranch: String = "main",
    val ahead: Int = 0,
    val behind: Int = 0,
    val recentCommits: List<GitCommit> = emptyList(),
    val branches: List<GitBranch> = emptyList()
)
