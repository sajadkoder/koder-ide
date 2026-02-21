package com.koder.ide.presentation.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.foundation.lazy.rememberLazyListState

@Composable
fun TerminalPanel(
    viewModel: TerminalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            state = listState
        ) {
            items(items = uiState.outputLines, key = { it.id }) { line ->
                TerminalLine(line = line)
            }
        }

        TerminalInput(
            currentDirectory = uiState.currentDirectory,
            commandInput = uiState.commandInput,
            isRunning = uiState.isRunning,
            onCommandChange = { viewModel.updateCommand(it) },
            onExecute = { viewModel.executeCommand() },
            onStop = { viewModel.stopProcess() }
        )
    }
}

@Composable
private fun TerminalLine(line: TerminalLine) {
    val textColor = when (line.type) {
        LineType.INPUT -> MaterialTheme.colorScheme.primary
        LineType.OUTPUT -> MaterialTheme.colorScheme.onSurface
        LineType.ERROR -> MaterialTheme.colorScheme.error
        LineType.SYSTEM -> MaterialTheme.colorScheme.tertiary
    }

    SelectionContainer {
        Text(
            text = line.content,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            ),
            color = textColor,
            modifier = Modifier.padding(vertical = 1.dp)
        )
    }
}

@Composable
private fun TerminalInput(
    currentDirectory: String,
    commandInput: String,
    isRunning: Boolean,
    onCommandChange: (String) -> Unit,
    onExecute: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "$ ",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.primary
            )

            BasicTextField(
                value = commandInput,
                onValueChange = onCommandChange,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                singleLine = true
            )

            if (isRunning) {
                IconButton(onClick = onStop, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Stop,
                        "Stop",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                IconButton(onClick = onExecute, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.PlayArrow,
                        "Execute"
                    )
                }
            }
        }
    }
}

data class TerminalLine(
    val id: Long,
    val content: String,
    val type: LineType
)

enum class LineType {
    INPUT,
    OUTPUT,
    ERROR,
    SYSTEM
}
