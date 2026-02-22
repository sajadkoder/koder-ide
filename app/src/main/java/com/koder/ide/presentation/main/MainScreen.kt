package com.koder.ide.presentation.main

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.koder.ide.core.editor.CodeEditorView
import com.koder.ide.core.editor.LanguageManager
import com.koder.ide.core.util.FileUtils
import com.koder.ide.presentation.theme.KoderTheme
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.openFileFromUri(it, context) }
    }

    KoderTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Code,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Koder IDE",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.toggleSidebar() }) {
                            Icon(Icons.Default.Menu, "Toggle sidebar")
                        }
                    },
                    actions = {
                        IconButton(onClick = { filePickerLauncher.launch(arrayOf("*/*")) }) {
                            Icon(Icons.Default.FolderOpen, "Open file")
                        }
                        IconButton(onClick = { showNewFileDialog = true }) {
                            Icon(Icons.Default.Add, "New file")
                        }
                        IconButton(onClick = { viewModel.toggleTerminal() }) {
                            Icon(Icons.Default.Terminal, "Terminal")
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, "More")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Close All Tabs") },
                                    onClick = {
                                        showMenu = false
                                        state.tabs.forEach { viewModel.closeTab(it.path) }
                                    },
                                    leadingIcon = { Icon(Icons.Default.Close, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    onClick = { showMenu = false },
                                    leadingIcon = { Icon(Icons.Default.Settings, null) }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (state.tabs.isNotEmpty()) {
                    TabBar(
                        tabs = state.tabs,
                        currentIndex = state.currentTabIndex,
                        onSelect = { viewModel.setCurrentTab(it) },
                        onClose = { viewModel.closeTab(it) }
                    )
                }

                Row(modifier = Modifier.weight(1f)) {
                    AnimatedVisibility(
                        visible = state.isSidebarOpen,
                        enter = slideInHorizontally(initialOffsetX = { -it }),
                        exit = slideOutHorizontally(targetOffsetX = { -it })
                    ) {
                        FileExplorerSidebar(
                            projectPath = state.projectPath,
                            onFileClick = { viewModel.openFile(it) },
                            onNewFile = { showNewFileDialog = true },
                            onNewFolder = { showNewFolderDialog = true }
                        )
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        val currentFile = state.tabs.getOrNull(state.currentTabIndex)?.let { File(it.path) }
                        
                        if (currentFile != null && currentFile.exists()) {
                            EditorPanel(
                                file = currentFile,
                                onSaved = { viewModel.updateTabModified(currentFile.absolutePath, false) },
                                onModified = { viewModel.updateTabModified(currentFile.absolutePath, true) }
                            )
                        } else {
                            EmptyState(
                                onOpenFile = { filePickerLauncher.launch(arrayOf("*/*")) },
                                onNewFile = { showNewFileDialog = true }
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = state.isTerminalOpen,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    TerminalPanel(onClose = { viewModel.toggleTerminal() })
                }
            }
        }

        if (showNewFileDialog) {
            NewFileDialog(
                projectPath = state.projectPath,
                isFolder = false,
                onDismiss = { showNewFileDialog = false },
                onConfirm = { name ->
                    state.projectPath?.let { path ->
                        val file = FileUtils.createFile(File(path), name)
                        if (file != null) {
                            viewModel.openFile(file.absolutePath)
                            Toast.makeText(context, "Created: $name", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to create file", Toast.LENGTH_SHORT).show()
                        }
                    }
                    showNewFileDialog = false
                }
            )
        }

        if (showNewFolderDialog) {
            NewFileDialog(
                projectPath = state.projectPath,
                isFolder = true,
                onDismiss = { showNewFolderDialog = false },
                onConfirm = { name ->
                    state.projectPath?.let { path ->
                        val folder = FileUtils.createDirectory(File(path), name)
                        if (folder != null) {
                            Toast.makeText(context, "Created folder: $name", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to create folder", Toast.LENGTH_SHORT).show()
                        }
                    }
                    showNewFolderDialog = false
                }
            )
        }
    }
}

@Composable
private fun TabBar(
    tabs: List<EditorTab>,
    currentIndex: Int,
    onSelect: (Int) -> Unit,
    onClose: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        itemsIndexed(tabs, key = { _, it -> it.path }) { index, tab ->
            val isSelected = index == currentIndex
            Surface(
                modifier = Modifier.clickable { onSelect(index) },
                color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = if (isSelected) 2.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tab.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.widthIn(max = 120.dp)
                    )
                    if (tab.isModified) {
                        Text(
                            text = " •",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { onClose(tab.path) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FileExplorerSidebar(
    projectPath: String?,
    onFileClick: (String) -> Unit,
    onNewFile: () -> Unit,
    onNewFolder: () -> Unit
) {
    var currentPath by remember { mutableStateOf(projectPath) }
    var expandedPaths by remember { mutableStateOf(setOf<String>()) }
    val files by remember(currentPath) {
        derivedStateOf {
            currentPath?.let { File(it) }
                ?.takeIf { it.exists() && it.isDirectory }
                ?.let { FileUtils.listFiles(it) }
                ?: emptyList()
        }
    }

    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "EXPLORER",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Row {
                IconButton(onClick = onNewFile, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Add, "New File", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onNewFolder, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.CreateNewFolder, "New Folder", modifier = Modifier.size(16.dp))
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            currentPath?.let { File(it).parentFile?.absolutePath?.let { currentPath = it } }
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Up",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "..",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(files, key = { it.absolutePath }) { file ->
                FileTreeItem(
                    file = file,
                    isExpanded = file.absolutePath in expandedPaths,
                    onClick = {
                        if (file.isDirectory) {
                            expandedPaths = if (file.absolutePath in expandedPaths) {
                                expandedPaths - file.absolutePath
                            } else {
                                expandedPaths + file.absolutePath
                            }
                        } else {
                            onFileClick(file.absolutePath)
                        }
                    },
                    onDoubleClick = {
                        if (file.isDirectory) {
                            currentPath = file.absolutePath
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FileTreeItem(
    file: File,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (file.isDirectory) {
            Icon(
                if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                Icons.Outlined.Description,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            file.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun EditorPanel(
    file: File,
    onSaved: () -> Unit,
    onModified: () -> Unit
) {
    var editorView by remember { mutableStateOf<CodeEditorView?>(null) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(file.name, style = MaterialTheme.typography.bodyMedium)
                }
                Row {
                    IconButton(
                        onClick = {
                            editorView?.undo()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Undo, "Undo", modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = {
                            editorView?.redo()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Redo, "Redo", modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = {
                            if (editorView?.save() == true) {
                                onSaved()
                                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Save, "Save", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        AndroidView(
            factory = { ctx ->
                CodeEditorView(ctx).apply {
                    openFile(file)
                    onContentChanged = { onModified() }
                    editorView = this
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            update = { view ->
                if (view.getCurrentFile()?.absolutePath != file.absolutePath) {
                    view.openFile(file)
                }
            }
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    LanguageManager.getLanguageName(file.extension),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    FileUtils.formatFileSize(file.length()),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    FileUtils.formatDate(file.lastModified()),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }

    DisposableEffect(file) {
        onDispose { editorView?.release() }
    }
}

@Composable
private fun TerminalPanel(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Terminal, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Terminal", style = MaterialTheme.typography.labelLarge)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, "Close")
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Terminal ready - coming soon",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyState(onOpenFile: () -> Unit, onNewFile: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Code,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Text(
                "Welcome to Koder IDE",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                "Open a file or create a new one to get started",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onOpenFile) {
                    Icon(Icons.Default.FolderOpen, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open File")
                }
                OutlinedButton(onClick = onNewFile) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New File")
                }
            }
        }
    }
}

@Composable
private fun NewFileDialog(
    projectPath: String?,
    isFolder: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { 
            Icon(if (isFolder) Icons.Default.CreateNewFolder else Icons.Default.Add, null)
        },
        title = { Text(if (isFolder) "New Folder" else "New File") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(if (isFolder) "Folder name" else "File name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
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
