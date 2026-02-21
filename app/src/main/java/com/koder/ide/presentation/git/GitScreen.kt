package com.koder.ide.presentation.git

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.koder.ide.domain.model.GitBranch
import com.koder.ide.domain.model.GitCommit
import com.koder.ide.domain.model.GitFile
import com.koder.ide.domain.model.GitFileStatus
import com.koder.ide.domain.model.GitStatus

@Composable
fun GitScreen(
    viewModel: GitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCommitDialog by remember { mutableStateOf(false) }
    var showBranchDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        GitHeader(
            branchName = uiState.currentBranch,
            ahead = uiState.ahead,
            behind = uiState.behind,
            onRefresh = { viewModel.refresh() },
            onPull = { viewModel.pull() },
            onPush = { viewModel.push() },
            onCommit = { showCommitDialog = true },
            onBranch = { showBranchDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Text(
                    text = uiState.error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error
                )
            }
            uiState.status == null -> {
                Text(
                    text = "No repository detected in current directory",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                GitStatusView(
                    status = uiState.status!!,
                    onStageFile = { viewModel.stageFile(it) },
                    onUnstageFile = { viewModel.unstageFile(it) },
                    onDiscardFile = { viewModel.discardFile(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                CommitHistory(
                    commits = uiState.recentCommits,
                    onViewCommit = { viewModel.viewCommit(it) }
                )
            }
        }
    }

    if (showCommitDialog) {
        CommitDialog(
            onDismiss = { showCommitDialog = false },
            onCommit = { message ->
                viewModel.commit(message)
                showCommitDialog = false
            }
        )
    }

    if (showBranchDialog) {
        BranchDialog(
            branches = uiState.branches,
            currentBranch = uiState.currentBranch,
            onDismiss = { showBranchDialog = false },
            onCreateBranch = { name ->
                viewModel.createBranch(name)
                showBranchDialog = false
            },
            onCheckout = { branch ->
                viewModel.checkout(branch)
                showBranchDialog = false
            }
        )
    }
}

@Composable
private fun GitHeader(
    branchName: String,
    ahead: Int,
    behind: Int,
    onRefresh: () -> Unit,
    onPull: () -> Unit,
    onPush: () -> Unit,
    onCommit: () -> Unit,
    onBranch: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = branchName,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                    IconButton(onClick = onPull) {
                        Icon(Icons.Default.ArrowDownward, "Pull")
                    }
                    IconButton(onClick = onPush) {
                        Icon(Icons.Default.ArrowUpward, "Push")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (ahead > 0) {
                    Text(
                        text = "$ahead commits ahead",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (behind > 0) {
                    Text(
                        text = "$behind commits behind",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onCommit) {
                    Icon(Icons.Default.Commit, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Commit")
                }
                Button(onClick = onBranch) {
                    Icon(Icons.Default.Code, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Branches")
                }
            }
        }
    }
}

@Composable
private fun GitStatusView(
    status: GitStatus,
    onStageFile: (GitFile) -> Unit,
    onUnstageFile: (GitFile) -> Unit,
    onDiscardFile: (GitFile) -> Unit
) {
    Column {
        if (status.staged.isNotEmpty()) {
            Text(
                text = "Staged Changes",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            status.staged.forEach { file ->
                GitFileItem(
                    file = file,
                    isStaged = true,
                    onStage = { onStageFile(file) },
                    onUnstage = { onUnstageFile(file) },
                    onDiscard = { onDiscardFile(file) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (status.unstaged.isNotEmpty()) {
            Text(
                text = "Changes",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            status.unstaged.forEach { file ->
                GitFileItem(
                    file = file,
                    isStaged = false,
                    onStage = { onStageFile(file) },
                    onUnstage = { onUnstageFile(file) },
                    onDiscard = { onDiscardFile(file) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (status.untracked.isNotEmpty()) {
            Text(
                text = "Untracked Files",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            status.untracked.forEach { file ->
                GitFileItem(
                    file = file,
                    isStaged = false,
                    onStage = { onStageFile(file) },
                    onUnstage = { onUnstageFile(file) },
                    onDiscard = { onDiscardFile(file) }
                )
            }
        }
    }
}

@Composable
private fun GitFileItem(
    file: GitFile,
    isStaged: Boolean,
    onStage: () -> Unit,
    onUnstage: () -> Unit,
    onDiscard: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                GitStatusIcon(status = file.status)
                Text(
                    text = file.path,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                if (!isStaged) {
                    IconButton(onClick = onStage, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, "Stage", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDiscard, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Discard", modifier = Modifier.size(18.dp))
                    }
                } else {
                    IconButton(onClick = onUnstage, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, "Unstage", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GitStatusIcon(status: GitFileStatus) {
    val (icon, color) = when (status) {
        GitFileStatus.ADDED -> Icons.Default.Add to MaterialTheme.colorScheme.primary
        GitFileStatus.MODIFIED -> Icons.Default.Edit to MaterialTheme.colorScheme.tertiary
        GitFileStatus.DELETED -> Icons.Default.Delete to MaterialTheme.colorScheme.error
        GitFileStatus.UNTRACKED -> Icons.Default.Code to MaterialTheme.colorScheme.onSurfaceVariant
        else -> Icons.Default.Code to MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Icon(
        imageVector = icon,
        contentDescription = status.name,
        modifier = Modifier.size(16.dp),
        tint = color
    )
}

@Composable
private fun CommitHistory(
    commits: List<GitCommit>,
    onViewCommit: (GitCommit) -> Unit
) {
    Column {
        Text(
            text = "Recent Commits",
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        commits.forEach { commit ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                onClick = { onViewCommit(commit) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Commit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = commit.message,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${commit.author} - ${commit.shortId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommitDialog(
    onDismiss: () -> Unit,
    onCommit: (String) -> Unit
) {
    var message by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Commit Changes") },
        text = {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Commit message") },
                singleLine = false,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onCommit(message) },
                enabled = message.isNotBlank()
            ) {
                Text("Commit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun BranchDialog(
    branches: List<GitBranch>,
    currentBranch: String,
    onDismiss: () -> Unit,
    onCreateBranch: (String) -> Unit,
    onCheckout: (String) -> Unit
) {
    var newBranchName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Branches") },
        text = {
            Column {
                OutlinedTextField(
                    value = newBranchName,
                    onValueChange = { newBranchName = it },
                    label = { Text("New branch name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Existing branches:",
                    style = MaterialTheme.typography.labelMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.height(200.dp)
                ) {
                    items(branches) { branch ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (branch.isCurrent) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(branch.name)
                            }
                            
                            if (!branch.isCurrent) {
                                TextButton(onClick = { onCheckout(branch.name) }) {
                                    Text("Checkout")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreateBranch(newBranchName) },
                enabled = newBranchName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
