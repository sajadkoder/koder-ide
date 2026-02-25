package com.koder.ide.presentation.main

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.koder.ide.core.editor.EditorHelper
import com.koder.ide.core.util.FileUtils
import com.koder.ide.domain.model.EditorTab
import com.koder.ide.domain.model.ProjectFile
import com.koder.ide.presentation.theme.KoderTheme
import io.github.rosemoe.sora.widget.CodeEditor
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var showGitCommitDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.message, uiState.error) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }
    
    KoderTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Koder IDE", style = MaterialTheme.typography.titleMedium)
                            Text(
                                uiState.currentPath.takeLast(40),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.toggleSidebar() }) {
                            Icon(Icons.Default.Menu, "Toggle")
                        }
                    },
                    actions = {
                        if (uiState.gitStatus != null) {
                            IconButton(onClick = { viewModel.toggleGitPanel() }) {
                                Icon(
                                    Icons.Default.Cloud,
                                    "Git",
                                    tint = if (uiState.gitStatus?.isRepository == true) 
                                        MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.toggleTerminal() }) {
                            Icon(Icons.Default.Terminal, "Terminal")
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, "Menu")
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("New File") },
                                    onClick = { showMenu = false; showNewFileDialog = true },
                                    leadingIcon = { Icon(Icons.Default.Add, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("New Folder") },
                                    onClick = { showMenu = false; showNewFolderDialog = true },
                                    leadingIcon = { Icon(Icons.Default.CreateNewFolder, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Refresh") },
                                    onClick = { showMenu = false; viewModel.navigateTo(uiState.currentPath) },
                                    leadingIcon = { Icon(Icons.Default.Refresh, null) }
                                )
                                HorizontalDivider()
                                if (uiState.gitStatus?.isRepository != true) {
                                    DropdownMenuItem(
                                        text = { Text("Git Init") },
                                        onClick = { showMenu = false; viewModel.gitInit() },
                                        leadingIcon = { Icon(Icons.Default.Cloud, null) }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Close All Tabs") },
                                    onClick = { 
                                        showMenu = false
                                        uiState.tabs.forEachIndexed { index, _ -> viewModel.closeTab(index) }
                                    },
                                    leadingIcon = { Icon(Icons.Default.Close, null) }
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
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Tabs
                if (uiState.tabs.isNotEmpty()) {
                    TabRow(
                        selectedTabIndex = uiState.currentTabIndex,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        uiState.tabs.forEachIndexed { index, tab ->
                            Tab(
                                selected = index == uiState.currentTabIndex,
                                onClick = { viewModel.selectTab(index) },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            tab.file.name,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (tab.isModified) {
                                            Text(" ●", color = MaterialTheme.colorScheme.primary)
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Close",
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { viewModel.closeTab(index) }
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
                
                Row(modifier = Modifier.weight(1f)) {
                    // Sidebar - File Explorer
                    AnimatedVisibility(
                        visible = uiState.isSidebarOpen,
                        enter = slideInHorizontally { -it },
                        exit = slideOutHorizontally { -it }
                    ) {
                        FileExplorer(
                            currentPath = uiState.currentPath,
                            files = uiState.files,
                            onNavigate = { viewModel.navigateTo(it) },
                            onOpenFile = { viewModel.openFile(it) },
                            onNavigateUp = { viewModel.navigateUp() }
                        )
                    }
                    
                    // Main Content Area
                    Box(modifier = Modifier.weight(1f)) {
                        if (uiState.currentTabIndex >= 0) {
                            val currentTab = uiState.tabs.getOrNull(uiState.currentTabIndex)
                            if (currentTab != null) {
                                EditorPane(
                                    tab = currentTab,
                                    onContentChange = { viewModel.updateTabContent(currentTab.id, it) },
                                    onSave = { viewModel.saveCurrentTab() }
                                )
                            }
                        } else if (uiState.isLoading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else {
                            EmptyState()
                        }
                    }
                    
                    // Git Panel
                    if (uiState.isGitPanelOpen) {
                        GitPanel(
                            gitStatus = uiState.gitStatus,
                            onAdd = { viewModel.gitAdd() },
                            onCommit = { showGitCommitDialog = true },
                            onPush = { viewModel.gitPush() },
                            onPull = { viewModel.gitPull() },
                            onRefresh = { viewModel.refreshGitStatus() }
                        )
                    }
                }
                
                // Terminal
                AnimatedVisibility(
                    visible = uiState.isTerminalOpen,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it }
                ) {
                    TerminalPane(
                        output = uiState.terminalOutput,
                        onExecute = { viewModel.executeCommand(it) },
                        onClear = { viewModel.clearTerminal() },
                        onClose = { viewModel.toggleTerminal() }
                    )
                }
            }
        }
        
        // Dialogs
        if (showNewFileDialog) {
            NewItemDialog(
                title = "New File",
                onDismiss = { showNewFileDialog = false },
                onConfirm = { name ->
                    viewModel.createFile(name, false)
                    showNewFileDialog = false
                }
            )
        }
        
        if (showNewFolderDialog) {
            NewItemDialog(
                title = "New Folder",
                onDismiss = { showNewFolderDialog = false },
                onConfirm = { name ->
                    viewModel.createFile(name, true)
                    showNewFolderDialog = false
                }
            )
        }
        
        if (showGitCommitDialog) {
            GitCommitDialog(
                onDismiss = { showGitCommitDialog = false },
                onCommit = { message ->
                    viewModel.gitCommit(message)
                    showGitCommitDialog = false
                }
            )
        }
    }
}

@Composable
private fun FileExplorer(
    currentPath: String,
    files: List<ProjectFile>,
    onNavigate: (String) -> Unit,
    onOpenFile: (String) -> Unit,
    onNavigateUp: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "EXPLORER",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onNavigateUp, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.ArrowUpward, "Up", modifier = Modifier.size(18.dp))
            }
        }
        
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateUp() }
                        .padding(12.dp, 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("..", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            items(files) { file ->
                FileItem(
                    file = file,
                    onClick = {
                        if (file.isDirectory) {
                            onNavigate(file.path)
                        } else {
                            onOpenFile(file.path)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FileItem(file: ProjectFile, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description,
            null,
            modifier = Modifier.size(20.dp),
            tint = if (file.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                file.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!file.isDirectory) {
                Text(
                    FileUtils.formatFileSize(file.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EditorPane(
    tab: EditorTab,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit
) {
    var editor by remember { mutableStateOf<CodeEditor?>(null) }
    val context = LocalContext.current
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Editor Toolbar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        FileUtils.getLanguageFromExtension(tab.file.extension),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row {
                    IconButton(onClick = { editor?.undo() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Undo, "Undo", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { editor?.redo() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Redo, "Redo", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onSave, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Save, "Save", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        
        // Editor
        AndroidView(
            factory = { ctx ->
                CodeEditor(ctx).apply {
                    EditorHelper.configure(this)
                    EditorHelper.setLanguage(this, tab.file.extension)
                    setText(tab.content)
                    subscribeEvent(io.github.rosemoe.sora.event.ContentChangeEvent::class.java) { _, _ ->
                        onContentChange(text.toString())
                    }
                    editor = this
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            update = { view ->
                if (view.text.toString() != tab.content && !tab.isModified) {
                    view.setText(tab.content)
                }
            }
        )
    }
}

@Composable
private fun GitPanel(
    gitStatus: com.koder.ide.domain.model.GitStatus?,
    onAdd: () -> Unit,
    onCommit: () -> Unit,
    onPush: () -> Unit,
    onPull: () -> Unit,
    onRefresh: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("GIT", style = MaterialTheme.typography.labelMedium)
                IconButton(onClick = onRefresh, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Refresh, "Refresh", modifier = Modifier.size(18.dp))
                }
            }
            
            gitStatus?.let { status ->
                if (status.branch.isNotEmpty()) {
                    Text(
                        "Branch: ${status.branch}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (status.untrackedFiles.isNotEmpty()) {
                    Text("Untracked (${status.untrackedFiles.size})", style = MaterialTheme.typography.labelSmall)
                    status.untrackedFiles.take(3).forEach {
                        Text("• $it", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                if (status.stagedChanges.isNotEmpty()) {
                    Text("Staged (${status.stagedChanges.size})", style = MaterialTheme.typography.labelSmall)
                }
                
                if (status.unstagedChanges.isNotEmpty()) {
                    Text("Modified (${status.unstagedChanges.size})", style = MaterialTheme.typography.labelSmall)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onAdd,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Add", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = onCommit,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Commit", style = MaterialTheme.typography.labelSmall)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onPull,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Pull", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = onPush,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Push", style = MaterialTheme.typography.labelSmall)
                    }
                }
            } ?: Text("Not a git repository", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TerminalPane(
    output: List<String>,
    onExecute: (String) -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit
) {
    var command by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
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
                Text("Terminal", style = MaterialTheme.typography.labelMedium)
            }
            Row {
                IconButton(onClick = onClear, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.ClearAll, "Clear", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, "Close", modifier = Modifier.size(18.dp))
                }
            }
        }
        
        // Terminal output
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                output.forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = if (line.startsWith("Error")) 
                            MaterialTheme.colorScheme.error 
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Command input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "\$ ",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = command,
                onValueChange = { command = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter command...") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                trailingIcon = {
                    IconButton(onClick = {
                        if (command.isNotBlank()) {
                            onExecute(command)
                            command = ""
                        }
                    }) {
                        Icon(Icons.Default.Send, "Execute")
                    }
                }
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Code,
                null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Open a file to start editing", style = MaterialTheme.typography.bodyLarge)
            Text(
                "Use the file explorer on the left",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NewItemDialog(title: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun GitCommitDialog(onDismiss: () -> Unit, onCommit: (String) -> Unit) {
    var message by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Git Commit") },
        text = {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Commit message") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        },
        confirmButton = {
            Button(onClick = { onCommit(message) }, enabled = message.isNotBlank()) {
                Text("Commit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
