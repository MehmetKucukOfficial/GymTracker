package com.gymtracker.ui.screens.settings

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
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import android.text.format.Formatter
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
    val context = LocalContext.current
    val createExport = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { destination ->
        destination?.let(viewModel::exportData)
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
        versionName = versionName,
        onThemeModeSelected = viewModel::setThemeMode,
        onExportData = {
            createExport.launch(context.getString(R.string.settings_export_filename))
        },
        onBack = onBack
    )
}

@Composable
fun SettingsContent(
    settings: AppSettings,
    versionName: String,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onBack: () -> Unit,
    dataExportState: DataExportState = DataExportState.Idle,
    onExportData: () -> Unit = {},
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
                state = dataExportState,
                onExport = onExportData
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
    state: DataExportState,
    onExport: () -> Unit
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
                enabled = state != DataExportState.Exporting,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings-export-action")
            ) {
                if (state == DataExportState.Exporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(GtSpacing.md),
                        strokeWidth = GtSpacing.xxs,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(GtSpacing.sm))
                }
                Text(
                    text = stringResource(
                        if (state == DataExportState.Exporting) {
                            R.string.settings_export_in_progress
                        } else {
                            R.string.settings_export_action
                        }
                    )
                )
            }

            when (state) {
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
                            state.summary.totalRecords,
                            state.summary.includedManagedPhotos,
                            Formatter.formatShortFileSize(context, state.summary.archiveBytes)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (state.summary.metadataOnlyLegacyPhotos > 0) {
                        Text(
                            text = stringResource(
                                R.string.settings_export_legacy_warning,
                                state.summary.metadataOnlyLegacyPhotos
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is DataExportState.Failure -> Text(
                    text = stringResource(
                        when (state.reason) {
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
