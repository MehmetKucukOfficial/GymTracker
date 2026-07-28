package com.gymtracker.ui.screens.settings

import android.text.format.Formatter
import androidx.annotation.StringRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymtracker.R
import com.gymtracker.data.settings.AppSettings
import com.gymtracker.data.settings.ThemeMode
import com.gymtracker.ui.components.GtSectionHeader
import com.gymtracker.ui.components.GtTopAppBar
import com.gymtracker.ui.theme.GtSpacing

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val dataExportState by viewModel.dataExportState.collectAsStateWithLifecycle()
    val dataRestoreState by viewModel.dataRestoreState.collectAsStateWithLifecycle()
    val dataDeletionState by viewModel.dataDeletionState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingRestore by remember { mutableStateOf<android.net.Uri?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var deletionPhrase by remember { mutableStateOf("") }
    val createExport = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { destination ->
        destination?.let(viewModel::exportData)
    }
    val openRestore = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { source ->
        pendingRestore = source
    }
    val versionName = remember(context) {
        context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName
            .orEmpty()
            .ifBlank { "—" }
    }
    SettingsContent(
        settings = settings,
        dataExportState = dataExportState,
        dataRestoreState = dataRestoreState,
        dataDeletionState = dataDeletionState,
        versionName = versionName,
        onThemeModeSelected = viewModel::setThemeMode,
        onExportData = {
            createExport.launch(context.getString(R.string.settings_export_filename))
        },
        onChooseRestore = {
            openRestore.launch(arrayOf("application/zip", "application/octet-stream"))
        },
        onRequestDelete = {
            deletionPhrase = ""
            showDeleteConfirmation = true
        },
        onBack = onBack
    )
    pendingRestore?.let { source ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text(stringResource(R.string.settings_restore_confirm_title)) },
            text = { Text(stringResource(R.string.settings_restore_confirm_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        pendingRestore = null
                        viewModel.restoreData(source)
                    }
                ) {
                    Text(stringResource(R.string.settings_restore_confirm_action))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingRestore = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
    if (showDeleteConfirmation) {
        DeleteConfirmationDialog(
            value = deletionPhrase,
            onValueChange = { deletionPhrase = it },
            onConfirm = {
                showDeleteConfirmation = false
                deletionPhrase = ""
                viewModel.deleteAllData()
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }
}

@Composable
internal fun DeleteConfirmationDialog(
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val expectedPhrase = stringResource(R.string.settings_delete_confirmation_phrase)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_delete_confirm_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)) {
                Text(stringResource(R.string.settings_delete_confirm_body))
                Text(
                    stringResource(
                        R.string.settings_delete_confirm_instruction,
                        expectedPhrase
                    )
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    label = { Text(stringResource(R.string.settings_delete_confirm_field)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings-delete-confirmation")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = value == expectedPhrase,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.testTag("settings-delete-confirm-action")
            ) {
                Text(stringResource(R.string.settings_delete_confirm_action))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
fun SettingsContent(
    settings: AppSettings,
    versionName: String,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onBack: () -> Unit,
    dataExportState: DataExportState = DataExportState.Idle,
    dataRestoreState: DataRestoreState = DataRestoreState.Idle,
    dataDeletionState: DataDeletionState = DataDeletionState.Idle,
    onExportData: () -> Unit = {},
    onChooseRestore: () -> Unit = {},
    onRequestDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            GtTopAppBar(
                title = stringResource(R.string.settings_title),
                supportingText = stringResource(R.string.settings_supporting),
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(GtSpacing.md),
            verticalArrangement = Arrangement.spacedBy(GtSpacing.lg)
        ) {
            GtSectionHeader(
                title = stringResource(R.string.settings_appearance_title),
                supportingText = stringResource(R.string.settings_appearance_supporting)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(GtSpacing.xs)
            ) {
                ThemeOption.entries.forEach { option ->
                    ThemeOptionRow(
                        option = option,
                        selected = settings.themeMode == option.mode,
                        onClick = { onThemeModeSelected(option.mode) }
                    )
                }
            }

            GtSectionHeader(
                title = stringResource(R.string.settings_data_title),
                supportingText = stringResource(R.string.settings_data_supporting)
            )
            InformationCard(
                icon = Icons.Default.Lock,
                title = stringResource(R.string.settings_local_first_title),
                body = stringResource(R.string.settings_local_first_body),
                testTag = "settings-local-first"
            )
            DataExportCard(
                exportState = dataExportState,
                restoreState = dataRestoreState,
                deletionState = dataDeletionState,
                onExport = onExportData,
                onChooseRestore = onChooseRestore
            )
            DeleteDataCard(
                state = dataDeletionState,
                exportState = dataExportState,
                restoreState = dataRestoreState,
                onRequestDelete = onRequestDelete
            )

            GtSectionHeader(title = stringResource(R.string.settings_about_title))
            InformationCard(
                icon = Icons.Default.Info,
                title = stringResource(R.string.app_name),
                body = stringResource(R.string.settings_version, versionName),
                testTag = "settings-version"
            )
        }
    }
}

@Composable
private fun DataExportCard(
    exportState: DataExportState,
    restoreState: DataRestoreState,
    deletionState: DataDeletionState,
    onExport: () -> Unit,
    onChooseRestore: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings-data-export"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(GtSpacing.md),
            verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(GtSpacing.sm))
                Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.xs)) {
                    Text(
                        text = stringResource(R.string.settings_export_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.settings_export_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onExport,
                enabled = exportState != DataExportState.Exporting &&
                    restoreState != DataRestoreState.Restoring &&
                    deletionState != DataDeletionState.Deleting,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings-export-action")
            ) {
                if (exportState == DataExportState.Exporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(GtSpacing.md),
                        strokeWidth = GtSpacing.xxs,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(GtSpacing.sm))
                }
                Text(
                    text = stringResource(
                        if (exportState == DataExportState.Exporting) {
                            R.string.settings_export_in_progress
                        } else {
                            R.string.settings_export_action
                        }
                    )
                )
            }

            when (exportState) {
                DataExportState.Idle -> Unit
                DataExportState.Exporting -> Text(
                    text = stringResource(R.string.settings_export_do_not_close),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                is DataExportState.Success -> {
                    Text(
                        text = stringResource(
                            R.string.settings_export_success,
                            exportState.summary.totalRecords,
                            exportState.summary.includedManagedPhotos,
                            Formatter.formatShortFileSize(context, exportState.summary.archiveBytes)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (exportState.summary.metadataOnlyLegacyPhotos > 0) {
                        Text(
                            text = stringResource(
                                R.string.settings_export_legacy_warning,
                                exportState.summary.metadataOnlyLegacyPhotos
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is DataExportState.Failure -> Text(
                    text = stringResource(
                        when (exportState.reason) {
                            DataExportFailure.INSUFFICIENT_STORAGE ->
                                R.string.settings_export_error_storage
                            DataExportFailure.MISSING_MANAGED_PHOTO ->
                                R.string.settings_export_error_missing_photo
                            DataExportFailure.GENERIC ->
                                R.string.settings_export_error_generic
                        }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            HorizontalDivider()
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(GtSpacing.sm))
                Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.xs)) {
                    Text(
                        text = stringResource(R.string.settings_restore_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.settings_restore_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            OutlinedButton(
                onClick = onChooseRestore,
                enabled = restoreState != DataRestoreState.Restoring &&
                    exportState != DataExportState.Exporting &&
                    deletionState != DataDeletionState.Deleting,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings-restore-action")
            ) {
                if (restoreState == DataRestoreState.Restoring) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(GtSpacing.md),
                        strokeWidth = GtSpacing.xxs
                    )
                    Spacer(Modifier.width(GtSpacing.sm))
                }
                Text(
                    stringResource(
                        if (restoreState == DataRestoreState.Restoring) {
                            R.string.settings_restore_in_progress
                        } else {
                            R.string.settings_restore_action
                        }
                    )
                )
            }
            when (restoreState) {
                DataRestoreState.Idle -> Unit
                DataRestoreState.Restoring -> Text(
                    text = stringResource(R.string.settings_restore_do_not_close),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                is DataRestoreState.Success -> {
                    Text(
                        text = stringResource(
                            R.string.settings_restore_success,
                            restoreState.summary.totalRecords,
                            restoreState.summary.restoredManagedPhotos
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (restoreState.summary.metadataOnlyLegacyPhotos > 0) {
                        Text(
                            text = stringResource(
                                R.string.settings_restore_legacy_warning,
                                restoreState.summary.metadataOnlyLegacyPhotos
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (restoreState.summary.oldMediaCleanupFailures > 0) {
                        Text(
                            text = stringResource(R.string.settings_restore_cleanup_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is DataRestoreState.Failure -> Text(
                    text = stringResource(
                        when (restoreState.reason) {
                            DataRestoreFailure.INSUFFICIENT_STORAGE ->
                                R.string.settings_restore_error_storage
                            DataRestoreFailure.INVALID_ARCHIVE ->
                                R.string.settings_restore_error_invalid
                            DataRestoreFailure.ROLLBACK_FAILED ->
                                R.string.settings_restore_error_rollback
                            DataRestoreFailure.GENERIC ->
                                R.string.settings_restore_error_generic
                        }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DeleteDataCard(
    state: DataDeletionState,
    exportState: DataExportState,
    restoreState: DataRestoreState,
    onRequestDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("settings-delete-data"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(GtSpacing.md),
            verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(GtSpacing.sm))
                Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.xs)) {
                    Text(
                        text = stringResource(R.string.settings_delete_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = stringResource(R.string.settings_delete_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            Button(
                onClick = onRequestDelete,
                enabled = state != DataDeletionState.Deleting &&
                    exportState != DataExportState.Exporting &&
                    restoreState != DataRestoreState.Restoring,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings-delete-action")
            ) {
                if (state == DataDeletionState.Deleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(GtSpacing.md),
                        strokeWidth = GtSpacing.xxs,
                        color = MaterialTheme.colorScheme.onError
                    )
                    Spacer(Modifier.width(GtSpacing.sm))
                }
                Text(
                    stringResource(
                        if (state == DataDeletionState.Deleting) {
                            R.string.settings_delete_in_progress
                        } else {
                            R.string.settings_delete_action
                        }
                    )
                )
            }
            when (state) {
                DataDeletionState.Idle -> Unit
                DataDeletionState.Deleting -> Text(
                    text = stringResource(R.string.settings_delete_do_not_close),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                is DataDeletionState.Success -> {
                    Text(
                        text = stringResource(
                            R.string.settings_delete_success,
                            state.summary.deletedRecords,
                            state.summary.deletedManagedFiles
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    if (state.summary.managedFileCleanupFailures > 0) {
                        Text(
                            text = stringResource(R.string.settings_delete_cleanup_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is DataDeletionState.Failure -> Text(
                    text = stringResource(
                        if (state.reason == DataDeletionFailure.ROLLBACK_FAILED) {
                            R.string.settings_delete_error_rollback
                        } else {
                            R.string.settings_delete_error_generic
                        }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private enum class ThemeOption(
    val mode: ThemeMode,
    @StringRes val labelRes: Int,
    @StringRes val supportingRes: Int
) {
    SYSTEM(ThemeMode.SYSTEM, R.string.settings_theme_system, R.string.settings_theme_system_support),
    LIGHT(ThemeMode.LIGHT, R.string.settings_theme_light, R.string.settings_theme_light_support),
    DARK(ThemeMode.DARK, R.string.settings_theme_dark, R.string.settings_theme_dark_support)
}

@Composable
private fun ThemeOptionRow(
    option: ThemeOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .testTag("settings-theme-${option.mode.name.lowercase()}"),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(GtSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = null)
            Spacer(Modifier.width(GtSpacing.sm))
            Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.xxs)) {
                Text(
                    text = stringResource(option.labelRes),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(option.supportingRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InformationCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(GtSpacing.md),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(GtSpacing.sm))
            Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.xs)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
