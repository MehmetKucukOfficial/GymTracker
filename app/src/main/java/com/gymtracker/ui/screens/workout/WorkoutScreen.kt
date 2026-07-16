@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gymtracker.ui.screens.workout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymtracker.R
import com.gymtracker.data.entities.WorkoutSession
import com.gymtracker.data.entities.WorkoutSet
import com.gymtracker.data.entities.WorkoutStatus
import com.gymtracker.domain.ExerciseDefinition
import com.gymtracker.ui.components.GtEmptyState
import com.gymtracker.ui.components.GtLoadingState
import com.gymtracker.ui.components.GtListDetailScaffold
import com.gymtracker.ui.components.GtSectionHeader
import com.gymtracker.ui.theme.GtSize
import com.gymtracker.ui.theme.GtSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WorkoutScreen(
    onOpenSession: (Long) -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val completedSessions by viewModel.completedSessions.collectAsStateWithLifecycle()
    var showNewSessionDialog by remember { mutableStateOf(false) }

    WorkoutListContent(
        activeSession = activeSession,
        completedSessions = completedSessions,
        onNewSession = {
            if (activeSession != null) onOpenSession(activeSession!!.id)
            else showNewSessionDialog = true
        },
        onOpenSession = onOpenSession
    )

    if (showNewSessionDialog) {
        NewSessionDialog(
            onDismiss = { showNewSessionDialog = false },
            onConfirm = { name ->
                viewModel.startOrResumeSession(name, onOpened = onOpenSession)
                showNewSessionDialog = false
            }
        )
    }
}

@Composable
fun WorkoutListContent(
    activeSession: WorkoutSession?,
    completedSessions: List<WorkoutSession>,
    onNewSession: () -> Unit,
    onOpenSession: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val sessions = listOfNotNull(activeSession) + completedSessions
    var selectedSessionId by rememberSaveable { mutableStateOf(sessions.firstOrNull()?.id) }
    val selectedSession = sessions.firstOrNull { it.id == selectedSessionId } ?: sessions.firstOrNull()

    GtListDetailScaffold(
        modifier = modifier,
        listPane = { expanded ->
            WorkoutListPane(
                activeSession = activeSession,
                completedSessions = completedSessions,
                selectedSessionId = selectedSession?.id.takeIf { expanded },
                onNewSession = onNewSession,
                onOpenSession = { session ->
                    if (expanded) selectedSessionId = session.id
                    else onOpenSession(session.id)
                }
            )
        },
        detailPane = {
            if (selectedSession == null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(GtSpacing.md),
                    contentAlignment = Alignment.Center
                ) {
                    GtEmptyState(
                        title = stringResource(R.string.workout_empty_title),
                        body = stringResource(R.string.workout_empty_body),
                        actionLabel = stringResource(R.string.workout_empty_action),
                        onAction = onNewSession
                    )
                }
            } else {
                WorkoutPreviewPane(
                    session = selectedSession,
                    onOpenDetails = { onOpenSession(selectedSession.id) }
                )
            }
        }
    )
}

@Composable
private fun WorkoutListPane(
    activeSession: WorkoutSession?,
    completedSessions: List<WorkoutSession>,
    selectedSessionId: Long?,
    onNewSession: () -> Unit,
    onOpenSession: (WorkoutSession) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = GtSpacing.md, vertical = GtSpacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GtSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GtSectionHeader(
                title = stringResource(R.string.workout_title),
                supportingText = stringResource(R.string.workout_subtitle),
                modifier = Modifier.weight(1f)
            )
            FloatingActionButton(
                onClick = onNewSession,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(GtSize.minimumTouchTarget)
            ) {
                Icon(
                    imageVector = if (activeSession == null) Icons.Default.Add else Icons.Default.PlayArrow,
                    contentDescription = stringResource(
                        if (activeSession == null) R.string.workout_new_content_description
                        else R.string.workout_resume_content_description
                    )
                )
            }
        }

        Spacer(Modifier.height(GtSpacing.lg))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)
        ) {
            if (activeSession != null) {
                item {
                    GtSectionHeader(
                        title = stringResource(R.string.workout_active_section),
                        supportingText = stringResource(R.string.workout_active_section_support)
                    )
                }
                item {
                    SessionCard(
                        session = activeSession,
                        isActive = true,
                        selected = activeSession.id == selectedSessionId,
                        onClick = { onOpenSession(activeSession) },
                        modifier = Modifier.testTag("workout-active-card")
                    )
                }
                item { Spacer(Modifier.height(GtSpacing.sm)) }
            }

            if (activeSession == null && completedSessions.isEmpty()) {
                item {
                    GtEmptyState(
                        title = stringResource(R.string.workout_empty_title),
                        body = stringResource(R.string.workout_empty_body),
                        actionLabel = stringResource(R.string.workout_empty_action),
                        onAction = onNewSession
                    )
                }
            } else {
                item {
                    GtSectionHeader(
                        title = stringResource(R.string.workout_history_section),
                        supportingText = stringResource(R.string.workout_history_section_support),
                        modifier = Modifier.testTag("workout-history-section")
                    )
                }
                if (completedSessions.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.workout_no_completed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = GtSpacing.sm)
                        )
                    }
                } else {
                    items(completedSessions, key = { it.id }) { session ->
                        SessionCard(
                            session = session,
                            isActive = false,
                            selected = session.id == selectedSessionId,
                            onClick = { onOpenSession(session) },
                            modifier = Modifier.testTag("workout-session-card-${session.id}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionCard(
    session: WorkoutSession,
    isActive: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = remember { SimpleDateFormat("dd MMM yyyy - HH:mm", Locale("tr")) }
    val largeFont = LocalDensity.current.fontScale >= 1.6f
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, role = Role.Button)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                this.selected = selected
            },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer
            else if (isActive) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        if (largeFont) {
            Column(
                modifier = Modifier.padding(GtSpacing.md),
                verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)
            ) {
                SessionIdentity(
                    session = session,
                    isActive = isActive,
                    formattedDate = formatter.format(Date(session.date))
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SessionStatus(isActive = isActive)
                }
            }
        } else {
            Row(
                modifier = Modifier.padding(GtSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SessionIdentity(
                    session = session,
                    isActive = isActive,
                    formattedDate = formatter.format(Date(session.date)),
                    modifier = Modifier.weight(1f)
                )
                SessionStatus(isActive = isActive)
            }
        }
    }
}

@Composable
private fun WorkoutPreviewPane(
    session: WorkoutSession,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = session.status == WorkoutStatus.ACTIVE
    val formatter = remember { SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault()) }
    Surface(
        modifier = modifier.fillMaxSize().testTag("workout-preview-pane"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(GtSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(GtSpacing.md)
        ) {
            GtSectionHeader(
                title = stringResource(R.string.workout_preview_title),
                supportingText = stringResource(R.string.workout_preview_support)
            )
            Text(session.name, style = MaterialTheme.typography.headlineSmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                SessionStatus(isActive = isActive)
            }
            Text(
                text = stringResource(
                    R.string.workout_preview_date,
                    formatter.format(Date(session.date))
                ),
                style = MaterialTheme.typography.bodyLarge
            )
            if (session.durationMinutes > 0) {
                Text(
                    text = pluralStringResource(
                        R.plurals.workout_duration_minutes,
                        session.durationMinutes,
                        session.durationMinutes
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (session.notes.isNotBlank()) {
                Text(session.notes, style = MaterialTheme.typography.bodyMedium)
            }
            Button(
                onClick = onOpenDetails,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = GtSize.minimumTouchTarget)
                    .testTag("workout-open-detail")
            ) {
                Text(
                    stringResource(
                        if (isActive) R.string.workout_continue
                        else R.string.adaptive_open_full_details
                    )
                )
            }
        }
    }
}

@Composable
private fun SessionIdentity(
    session: WorkoutSession,
    isActive: Boolean,
    formattedDate: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(GtSize.minimumTouchTarget)
                .background(
                    color = if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.PlayArrow else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isActive) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.width(GtSpacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = session.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (session.durationMinutes > 0) {
                Text(
                    text = pluralStringResource(
                        R.plurals.workout_duration_minutes,
                        session.durationMinutes,
                        session.durationMinutes
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SessionStatus(isActive: Boolean) {
    Text(
        text = stringResource(
            if (isActive) R.string.workout_continue else R.string.workout_completed
        ),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(Modifier.width(GtSpacing.xs))
    Icon(
        Icons.Default.ChevronRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun NewSessionDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.workout_new_dialog_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.workout_name_label)) },
                placeholder = { Text(stringResource(R.string.workout_name_placeholder)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = { onConfirm(name) }
            ) { Text(stringResource(R.string.workout_start)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

@Composable
fun WorkoutDetailScreen(
    onBack: () -> Unit,
    onFinished: () -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddSetDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var plannedSet by remember { mutableStateOf<WorkoutSet?>(null) }

    when {
        uiState.isLoading -> GtLoadingState(
            title = stringResource(R.string.workout_loading),
            modifier = Modifier.fillMaxSize()
        )

        uiState.activeSession == null -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(GtSpacing.md),
            contentAlignment = Alignment.Center
        ) {
            GtEmptyState(
                title = stringResource(R.string.workout_not_found),
                body = stringResource(R.string.workout_not_found),
                actionLabel = stringResource(R.string.action_back_long),
                onAction = onBack
            )
        }

        else -> WorkoutDetailContent(
            session = requireNotNull(uiState.activeSession),
            sets = uiState.activeSets,
            onBack = onBack,
            onFinishRequest = { showFinishDialog = true },
            onAddSetRequest = { showAddSetDialog = true },
            onDeleteSet = viewModel::deleteSet,
            onCompletePlannedSet = { plannedSet = it }
        )
    }

    if (showAddSetDialog && uiState.activeSession?.status == WorkoutStatus.ACTIVE) {
        AddSetDialog(
            categories = viewModel.getExerciseCategories(),
            exercisesForCategory = viewModel::getExercisesForCategory,
            onDismiss = { showAddSetDialog = false },
            onConfirm = { exercise, setNumber, reps, weight ->
                viewModel.addSet(exercise, setNumber, reps, weight)
                showAddSetDialog = false
            }
        )
    }

    if (showFinishDialog && uiState.activeSession?.status == WorkoutStatus.ACTIVE) {
        FinishSessionDialog(
            onDismiss = { showFinishDialog = false },
            onConfirm = { duration, notes ->
                viewModel.finishSession(duration, notes, onFinished)
                showFinishDialog = false
            }
        )
    }

    plannedSet?.let { set ->
        CompletePlannedSetDialog(
            set = set,
            onDismiss = { plannedSet = null },
            onConfirm = { reps, weight ->
                viewModel.completePlannedSet(set.id, reps, weight)
                plannedSet = null
            }
        )
    }

    uiState.mutationError?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::clearMutationError,
            title = { Text(stringResource(R.string.workout_title)) },
            text = { Text(error.userMessage()) },
            confirmButton = {
                TextButton(onClick = viewModel::clearMutationError) {
                    Text(stringResource(R.string.action_ok))
                }
            }
        )
    }
}

@Composable
fun WorkoutDetailContent(
    session: WorkoutSession,
    sets: List<WorkoutSet>,
    onBack: () -> Unit,
    onFinishRequest: () -> Unit,
    onAddSetRequest: () -> Unit,
    onDeleteSet: (WorkoutSet) -> Unit,
    modifier: Modifier = Modifier,
    onCompletePlannedSet: (WorkoutSet) -> Unit = {}
) {
    val isActive = session.status == WorkoutStatus.ACTIVE
    val largeFont = LocalDensity.current.fontScale >= 1.6f
    val groupedSets = sets.groupBy { it.exerciseName }
    val hasIncompleteSets = sets.any { !it.isCompleted }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = GtSpacing.md, vertical = GtSpacing.sm)
            .testTag(if (isActive) "workout-detail-active" else "workout-detail-read-only")
    ) {
        if (largeFont) {
            Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.xs)) {
                WorkoutDetailTitle(session = session, isActive = isActive, onBack = onBack)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    WorkoutStatusBadge(isActive = isActive)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WorkoutDetailTitle(
                    session = session,
                    isActive = isActive,
                    onBack = onBack,
                    modifier = Modifier.weight(1f)
                )
                WorkoutStatusBadge(isActive = isActive)
            }
        }

        if (isActive) {
            Spacer(Modifier.height(GtSpacing.sm))
            Button(
                onClick = onFinishRequest,
                enabled = !hasIncompleteSets,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = GtSize.minimumTouchTarget)
                    .testTag("workout-finish-action")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(GtSpacing.xs))
                Text(stringResource(R.string.workout_finish))
            }
            if (hasIncompleteSets) {
                Text(
                    text = stringResource(R.string.workout_finish_planned_sets_first),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(GtSpacing.md))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)
        ) {
            if (groupedSets.isEmpty()) {
                item {
                    GtEmptyState(
                        title = stringResource(
                            if (isActive) R.string.workout_add_set else R.string.workout_detail_summary
                        ),
                        body = stringResource(
                            if (isActive) R.string.workout_no_sets_active
                            else R.string.workout_no_sets_completed
                        ),
                        actionLabel = if (isActive) stringResource(R.string.workout_add_set) else null,
                        onAction = if (isActive) onAddSetRequest else null
                    )
                }
            } else {
                groupedSets.forEach { (exerciseName, exerciseSets) ->
                    item(key = exerciseName) {
                        ExerciseSetGroup(
                            exerciseName = exerciseName,
                            sets = exerciseSets,
                            editable = isActive,
                            onDeleteSet = onDeleteSet,
                            onCompletePlannedSet = onCompletePlannedSet
                        )
                    }
                }
            }
        }

        if (isActive && groupedSets.isNotEmpty()) {
            Button(
                onClick = onAddSetRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = GtSpacing.xs)
                    .heightIn(min = GtSize.minimumTouchTarget)
                    .testTag("workout-add-set-action"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(GtSpacing.xs))
                Text(stringResource(R.string.workout_add_set))
            }
        }
    }
}

@Composable
private fun WorkoutDetailTitle(
    session: WorkoutSession,
    isActive: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_back)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = session.name, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = stringResource(
                    if (isActive) R.string.workout_detail_active
                    else R.string.workout_detail_summary
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WorkoutStatusBadge(isActive: Boolean) {
    Surface(
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = stringResource(if (isActive) R.string.workout_active else R.string.workout_completed),
            style = MaterialTheme.typography.labelMedium,
            color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = GtSpacing.sm, vertical = GtSpacing.xs)
        )
    }
}

@Composable
private fun ExerciseSetGroup(
    exerciseName: String,
    sets: List<WorkoutSet>,
    editable: Boolean,
    onDeleteSet: (WorkoutSet) -> Unit,
    onCompletePlannedSet: (WorkoutSet) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(GtSpacing.md),
            verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)
        ) {
            Text(
                text = exerciseName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            sets.forEach { set ->
                WorkoutSetRow(
                    set = set,
                    editable = editable,
                    onDelete = { onDeleteSet(set) },
                    onComplete = { onCompletePlannedSet(set) }
                )
            }
        }
    }
}

@Composable
private fun WorkoutSetRow(
    set: WorkoutSet,
    editable: Boolean,
    onDelete: () -> Unit,
    onComplete: () -> Unit
) {
    val largeFont = LocalDensity.current.fontScale >= 1.6f
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.small
    ) {
        if (!set.isCompleted) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(GtSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(GtSpacing.xs)
            ) {
                Text(
                    text = stringResource(R.string.workout_planned_set, set.setNumber),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(
                        R.string.workout_planned_target,
                        set.targetReps ?: stringResource(R.string.workout_planned_target_unspecified)
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (editable) {
                    Row(horizontalArrangement = Arrangement.spacedBy(GtSpacing.xs)) {
                        Button(onClick = onComplete) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(Modifier.width(GtSpacing.xs))
                            Text(stringResource(R.string.workout_complete_planned_set))
                        }
                        TextButton(onClick = onDelete) {
                            Text(stringResource(R.string.action_delete))
                        }
                    }
                }
            }
        } else if (largeFont) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(GtSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(GtSpacing.xs)
            ) {
                Text(stringResource(R.string.workout_set_value, set.setNumber))
                Text(stringResource(R.string.workout_reps_value, set.reps))
                Text(stringResource(R.string.workout_weight_value, set.weightKg))
                if (editable) {
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(GtSpacing.xs))
                        Text(stringResource(R.string.action_delete))
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(GtSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GtSpacing.sm)
            ) {
                MetricValue(
                    label = stringResource(R.string.workout_set_label),
                    value = set.setNumber.toString(),
                    modifier = Modifier.weight(1f)
                )
                MetricValue(
                    label = stringResource(R.string.workout_reps_label),
                    value = set.reps.toString(),
                    modifier = Modifier.weight(1f)
                )
                MetricValue(
                    label = stringResource(R.string.workout_weight_label),
                    value = stringResource(R.string.workout_weight_value, set.weightKg),
                    modifier = Modifier.weight(1.4f)
                )
                if (editable) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.action_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletePlannedSetDialog(
    set: WorkoutSet,
    onDismiss: () -> Unit,
    onConfirm: (Int, Float) -> Unit
) {
    var reps by remember(set.id) { mutableStateOf("") }
    var weight by remember(set.id) { mutableStateOf("0") }
    val repsValue = reps.toIntOrNull()
    val weightValue = weight.replace(',', '.').toFloatOrNull()
    val valid = repsValue != null && repsValue > 0 &&
        weightValue != null && weightValue.isFinite() && weightValue >= 0f
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.workout_complete_planned_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.xs)) {
                Text(stringResource(R.string.workout_planned_target, set.targetReps.orEmpty()))
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text(stringResource(R.string.workout_reps_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(stringResource(R.string.workout_weight_kg)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = valid,
                onClick = { onConfirm(requireNotNull(repsValue), requireNotNull(weightValue)) }
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

@Composable
private fun MetricValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun AddSetDialog(
    categories: List<String>,
    exercisesForCategory: (String) -> List<ExerciseDefinition>,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int, Float) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull().orEmpty()) }
    var exercisesInCategory by remember { mutableStateOf(exercisesForCategory(selectedCategory)) }
    var selectedExercise by remember { mutableStateOf(exercisesInCategory.firstOrNull()) }
    var setNumber by remember { mutableStateOf("1") }
    var reps by remember { mutableStateOf("10") }
    var weight by remember { mutableStateOf("0") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var exerciseExpanded by remember { mutableStateOf(false) }
    val setNumberValue = setNumber.toIntOrNull()
    val repsValue = reps.toIntOrNull()
    val weightValue = weight.toFloatOrNull()
    val isValid = selectedExercise != null && setNumberValue != null && setNumberValue > 0 &&
        repsValue != null && repsValue > 0 && weightValue != null && weightValue >= 0f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.workout_add_set_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.xs)) {
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.workout_muscle_group)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    exercisesInCategory = exercisesForCategory(category)
                                    selectedExercise = exercisesInCategory.firstOrNull()
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = exerciseExpanded,
                    onExpandedChange = { exerciseExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedExercise?.name.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.workout_exercise)) },
                        supportingText = selectedExercise?.let { exercise ->
                            { Text(exercise.equipment) }
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(exerciseExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = exerciseExpanded,
                        onDismissRequest = { exerciseExpanded = false }
                    ) {
                        exercisesInCategory.forEach { exercise ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(exercise.name)
                                        Text(
                                            exercise.equipment,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedExercise = exercise
                                    exerciseExpanded = false
                                }
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(GtSpacing.xs)) {
                    OutlinedTextField(
                        value = setNumber,
                        onValueChange = { setNumber = it },
                        label = { Text(stringResource(R.string.workout_set_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it },
                        label = { Text(stringResource(R.string.workout_reps_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(stringResource(R.string.workout_weight_kg)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = {
                    onConfirm(
                        requireNotNull(selectedExercise).name,
                        requireNotNull(setNumberValue),
                        requireNotNull(repsValue),
                        requireNotNull(weightValue)
                    )
                }
            ) { Text(stringResource(R.string.action_add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

@Composable
private fun FinishSessionDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var duration by remember { mutableStateOf("60") }
    var notes by remember { mutableStateOf("") }
    val durationValue = duration.toIntOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.workout_finish_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.xs)) {
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text(stringResource(R.string.workout_duration_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.workout_notes_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                enabled = durationValue != null && durationValue >= 0,
                onClick = { onConfirm(requireNotNull(durationValue), notes) }
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

@Composable
private fun WorkoutMutationError.userMessage(): String = stringResource(
    when (this) {
        WorkoutMutationError.INVALID_SET -> R.string.workout_error_invalid_set
        WorkoutMutationError.READ_ONLY_SESSION -> R.string.workout_error_read_only
        WorkoutMutationError.FINISH_FAILED -> R.string.workout_error_finish
    }
)
