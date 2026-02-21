package com.koder.ide.presentation.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.navigation.NavHostController
import com.koder.ide.presentation.editor.EditorScreen
import com.koder.ide.presentation.explorer.ExplorerScreen
import com.koder.ide.presentation.terminal.TerminalPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentFilePath by viewModel.currentFilePath.collectAsState()
    var currentTab by remember { mutableStateOf(0) }
    val openFiles = remember { mutableStateOf<List<String>>(emptyList()) }

    val sidebarTabs = listOf(
        SidebarTab("Explorer", Icons.Filled.Folder),
        SidebarTab("Search", Icons.Filled.Search),
        SidebarTab("Git", Icons.Filled.Code),
        SidebarTab("Debug", Icons.Filled.Build)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Koder IDE",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.toggleSidebar() }) {
                        Icon(Icons.Default.Menu, contentDescription = "Toggle sidebar")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Run */ }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Run")
                    }
                    IconButton(onClick = { /* Build */ }) {
                        Icon(Icons.Default.Build, contentDescription = "Build")
                    }
                    IconButton(onClick = { viewModel.openTerminal() }) {
                        Icon(Icons.Default.Terminal, contentDescription = "Terminal")
                    }
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        bottomBar = {
            if (uiState.isBottomPanelOpen) {
                BottomPanel(
                    currentTab = uiState.bottomPanelTab,
                    onTabSelected = { viewModel.setBottomPanelTab(it) },
                    onClose = { viewModel.toggleBottomPanel() }
                )
            }
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedVisibility(
                visible = uiState.isSidebarOpen,
                enter = slideInHorizontally(initialOffsetX = { -it }),
                exit = slideOutHorizontally(targetOffsetX = { -it })
            ) {
                SidebarPanel(
                    tabs = sidebarTabs,
                    selectedTabIndex = currentTab,
                    onTabSelected = { currentTab = it },
                    onFileClick = { path -> viewModel.openFile(path) }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                EditorScreen(
                    filePath = currentFilePath,
                    onOpenFile = { path ->
                        viewModel.openFile(path)
                    }
                )
            }
        }
    }
}

@Composable
private fun SidebarPanel(
    tabs: List<SidebarTab>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onFileClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        NavigationRail(
            modifier = Modifier.fillMaxHeight()
        ) {
            tabs.forEachIndexed { index, tab ->
                NavigationRailItem(
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title
                        )
                    },
                    label = { Text(tab.title) }
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            when (selectedTabIndex) {
                0 -> ExplorerScreen(
                    onFileClick = onFileClick
                )
                1 -> SearchPanel()
                2 -> GitPanel()
                3 -> DebugPanel()
            }
        }
    }
}

@Composable
private fun SearchPanel() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Search in files...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GitPanel() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Source Control",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No repository detected",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DebugPanel() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Debug",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No active debug session",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomPanel(
    currentTab: BottomPanelTab,
    onTabSelected: (BottomPanelTab) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        TabRow(
            selectedTabIndex = BottomPanelTab.entries.indexOf(currentTab),
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ) {
            BottomPanelTab.entries.forEach { tab ->
                Tab(
                    selected = currentTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = { Text(tab.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    icon = {
                        when (tab) {
                            BottomPanelTab.TERMINAL -> Icon(Icons.Default.Terminal, null)
                            BottomPanelTab.OUTPUT -> Icon(Icons.Default.Description, null)
                            BottomPanelTab.PROBLEMS -> Icon(Icons.Default.Warning, null)
                            BottomPanelTab.DEBUG -> Icon(Icons.Default.Build, null)
                        }
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentTab) {
                BottomPanelTab.TERMINAL -> TerminalPanel()
                BottomPanelTab.OUTPUT -> OutputPanel()
                BottomPanelTab.PROBLEMS -> ProblemsPanel()
                BottomPanelTab.DEBUG -> DebugOutputPanel()
            }
        }
    }
}

@Composable
private fun OutputPanel() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "Output will appear here...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProblemsPanel() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "No problems detected",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DebugOutputPanel() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "Debug output will appear here...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class SidebarTab(
    val title: String,
    val icon: ImageVector
)
