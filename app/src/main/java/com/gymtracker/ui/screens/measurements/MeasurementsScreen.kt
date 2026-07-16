package com.gymtracker.ui.screens.measurements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymtracker.R
import com.gymtracker.data.entities.BodyMeasurement
import com.gymtracker.data.measurements.MeasurementValidationError
import com.gymtracker.data.measurements.MeasurementChartPeriod
import com.gymtracker.data.measurements.WeightTrend
import com.gymtracker.data.measurements.validateMeasurement
import com.gymtracker.domain.parseLocalizedDecimal
import com.gymtracker.ui.components.GtEmptyState
import com.gymtracker.ui.components.GtSectionHeader
import com.gymtracker.ui.theme.GtSize
import com.gymtracker.ui.theme.GtSpacing
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MeasurementsScreen(viewModel: MeasurementsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val validationError by viewModel.validationError.collectAsStateWithLifecycle()
    val deletedMeasurement by viewModel.deletedMeasurement.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMeasurement by remember { mutableStateOf<BodyMeasurement?>(null) }

    androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
        MeasurementsContent(
            uiState = uiState,
            onAddMeasurement = { showAddDialog = true },
            onDeleteMeasurement = viewModel::deleteMeasurement,
            onEditMeasurement = { editingMeasurement = it },
            onSelectChartPeriod = viewModel::selectChartPeriod
        )
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }

    val deletedMessage = stringResource(R.string.measurements_deleted)
    val undoLabel = stringResource(R.string.action_undo)
    LaunchedEffect(deletedMeasurement) {
        deletedMeasurement?.let { measurement ->
            val result = snackbarHostState.showSnackbar(deletedMessage, undoLabel)
            if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete(measurement)
            else viewModel.consumeDeletedMeasurement(measurement)
        }
    }

    if (showAddDialog) {
        MeasurementDialog(
            existing = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { measurement ->
                viewModel.saveMeasurement(measurement) { showAddDialog = false }
            }
        )
    }
    editingMeasurement?.let { measurement ->
        MeasurementDialog(
            existing = measurement,
            onDismiss = { editingMeasurement = null },
            onConfirm = { updated ->
                viewModel.updateMeasurement(updated) { editingMeasurement = null }
            }
        )
    }

    validationError?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::clearValidationError,
            title = { Text(stringResource(R.string.measurements_title)) },
            text = { Text(error.userMessage()) },
            confirmButton = {
                TextButton(onClick = viewModel::clearValidationError) {
                    Text(stringResource(R.string.action_ok))
                }
            }
        )
    }
}

@Composable
fun MeasurementsContent(
    uiState: MeasurementsUiState,
    onAddMeasurement: () -> Unit,
    onDeleteMeasurement: (BodyMeasurement) -> Unit,
    onEditMeasurement: (BodyMeasurement) -> Unit = {},
    onSelectChartPeriod: (MeasurementChartPeriod) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = GtSpacing.md, vertical = GtSpacing.lg)
            .testTag("measurements-content"),
        verticalArrangement = Arrangement.spacedBy(GtSpacing.md)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GtSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GtSectionHeader(
                    title = stringResource(R.string.measurements_title),
                    supportingText = stringResource(R.string.measurements_subtitle),
                    modifier = Modifier.weight(1f)
                )
                FloatingActionButton(
                    onClick = onAddMeasurement,
                    modifier = Modifier.size(GtSize.minimumTouchTarget)
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.measurements_add))
                }
            }
        }

        if (uiState.latest == null) {
            item {
                GtEmptyState(
                    title = stringResource(R.string.measurements_empty_title),
                    body = stringResource(R.string.measurements_empty_body),
                    actionLabel = stringResource(R.string.measurements_first_action),
                    onAction = onAddMeasurement
                )
            }
        } else {
            item {
                LatestMeasurementCard(
                    uiState.latest,
                    uiState.weightTrend,
                    onEdit = { onEditMeasurement(uiState.latest) },
                    onDelete = { onDeleteMeasurement(uiState.latest) }
                )
            }
            item {
                MeasurementChartCard(
                    measurements = uiState.chartMeasurements,
                    selectedPeriod = uiState.chartPeriod,
                    onSelectPeriod = onSelectChartPeriod
                )
            }
            item {
                GtSectionHeader(
                    title = stringResource(R.string.measurements_history),
                    supportingText = stringResource(R.string.measurements_history_support)
                )
            }
            if (uiState.history.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.measurements_empty_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(uiState.history, key = BodyMeasurement::id) { measurement ->
                    MeasurementHistoryCard(
                        measurement,
                        onEdit = { onEditMeasurement(measurement) },
                        onDelete = { onDeleteMeasurement(measurement) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LatestMeasurementCard(
    measurement: BodyMeasurement,
    trend: WeightTrend,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val largeFont = LocalDensity.current.fontScale >= 1.6f
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        ),
        modifier = Modifier.testTag("measurements-latest")
    ) {
        Column(
            modifier = Modifier.padding(GtSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(GtSpacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.measurements_latest),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit))
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.action_delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            val metrics = measurementMetrics(measurement)
            if (largeFont) {
                Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)) {
                    metrics.forEach { (label, value) -> MeasurementBadge(label, value) }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GtSpacing.md)
                ) {
                    metrics.take(4).forEach { (label, value) ->
                        MeasurementBadge(label, value, Modifier.weight(1f))
                    }
                }
            }
            trend.changeKg?.let { change ->
                Text(
                    stringResource(R.string.measurements_weight_change, change),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun measurementMetrics(measurement: BodyMeasurement): List<Pair<String, String>> = buildList {
    measurement.weightKg?.let {
        add(stringResource(R.string.measurements_weight) to stringResource(R.string.measurements_weight_value, it))
    }
    measurement.bodyFatPercent?.let {
        add(stringResource(R.string.measurements_body_fat) to stringResource(R.string.measurements_percent_value, it))
    }
    measurement.chestCm?.let {
        add(stringResource(R.string.measurements_chest) to stringResource(R.string.measurements_cm_value, it))
    }
    measurement.waistCm?.let {
        add(stringResource(R.string.measurements_waist) to stringResource(R.string.measurements_cm_value, it))
    }
}

@Composable
private fun MeasurementBadge(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MeasurementHistoryCard(
    measurement: BodyMeasurement,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
    val formatter = remember(locale) { DateTimeFormatter.ofPattern("dd MMM yyyy", locale) }
    val date = Instant.ofEpochMilli(measurement.date).atZone(ZoneId.systemDefault()).toLocalDate()
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(GtSpacing.md),
            verticalArrangement = Arrangement.spacedBy(GtSpacing.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(date.format(formatter), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit))
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.action_delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Text(
                measurementMetrics(measurement).joinToString(" · ") { (label, value) -> "$label: $value" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MeasurementChartCard(
    measurements: List<BodyMeasurement>,
    selectedPeriod: MeasurementChartPeriod,
    onSelectPeriod: (MeasurementChartPeriod) -> Unit
) {
    val points = measurements.filter { it.weightKg != null }.sortedBy(BodyMeasurement::date)
    val weights = points.mapNotNull(BodyMeasurement::weightKg)
    val minWeight = weights.minOrNull()
    val maxWeight = weights.maxOrNull()
    val chartDescription = if (minWeight != null && maxWeight != null) {
        pluralStringResource(
            R.plurals.measurements_chart_record_count,
            points.size,
            points.size
        ) + "; " + stringResource(R.string.measurements_chart_range, minWeight, maxWeight)
    } else {
        stringResource(R.string.measurements_chart_empty)
    }
    val lineColor = MaterialTheme.colorScheme.primary
    val guideColor = MaterialTheme.colorScheme.outlineVariant

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.testTag("measurements-chart")
    ) {
        Column(
            modifier = Modifier.padding(GtSpacing.md),
            verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)
        ) {
            GtSectionHeader(
                title = stringResource(R.string.measurements_chart_title),
                supportingText = stringResource(R.string.measurements_chart_support)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(GtSpacing.xs)) {
                items(MeasurementChartPeriod.values().toList()) { period ->
                    FilterChip(
                        selected = period == selectedPeriod,
                        onClick = { onSelectPeriod(period) },
                        label = { Text(period.localizedLabel()) }
                    )
                }
            }
            if (points.size < 2 || minWeight == null || maxWeight == null) {
                Text(
                    stringResource(R.string.measurements_chart_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .semantics { contentDescription = chartDescription }
                        .testTag("measurements-weight-chart")
                ) {
                    val padding = 12.dp.toPx()
                    val width = (size.width - padding * 2).coerceAtLeast(1f)
                    val height = (size.height - padding * 2).coerceAtLeast(1f)
                    drawLine(
                        color = guideColor,
                        start = Offset(padding, size.height - padding),
                        end = Offset(size.width - padding, size.height - padding)
                    )
                    val range = (maxWeight - minWeight).takeIf { it > 0f } ?: 1f
                    val path = Path()
                    points.forEachIndexed { index, measurement ->
                        val x = padding + width * index / (points.size - 1)
                        val normalized = (requireNotNull(measurement.weightKg) - minWeight) / range
                        val y = padding + height * (1f - normalized)
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        drawCircle(lineColor, radius = 4.dp.toPx(), center = Offset(x, y))
                    }
                    drawPath(path, color = lineColor, style = Stroke(width = 3.dp.toPx()))
                }
                Text(
                    chartDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MeasurementChartPeriod.localizedLabel(): String = stringResource(
    when (this) {
        MeasurementChartPeriod.SEVEN_DAYS -> R.string.measurements_period_7
        MeasurementChartPeriod.THIRTY_DAYS -> R.string.measurements_period_30
        MeasurementChartPeriod.NINETY_DAYS -> R.string.measurements_period_90
        MeasurementChartPeriod.ALL -> R.string.measurements_period_all
    }
)

private data class MeasurementFormState(
    val date: String = LocalDate.now().toString(),
    val weight: String = "",
    val bodyFat: String = "",
    val chest: String = "",
    val waist: String = "",
    val hips: String = "",
    val leftArm: String = "",
    val rightArm: String = "",
    val leftCalf: String = "",
    val rightCalf: String = ""
) {
    val values get() = listOf(weight, bodyFat, chest, waist, hips, leftArm, rightArm, leftCalf, rightCalf)
    val parsedDate get() = runCatching { LocalDate.parse(date) }.getOrNull()
    val syntaxValid get() =
        parsedDate?.let { !it.isAfter(LocalDate.now()) } == true &&
            values.all { it.isBlank() || parseLocalizedDecimal(it) != null }

    fun toMeasurement(existing: BodyMeasurement?): BodyMeasurement {
        val localDate = requireNotNull(parsedDate)
        val existingLocalDate = existing?.date?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
        }
        val timestamp = if (existing != null && existingLocalDate == localDate) {
            existing.date
        } else if (localDate == LocalDate.now()) {
            System.currentTimeMillis()
        } else {
            localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
        return (existing ?: BodyMeasurement()).copy(
            date = timestamp,
            weightKg = weight.parseOptional(),
            bodyFatPercent = bodyFat.parseOptional(),
            chestCm = chest.parseOptional(),
            waistCm = waist.parseOptional(),
            hipsCm = hips.parseOptional(),
            leftArmCm = leftArm.parseOptional(),
            rightArmCm = rightArm.parseOptional(),
            leftCalfCm = leftCalf.parseOptional(),
            rightCalfCm = rightCalf.parseOptional()
        )
    }

    companion object {
        fun from(measurement: BodyMeasurement?): MeasurementFormState = MeasurementFormState(
            date = measurement?.date?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().toString()
            } ?: LocalDate.now().toString(),
            weight = measurement?.weightKg.formValue(),
            bodyFat = measurement?.bodyFatPercent.formValue(),
            chest = measurement?.chestCm.formValue(),
            waist = measurement?.waistCm.formValue(),
            hips = measurement?.hipsCm.formValue(),
            leftArm = measurement?.leftArmCm.formValue(),
            rightArm = measurement?.rightArmCm.formValue(),
            leftCalf = measurement?.leftCalfCm.formValue(),
            rightCalf = measurement?.rightCalfCm.formValue()
        )
    }
}

@Composable
private fun MeasurementDialog(
    existing: BodyMeasurement?,
    onDismiss: () -> Unit,
    onConfirm: (BodyMeasurement) -> Unit
) {
    var state by remember(existing?.id) { mutableStateOf(MeasurementFormState.from(existing)) }
    val measurement = if (state.syntaxValid) state.toMeasurement(existing) else null
    val valid = measurement != null && validateMeasurement(measurement) == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (existing == null) R.string.measurements_add_title
                    else R.string.measurements_edit_title
                )
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)) {
                item {
                    OutlinedTextField(
                        value = state.date,
                        onValueChange = { state = state.copy(date = it) },
                        label = { Text(stringResource(R.string.measurements_date_field)) },
                        supportingText = {
                            if (state.parsedDate?.let { it.isAfter(LocalDate.now()) } != false) {
                                Text(stringResource(R.string.measurements_error_date))
                            }
                        },
                        isError = state.parsedDate?.let { it.isAfter(LocalDate.now()) } != false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item { MeasurementField(R.string.measurements_weight_field, state.weight) { state = state.copy(weight = it) } }
                item { MeasurementField(R.string.measurements_body_fat_field, state.bodyFat) { state = state.copy(bodyFat = it) } }
                item { MeasurementField(R.string.measurements_chest_field, state.chest) { state = state.copy(chest = it) } }
                item { MeasurementField(R.string.measurements_waist_field, state.waist) { state = state.copy(waist = it) } }
                item { MeasurementField(R.string.measurements_hips_field, state.hips) { state = state.copy(hips = it) } }
                item { MeasurementField(R.string.measurements_left_arm_field, state.leftArm) { state = state.copy(leftArm = it) } }
                item { MeasurementField(R.string.measurements_right_arm_field, state.rightArm) { state = state.copy(rightArm = it) } }
                item { MeasurementField(R.string.measurements_left_calf_field, state.leftCalf) { state = state.copy(leftCalf = it) } }
                item { MeasurementField(R.string.measurements_right_calf_field, state.rightCalf) { state = state.copy(rightCalf = it) } }
            }
        },
        confirmButton = {
            Button(enabled = valid, onClick = { onConfirm(requireNotNull(measurement)) }) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@Composable
private fun MeasurementField(label: Int, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(stringResource(label)) },
        supportingText = { Text(stringResource(R.string.measurements_decimal_support)) },
        isError = value.isNotBlank() && parseLocalizedDecimal(value) == null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = GtSize.minimumTouchTarget)
    )
}

private fun String.parseOptional(): Float? = if (isBlank()) null else parseLocalizedDecimal(this)

private fun Float?.formValue(): String = this?.toString().orEmpty()

@Composable
private fun MeasurementValidationError.userMessage(): String = stringResource(
    when (this) {
        MeasurementValidationError.EMPTY -> R.string.measurements_error_empty
        MeasurementValidationError.WEIGHT_OUT_OF_RANGE -> R.string.measurements_error_weight
        MeasurementValidationError.BODY_FAT_OUT_OF_RANGE -> R.string.measurements_error_body_fat
        MeasurementValidationError.CIRCUMFERENCE_OUT_OF_RANGE -> R.string.measurements_error_circumference
        MeasurementValidationError.SAVE_FAILED -> R.string.measurements_error_save
    }
)
