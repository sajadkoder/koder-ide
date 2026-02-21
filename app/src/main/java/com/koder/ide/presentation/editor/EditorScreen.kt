package com.koder.ide.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.io.File

@Composable
fun EditorScreen(
    filePath: String?,
    onOpenFile: (String) -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val openTabs = remember { mutableStateListOf<EditorTab>() }
    var selectedTabIndex by remember { mutableStateOf(0) }

    if (filePath != null && openTabs.none { it.path == filePath }) {
        openTabs.add(EditorTab(path = filePath, name = File(filePath).name))
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (openTabs.isNotEmpty()) {
            EditorTabRow(
                tabs = openTabs.toList(),
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it },
                onTabClosed = { index ->
                    openTabs.removeAt(index)
                    if (selectedTabIndex >= openTabs.size && openTabs.isNotEmpty()) {
                        selectedTabIndex = openTabs.size - 1
                    }
                }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (selectedTabIndex < openTabs.size) {
                    CodeEditor(
                        filePath = openTabs[selectedTabIndex].path,
                        content = uiState.content,
                        onContentChange = { viewModel.updateContent(it) },
                        language = uiState.language,
                        isModified = uiState.isModified,
                        onSave = { viewModel.saveFile() }
                    )
                }
            }

            StatusBar(
                line = uiState.cursorLine,
                column = uiState.cursorColumn,
                language = uiState.language,
                encoding = uiState.encoding,
                fileSize = uiState.fileSize
            )
        } else {
            EmptyEditorState(
                onOpenFile = onOpenFile,
                onNewFile = { viewModel.createNewFile() }
            )
        }
    }
}

@Composable
private fun EditorTabRow(
    tabs: List<EditorTab>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onTabClosed: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
    ) {
        items(items = tabs, key = { it.path }) { tab ->
            val index = tabs.indexOf(tab)
            EditorTabItem(
                tab = tab,
                isSelected = index == selectedTabIndex,
                onClick = { onTabSelected(index) },
                onClose = { onTabClosed(index) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorTabItem(
    tab: EditorTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.padding(horizontal = 1.dp),
        color = if (isSelected) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Filled.Description else Icons.Outlined.Description,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = tab.name,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close tab",
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

data class EditorTab(
    val path: String,
    val name: String,
    val isModified: Boolean = false
)

@Composable
private fun CodeEditor(
    filePath: String,
    content: String,
    onContentChange: (String) -> Unit,
    language: String,
    isModified: Boolean,
    onSave: () -> Unit
) {
    val scrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = filePath,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onSave, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Save, "Save", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Undo, "Undo", modifier = Modifier.size(18.dp))
                }
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, "More", modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Format Code") },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Go to Line") },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Find & Replace") },
                            onClick = { showMenu = false }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState)
            ) {
                LineNumbers(
                    lines = content.lines().size,
                    modifier = Modifier.padding(end = 8.dp)
                )

                BasicTextField(
                    value = content,
                    onValueChange = onContentChange,
                    modifier = Modifier
                        .weight(1f),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun LineNumbers(
    lines: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(start = 8.dp, top = 8.dp)
    ) {
        repeat(lines) { index ->
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun StatusBar(
    line: Int,
    column: Int,
    language: String,
    encoding: String,
    fileSize: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatusBarItem("Ln $line, Col $column")
                StatusBarItem(language)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatusBarItem(encoding)
                StatusBarItem(fileSize)
            }
        }
    }
}

@Composable
private fun StatusBarItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer
    )
}

@Composable
private fun EmptyEditorState(
    onOpenFile: (String) -> Unit,
    onNewFile: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "No file open",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Open a file from the explorer or create a new one",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onNewFile) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New File")
                }
                TextButton(onClick = { onOpenFile("") }) {
                    Icon(Icons.Default.Description, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open File")
                }
            }
        }
    }
}
