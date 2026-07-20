package com.gymtracker.ui.screens.fatigue

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymtracker.R
import com.gymtracker.domain.MuscleLoad
import com.gymtracker.domain.MuscleLoadLevel
import com.gymtracker.domain.MuscleLoadSummary
import com.gymtracker.domain.MuscleGroup
import com.gymtracker.ui.components.GtEmptyState
import com.gymtracker.ui.components.GtErrorState
import com.gymtracker.ui.components.GtLoadingState
import com.gymtracker.ui.components.GtSectionHeader
import com.gymtracker.ui.components.GtTopAppBar
import com.gymtracker.ui.components.MuscleBodyMap
import com.gymtracker.ui.theme.GtSpacing

@Composable
fun FatigueScreen(
    onBack: () -> Unit,
    viewModel: FatigueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showMethodology by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            GtTopAppBar(
                title = stringResource(R.string.muscle_load_title),
                supportingText = stringResource(R.string.muscle_load_subtitle),
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showMethodology = true }) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = stringResource(R.string.muscle_load_methodology_action)
                        )
                    }
                }
            )
        }
    ) { padding ->
        MuscleLoadContent(
            uiState = uiState,
            onRetry = viewModel::retry,
            showHeader = false,
            modifier = Modifier.padding(padding)
        )
    }

    if (showMethodology) {
        MuscleLoadMethodologyDialog(onDismiss = { showMethodology = false })
    }
}

@Composable
fun MuscleLoadContent(
    uiState: MuscleLoadUiState,
    onRetry: () -> Unit,
    showHeader: Boolean = true,
    modifier: Modifier = Modifier
) {
    var showFront by remember { mutableStateOf(true) }
    var selectedMuscle by remember { mutableStateOf<MuscleGroup?>(null) }
    var showMethodology by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = GtSpacing.md)
            .testTag("muscle-load-content"),
        verticalArrangement = Arrangement.spacedBy(GtSpacing.md),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = GtSpacing.lg)
    ) {
        if (showHeader) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    GtSectionHeader(
                        title = stringResource(R.string.muscle_load_title),
                        supportingText = stringResource(R.string.muscle_load_subtitle),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showMethodology = true }) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = stringResource(R.string.muscle_load_methodology_action)
                        )
                    }
                }
            }
        }

        when (uiState) {
            MuscleLoadUiState.Loading -> item {
                GtLoadingState(stringResource(R.string.muscle_load_loading))
            }
            MuscleLoadUiState.Error -> item {
                GtErrorState(
                    title = stringResource(R.string.muscle_load_error_title),
                    body = stringResource(R.string.muscle_load_error_body),
                    retryLabel = stringResource(R.string.action_retry),
                    onRetry = onRetry
                )
            }
            is MuscleLoadUiState.Content -> {
                val summary = uiState.summary
                if (summary.completedSetCount == 0) {
                    item {
                        GtEmptyState(
                            title = stringResource(R.string.muscle_load_empty_title),
                            body = stringResource(R.string.muscle_load_empty_body)
                        )
                    }
                } else {
                    item { MuscleLoadSummaryCard(summary) }
                    item {
                        ViewSelector(
                            showFront = showFront,
                            onToggle = {
                                showFront = it
                                selectedMuscle = null
                            }
                        )
                    }
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(GtSpacing.md),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)
                            ) {
                                MuscleBodyMap(
                                    loadData = summary.muscles,
                                    showFront = showFront,
                                    selectedMuscle = selectedMuscle,
                                    accessibilityDescription = stringResource(
                                        if (showFront) R.string.muscle_load_map_front_description
                                        else R.string.muscle_load_map_back_description
                                    ),
                                    onMuscleTap = { tapped ->
                                        selectedMuscle = if (selectedMuscle == tapped) null else tapped
                                    },
                                    modifier = Modifier.fillMaxWidth(
                                        if (LocalDensity.current.fontScale >= 1.6f) 1f else 0.72f
                                    )
                                )
                                selectedMuscle?.let { muscle ->
                                    val load = summary.muscles.first { it.muscle == muscle }
                                    Text(
                                        text = stringResource(
                                            R.string.muscle_load_selected_value,
                                            muscle.localizedName(),
                                            levelLabel(load.level),
                                            load.loadScore
                                        ),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        }
                    }
                    item { LoadLegend() }
                    item {
                        GtSectionHeader(
                            title = stringResource(R.string.muscle_load_list_title),
                            supportingText = stringResource(R.string.muscle_load_list_support)
                        )
                    }
                    items(
                        summary.muscles.filter { it.weightedVolume > 0f }
                            .sortedByDescending(MuscleLoad::loadScore),
                        key = { it.muscle.name }
                    ) { load ->
                        MuscleLoadRow(
                            load = load,
                            selected = selectedMuscle == load.muscle,
                            onClick = { selectedMuscle = load.muscle }
                        )
                    }
                }
            }
        }
    }

    if (showMethodology) {
        MuscleLoadMethodologyDialog(onDismiss = { showMethodology = false })
    }
}

@Composable
private fun MuscleLoadMethodologyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.muscle_load_methodology_title)) },
        text = { Text(stringResource(R.string.muscle_load_methodology_body)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_ok))
            }
        }
    )
}

@Composable
private fun MuscleLoadSummaryCard(summary: MuscleLoadSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(GtSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(GtSpacing.xs)
        ) {
            Text(
                stringResource(R.string.muscle_load_completed_sets, summary.completedSetCount),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                stringResource(R.string.muscle_load_summary_support),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stringResource(
                    if (summary.usesPersonalBodyweight) {
                        R.string.muscle_load_personal_calibration
                    } else {
                        R.string.muscle_load_default_calibration
                    },
                    summary.bodyweightCalibrationKg
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            if (summary.unknownExerciseCount > 0) {
                Text(
                    stringResource(R.string.muscle_load_unknown_exercises, summary.unknownExerciseCount),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ViewSelector(showFront: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(GtSpacing.xs)
    ) {
        FilterChip(
            selected = showFront,
            onClick = { onToggle(true) },
            label = { Text(stringResource(R.string.muscle_load_front)) },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = !showFront,
            onClick = { onToggle(false) },
            label = { Text(stringResource(R.string.muscle_load_back)) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LoadLegend() {
    val largeFont = LocalDensity.current.fontScale >= 1.6f
    val content: @Composable () -> Unit = {
        MuscleLoadLevel.values().forEach { level ->
            Text(
                text = levelLabel(level),
                style = MaterialTheme.typography.labelLarge,
                color = levelColor(level)
            )
        }
    }
    if (largeFont) {
        Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.xs)) { content() }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) { content() }
    }
}

@Composable
private fun MuscleLoadRow(load: MuscleLoad, selected: Boolean, onClick: () -> Unit) {
    val description = stringResource(
        R.string.muscle_load_row_description,
        load.muscle.localizedName(),
        levelLabel(load.level),
        load.loadScore
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = description },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(GtSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(load.muscle.localizedName(), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(GtSpacing.sm))
            Column(horizontalAlignment = Alignment.End) {
                Text(levelLabel(load.level), color = levelColor(load.level), style = MaterialTheme.typography.labelLarge)
                Text(stringResource(R.string.muscle_load_score, load.loadScore), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun levelLabel(level: MuscleLoadLevel): String = stringResource(
    when (level) {
        MuscleLoadLevel.LOW -> R.string.muscle_load_low
        MuscleLoadLevel.MEDIUM -> R.string.muscle_load_medium
        MuscleLoadLevel.HIGH -> R.string.muscle_load_high
    }
)

@Composable
private fun levelColor(level: MuscleLoadLevel) = when (level) {
    MuscleLoadLevel.LOW -> MaterialTheme.colorScheme.tertiary
    MuscleLoadLevel.MEDIUM -> MaterialTheme.colorScheme.primary
    MuscleLoadLevel.HIGH -> MaterialTheme.colorScheme.error
}

@Composable
private fun MuscleGroup.localizedName(): String = stringResource(
    when (this) {
        MuscleGroup.UPPER_BACK -> R.string.muscle_upper_back
        MuscleGroup.LATS -> R.string.muscle_lats
        MuscleGroup.TRAPS -> R.string.muscle_traps
        MuscleGroup.FRONT_DELTS -> R.string.muscle_front_delts
        MuscleGroup.SIDE_DELTS -> R.string.muscle_side_delts
        MuscleGroup.REAR_DELTS -> R.string.muscle_rear_delts
        MuscleGroup.FOREARMS -> R.string.muscle_forearms
        MuscleGroup.ABS -> R.string.muscle_abs
        MuscleGroup.OBLIQUES -> R.string.muscle_obliques
        MuscleGroup.LOWER_BACK -> R.string.muscle_lower_back
        MuscleGroup.GLUTES -> R.string.muscle_glutes
        MuscleGroup.QUADS -> R.string.muscle_quads
        MuscleGroup.HAMSTRINGS -> R.string.muscle_hamstrings
        MuscleGroup.CALVES -> R.string.muscle_calves
        MuscleGroup.HIP_FLEXORS -> R.string.muscle_hip_flexors
        MuscleGroup.NECK -> R.string.muscle_neck
        MuscleGroup.SERRATUS -> R.string.muscle_serratus
        MuscleGroup.ADDUCTORS -> R.string.muscle_adductors
        MuscleGroup.GLUTE_MEDIUS -> R.string.muscle_glute_medius
        MuscleGroup.CHEST_UPPER -> R.string.muscle_chest_upper
        MuscleGroup.CHEST_MID -> R.string.muscle_chest_mid
        MuscleGroup.CHEST_LOWER -> R.string.muscle_chest_lower
        MuscleGroup.BICEPS_LONG_HEAD -> R.string.muscle_biceps_long
        MuscleGroup.BICEPS_SHORT_HEAD -> R.string.muscle_biceps_short
        MuscleGroup.BRACHIALIS -> R.string.muscle_brachialis
        MuscleGroup.TRICEPS_LATERAL -> R.string.muscle_triceps_lateral
        MuscleGroup.TRICEPS_LONG -> R.string.muscle_triceps_long
        MuscleGroup.TRICEPS_MEDIAL -> R.string.muscle_triceps_medial
        MuscleGroup.TERES -> R.string.muscle_teres
        MuscleGroup.INTERNAL_OBLIQUES -> R.string.muscle_internal_obliques
        MuscleGroup.TRANSVERSE_ABDOMINIS -> R.string.muscle_transverse_abdominis
        MuscleGroup.RHOMBOIDS -> R.string.muscle_rhomboids
    }
)
