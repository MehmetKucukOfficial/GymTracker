@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gymtracker.ui.screens.programs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymtracker.R
import com.gymtracker.data.entities.Program
import com.gymtracker.data.entities.ProgramDayExercise
import com.gymtracker.data.entities.ProgramDifficulty
import com.gymtracker.data.programs.ProgramWorkoutPlan
import com.gymtracker.data.programs.orderedExercises
import com.gymtracker.ui.components.GtEmptyState
import com.gymtracker.ui.components.GtErrorState
import com.gymtracker.ui.components.GtLoadingState
import com.gymtracker.ui.components.GtSectionHeader
import com.gymtracker.ui.theme.GtSize
import com.gymtracker.ui.theme.GtSpacing
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProgramsScreen(
    onProgramSelected: (Long) -> Unit,
    viewModel: ProgramsViewModel = hiltViewModel()
) {
    val state by viewModel.listState.collectAsStateWithLifecycle()
    var purchase by remember { mutableStateOf<Program?>(null) }

    when (val current = state) {
        ProgramsListUiState.Loading -> GtLoadingState(
            title = stringResource(R.string.programs_loading),
            modifier = Modifier.fillMaxSize()
        )
        ProgramsListUiState.Error -> Box(
            modifier = Modifier.fillMaxSize().padding(GtSpacing.md),
            contentAlignment = Alignment.Center
        ) {
            GtErrorState(
                title = stringResource(R.string.programs_error_title),
                body = stringResource(R.string.programs_error_body),
                retryLabel = stringResource(R.string.action_retry),
                onRetry = viewModel::retryPrograms
            )
        }
        is ProgramsListUiState.Content -> ProgramsContent(
            programs = current.programs,
            onProgramSelected = onProgramSelected,
            onPurchase = { purchase = it }
        )
    }

    purchase?.let { program ->
        PurchaseDialog(
            program = program,
            onDismiss = { purchase = null },
            onConfirm = {
                viewModel.purchaseProgram(program)
                purchase = null
            }
        )
    }
}

@Composable
fun ProgramsContent(
    programs: List<Program>,
    onProgramSelected: (Long) -> Unit,
    onPurchase: (Program) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = GtSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(GtSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            GtSectionHeader(
                title = stringResource(R.string.programs_title),
                supportingText = stringResource(R.string.programs_subtitle),
                modifier = Modifier.padding(top = GtSpacing.lg, bottom = GtSpacing.sm)
            )
        }
        if (programs.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                GtEmptyState(
                    title = stringResource(R.string.programs_empty_title),
                    body = stringResource(R.string.programs_empty_body)
                )
            }
        } else {
            items(programs, key = { it.id }) { program ->
                ProgramCard(
                    program = program,
                    onClick = { onProgramSelected(program.id) },
                    onPurchase = { onPurchase(program) }
                )
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(GtSpacing.lg)) }
    }
}

@Composable
private fun ProgramCard(program: Program, onClick: () -> Unit, onPurchase: () -> Unit) {
    val locked = program.isPaid && !program.isPurchased
    val access = stringResource(
        when {
            locked -> R.string.program_access_locked
            program.isPurchased -> R.string.program_access_purchased
            else -> R.string.program_access_free
        }
    )
    val description = stringResource(
        R.string.program_card_description,
        program.name,
        access,
        program.durationWeeks,
        program.daysPerWeek
    )
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = description
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(GtSpacing.md),
            verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GtSpacing.sm),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(program.name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        stringResource(R.string.program_creator, program.creatorName),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                ProgramAccessBadge(program)
            }
            Text(
                text = program.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(GtSpacing.xs)) {
                ProgramMetadataChip(
                    text = pluralStringResource(R.plurals.program_weeks, program.durationWeeks, program.durationWeeks),
                    icon = { Icon(Icons.Default.CalendarMonth, null, Modifier.size(GtSize.iconSmall)) }
                )
                ProgramMetadataChip(
                    text = pluralStringResource(R.plurals.program_days_week, program.daysPerWeek, program.daysPerWeek),
                    icon = { Icon(Icons.Default.FitnessCenter, null, Modifier.size(GtSize.iconSmall)) }
                )
            }
            DifficultyBadge(program.difficulty)
            if (locked) {
                Button(
                    onClick = onPurchase,
                    modifier = Modifier.fillMaxWidth().heightIn(min = GtSize.minimumTouchTarget)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(Modifier.width(GtSpacing.xs))
                    Text(stringResource(R.string.program_purchase_price, formatPrice(program.price)))
                }
            }
        }
    }
}

@Composable
private fun ProgramMetadataChip(text: String, icon: @Composable () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = GtSpacing.xs, vertical = GtSpacing.xxs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(Modifier.width(GtSpacing.xxs))
            Text(text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ProgramAccessBadge(program: Program) {
    val (icon, label) = when {
        program.isPaid && !program.isPurchased -> Icons.Default.Lock to R.string.program_access_locked
        program.isPurchased -> Icons.Default.CheckCircle to R.string.program_access_purchased
        else -> Icons.Default.CheckCircle to R.string.program_access_free
    }
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = GtSpacing.xs, vertical = GtSpacing.xxs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, Modifier.size(GtSize.iconSmall))
            Spacer(Modifier.width(GtSpacing.xxs))
            Text(stringResource(label), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun DifficultyBadge(difficulty: ProgramDifficulty) {
    val label = when (difficulty) {
        ProgramDifficulty.BEGINNER -> R.string.program_difficulty_beginner
        ProgramDifficulty.INTERMEDIATE -> R.string.program_difficulty_intermediate
        ProgramDifficulty.ADVANCED -> R.string.program_difficulty_advanced
    }
    Text(
        text = stringResource(R.string.program_difficulty_label, stringResource(label)),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun ProgramDetailScreen(
    onBack: () -> Unit,
    onOpenSession: (Long) -> Unit,
    viewModel: ProgramsViewModel = hiltViewModel()
) {
    val state by viewModel.detailState.collectAsStateWithLifecycle()
    var purchase by remember { mutableStateOf<Program?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val startFailedMessage = stringResource(R.string.program_start_failed)

    LaunchedEffect(state.message) {
        if (state.message == ProgramActionMessage.START_FAILED) {
            snackbarHostState.showSnackbar(
                message = startFailedMessage
            )
            viewModel.clearMessage()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        when {
            state.isLoading -> GtLoadingState(
                title = stringResource(R.string.program_detail_loading),
                modifier = Modifier.fillMaxSize().padding(padding)
            )
            state.program == null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(GtSpacing.md),
                contentAlignment = Alignment.Center
            ) {
                GtEmptyState(
                    title = stringResource(R.string.program_not_found_title),
                    body = stringResource(R.string.program_not_found_body),
                    actionLabel = stringResource(R.string.action_back),
                    onAction = onBack
                )
            }
            else -> ProgramDetailContent(
                program = requireNotNull(state.program),
                workouts = state.workouts,
                isStarting = state.isStarting,
                onBack = onBack,
                onPurchase = { purchase = state.program },
                onStartWorkout = { workoutId ->
                    viewModel.startWorkout(workoutId) { sessionId ->
                        viewModel.clearMessage()
                        onOpenSession(sessionId)
                    }
                },
                modifier = Modifier.padding(padding)
            )
        }
    }

    purchase?.let { program ->
        PurchaseDialog(
            program = program,
            onDismiss = { purchase = null },
            onConfirm = {
                viewModel.purchaseProgram(program)
                purchase = null
            }
        )
    }
}

@Composable
fun ProgramDetailContent(
    program: Program,
    workouts: List<ProgramWorkoutPlan>,
    isStarting: Boolean,
    onBack: () -> Unit,
    onPurchase: () -> Unit,
    onStartWorkout: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val locked = program.isPaid && !program.isPurchased
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = GtSpacing.md),
        verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = GtSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back))
                }
                GtSectionHeader(
                    title = program.name,
                    supportingText = stringResource(R.string.program_creator, program.creatorName),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(GtSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)
                ) {
                    Text(program.description, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        pluralStringResource(R.plurals.program_weeks, program.durationWeeks, program.durationWeeks),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        pluralStringResource(R.plurals.program_days_week, program.daysPerWeek, program.daysPerWeek),
                        style = MaterialTheme.typography.labelLarge
                    )
                    DifficultyBadge(program.difficulty)
                }
            }
        }
        if (locked) {
            item {
                GtEmptyState(
                    title = stringResource(R.string.program_locked_title),
                    body = stringResource(R.string.program_locked_body),
                    actionLabel = stringResource(R.string.program_purchase_price, formatPrice(program.price)),
                    onAction = onPurchase
                )
            }
        } else {
            item {
                GtSectionHeader(
                    title = stringResource(R.string.program_plan_title),
                    supportingText = stringResource(R.string.program_plan_repeat, program.durationWeeks)
                )
            }
            if (workouts.isEmpty()) {
                item {
                    GtEmptyState(
                        title = stringResource(R.string.program_plan_empty_title),
                        body = stringResource(R.string.program_plan_empty_body)
                    )
                }
            } else {
                items(workouts, key = { it.workout.id }) { plan ->
                    WorkoutDayCard(
                        plan = plan,
                        isStarting = isStarting,
                        onStart = { onStartWorkout(plan.workout.id) }
                    )
                }
            }
        }
        item { Spacer(Modifier.height(GtSpacing.lg)) }
    }
}

@Composable
private fun WorkoutDayCard(
    plan: ProgramWorkoutPlan,
    isStarting: Boolean,
    onStart: () -> Unit
) {
    var expanded by remember(plan.workout.id) { mutableStateOf(false) }
    val exercises = plan.orderedExercises
    val expandDescription = stringResource(
        if (expanded) R.string.program_collapse_exercises else R.string.program_expand_exercises
    )
    Card(
        modifier = Modifier.fillMaxWidth().testTag("program-day-${plan.workout.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(GtSpacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.program_day_number, plan.workout.dayNumber),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(plan.workout.dayName, style = MaterialTheme.typography.titleLarge)
                    Text(
                        pluralStringResource(R.plurals.program_exercises, exercises.size, exercises.size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.semantics {
                        contentDescription = expandDescription
                    }
                ) {
                    Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                }
            }
            AnimatedVisibility(expanded) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = GtSpacing.sm))
                    exercises.forEach { ExerciseRow(it) }
                }
            }
            Button(
                onClick = onStart,
                enabled = !isStarting && exercises.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = GtSpacing.sm)
                    .heightIn(min = GtSize.minimumTouchTarget)
            ) {
                if (isStarting) {
                    CircularProgressIndicator(Modifier.size(GtSize.iconSmall), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                }
                Spacer(Modifier.width(GtSpacing.xs))
                Text(stringResource(R.string.program_start_workout))
            }
        }
    }
}

@Composable
private fun ExerciseRow(exercise: ProgramDayExercise) {
    val description = stringResource(
        R.string.program_exercise_description,
        exercise.exerciseName,
        exercise.targetSets,
        exercise.targetReps,
        exercise.restSeconds
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = GtSpacing.xs)
            .semantics(mergeDescendants = true) {
                contentDescription = description
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(GtSpacing.sm)
    ) {
        Icon(Icons.Default.FitnessCenter, null, Modifier.size(GtSize.iconSmall), tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f)) {
            Text(exercise.exerciseName, style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(R.string.program_exercise_target, exercise.targetSets, exercise.targetReps),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, null, Modifier.size(GtSize.iconSmall))
            Spacer(Modifier.width(GtSpacing.xxs))
            Text(stringResource(R.string.program_rest_seconds, exercise.restSeconds))
        }
    }
}

@Composable
private fun PurchaseDialog(program: Program, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.program_purchase_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.xs)) {
                Text(stringResource(R.string.program_purchase_question, program.name, formatPrice(program.price)))
                Text(
                    stringResource(R.string.program_purchase_demo_notice),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text(stringResource(R.string.program_purchase_demo_action)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

@Composable
private fun formatPrice(price: Float): String = remember(price) {
    NumberFormat.getCurrencyInstance(Locale("tr", "TR")).format(price)
}
