package com.koder.ide.presentation.explorer

import android.os.Environment
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.koder.ide.core.util.FileUtils
import com.koder.ide.domain.model.FileNode
import java.io.File

@Composable
fun ExplorerScreen(
    onFileClick: (String) -> Unit,
    viewModel: ExplorerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ExplorerHeader(
            currentPath = uiState.currentPath,
            onRefresh = { viewModel.refresh() },
            onNewFile = { showNewFileDialog = true },
            onNewFolder = { showNewFolderDialog = true }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = uiState.files,
                key = { it.path }
            ) { fileNode ->
                FileTreeItem(
                    node = fileNode,
                    depth = 0,
                    onFileClick = onFileClick,
                    onToggleExpand = { viewModel.toggleExpand(it) }
                )
            }
        }
    }

    if (showNewFileDialog) {
        NewFileDialog(
            onDismiss = { showNewFileDialog = false },
            onConfirm = { name ->
                viewModel.createFile(name, isDirectory = false)
                showNewFileDialog = false
            }
        )
    }

    if (showNewFolderDialog) {
        NewFolderDialog(
            onDismiss = { showNewFolderDialog = false },
            onConfirm = { name ->
                viewModel.createFile(name, isDirectory = true)
                showNewFolderDialog = false
            }
        )
    }
}

@Composable
private fun ExplorerHeader(
    currentPath: String,
    onRefresh: () -> Unit,
    onNewFile: () -> Unit,
    onNewFolder: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "EXPLORER",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = currentPath,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            IconButton(onClick = onNewFile, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, "New File", modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onNewFolder, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.CreateNewFolder, "New Folder", modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Refresh, "Refresh", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun FileTreeItem(
    node: FileNode,
    depth: Int,
    onFileClick: (String) -> Unit,
    onToggleExpand: (FileNode) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (node.isDirectory) {
                        onToggleExpand(node)
                    } else {
                        onFileClick(node.path)
                    }
                }
                .padding(
                    start = (depth * 16 + 8).dp,
                    end = 8.dp,
                    top = 4.dp,
                    bottom = 4.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (node.isDirectory) {
                Icon(
                    imageVector = if (node.isExpanded) Icons.Default.KeyboardArrowDown
                                  else Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }

            Spacer(modifier = Modifier.width(4.dp))

            FileIcon(
                isDirectory = node.isDirectory,
                isExpanded = node.isExpanded,
                language = FileUtils.getLanguageFromFile(node.file)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = node.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    "More options",
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Rename") },
                    onClick = { showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = { showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("Copy Path") },
                    onClick = { showMenu = false }
                )
            }
        }

        if (node.isDirectory && node.isExpanded) {
            node.children.forEach { child ->
                FileTreeItem(
                    node = child,
                    depth = depth + 1,
                    onFileClick = onFileClick,
                    onToggleExpand = onToggleExpand
                )
            }
        }
    }
}

@Composable
private fun FileIcon(
    isDirectory: Boolean,
    isExpanded: Boolean,
    language: String
) {
    val icon: ImageVector = when {
        isDirectory && isExpanded -> Icons.Filled.FolderOpen
        isDirectory -> Icons.Filled.Folder
        else -> Icons.Filled.Description
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
        tint = if (isDirectory) MaterialTheme.colorScheme.primary
               else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun NewFileDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New File") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("File name") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }) {
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

@Composable
private fun NewFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Folder") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Folder name") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }) {
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
