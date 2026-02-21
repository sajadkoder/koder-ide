package com.koder.ide.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.koder.ide.domain.model.EditorTheme
import com.koder.ide.domain.model.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection(title = "Appearance", icon = Icons.Default.Palette) {
                SettingsItem(
                    title = "Theme",
                    subtitle = uiState.themeMode.displayName,
                    onClick = { viewModel.showThemeDialog() }
                )
                
                SettingsItem(
                    title = "Editor Theme",
                    subtitle = uiState.editorTheme.displayName,
                    onClick = { viewModel.showEditorThemeDialog() }
                )
            }

            SettingsSection(title = "Editor", icon = Icons.Default.Code) {
                SettingsSliderItem(
                    title = "Font Size",
                    value = uiState.fontSize.toFloat(),
                    valueRange = 8f..32f,
                    onValueChange = { viewModel.setFontSize(it.toInt()) }
                )

                SettingsItem(
                    title = "Font Family",
                    subtitle = uiState.fontFamily,
                    onClick = { viewModel.showFontDialog() }
                )

                SettingsSliderItem(
                    title = "Tab Size",
                    value = uiState.tabSize.toFloat(),
                    valueRange = 2f..8f,
                    steps = 5,
                    onValueChange = { viewModel.setTabSize(it.toInt()) }
                )

                SettingsSwitchItem(
                    title = "Word Wrap",
                    checked = uiState.wordWrap,
                    onCheckedChange = { viewModel.setWordWrap(it) }
                )

                SettingsSwitchItem(
                    title = "Line Numbers",
                    checked = uiState.lineNumbers,
                    onCheckedChange = { viewModel.setLineNumbers(it) }
                )

                SettingsSwitchItem(
                    title = "Minimap",
                    checked = uiState.minimap,
                    onCheckedChange = { viewModel.setMinimap(it) }
                )

                SettingsSwitchItem(
                    title = "Auto Save",
                    checked = uiState.autoSave,
                    onCheckedChange = { viewModel.setAutoSave(it) }
                )

                SettingsSwitchItem(
                    title = "Auto Complete",
                    checked = uiState.autoComplete,
                    onCheckedChange = { viewModel.setAutoComplete(it) }
                )

                SettingsSwitchItem(
                    title = "Syntax Highlighting",
                    checked = uiState.syntaxHighlighting,
                    onCheckedChange = { viewModel.setSyntaxHighlighting(it) }
                )

                SettingsSwitchItem(
                    title = "Bracket Matching",
                    checked = uiState.bracketMatching,
                    onCheckedChange = { viewModel.setBracketMatching(it) }
                )

                SettingsSwitchItem(
                    title = "Indent Guides",
                    checked = uiState.indentGuides,
                    onCheckedChange = { viewModel.setIndentGuides(it) }
                )

                SettingsSwitchItem(
                    title = "Current Line Highlight",
                    checked = uiState.currentLineHighlight,
                    onCheckedChange = { viewModel.setCurrentLineHighlight(it) }
                )
            }

            SettingsSection(title = "Terminal", icon = Icons.Default.Terminal) {
                SettingsItem(
                    title = "Shell",
                    subtitle = uiState.shell,
                    onClick = { }
                )
                
                SettingsSwitchItem(
                    title = "Vibration",
                    checked = uiState.vibration,
                    onCheckedChange = { viewModel.setVibration(it) }
                )
            }

            SettingsSection(title = "File Manager", icon = Icons.Default.Storage) {
                SettingsSwitchItem(
                    title = "Show Hidden Files",
                    checked = uiState.showHiddenFiles,
                    onCheckedChange = { viewModel.setShowHiddenFiles(it) }
                )

                SettingsSwitchItem(
                    title = "Confirm Before Delete",
                    checked = uiState.confirmBeforeDelete,
                    onCheckedChange = { viewModel.setConfirmBeforeDelete(it) }
                )
            }

            SettingsSection(title = "About", icon = Icons.Default.Info) {
                SettingsItem(
                    title = "Version",
                    subtitle = "1.0.0",
                    onClick = { }
                )
                
                SettingsItem(
                    title = "License",
                    subtitle = "MIT",
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                content()
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsSliderItem(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = value.toInt().toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}
