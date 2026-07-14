package com.gymtracker.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymtracker.R
import com.gymtracker.data.home.HomeDashboardData
import com.gymtracker.ui.components.GtEmptyState
import com.gymtracker.ui.components.GtErrorState
import com.gymtracker.ui.components.GtLoadingState
import com.gymtracker.ui.components.GtMetricCard
import com.gymtracker.ui.components.GtQuickActionCard
import com.gymtracker.ui.components.GtSectionHeader
import com.gymtracker.ui.navigation.Screen
import com.gymtracker.ui.theme.GtSize
import com.gymtracker.ui.theme.GtSpacing
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val greeting = stringResource(
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> R.string.home_greeting_morning
            in 12..17 -> R.string.home_greeting_afternoon
            else -> R.string.home_greeting_evening
        }
    )

    HomeContent(
        uiState = uiState,
        greeting = greeting,
        onNavigate = onNavigate,
        onRetry = viewModel::retry
    )
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    greeting: String,
    onNavigate: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val fontScale = LocalDensity.current.fontScale
        val layout = homeLayoutConfig(maxWidth, fontScale)
        val isExpanded = layout.isExpanded
        val gridColumns = layout.metricColumns
        val horizontalPadding = if (isExpanded) GtSpacing.xl else GtSpacing.md

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding, vertical = GtSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = GtSize.contentMaxWidth)
                    .fillMaxWidth()
                    .testTag(if (isExpanded) "home-layout-expanded" else "home-layout-compact")
            ) {
                HomeHeader(greeting = greeting)
                Spacer(Modifier.height(GtSpacing.lg))
                StartWorkoutCard(onClick = { onNavigate(Screen.Workout.route) })
                Spacer(Modifier.height(GtSpacing.xl))

                when (uiState) {
                    HomeUiState.Loading -> GtLoadingState(
                        title = stringResource(R.string.home_loading),
                        modifier = Modifier.testTag("home-state-loading")
                    )

                    HomeUiState.Error -> GtErrorState(
                        title = stringResource(R.string.home_error_title),
                        body = stringResource(R.string.home_error_body),
                        retryLabel = stringResource(R.string.action_retry),
                        onRetry = onRetry,
                        modifier = Modifier.testTag("home-state-error")
                    )

                    is HomeUiState.Content -> DashboardContent(
                        dashboard = uiState.dashboard,
                        gridColumns = gridColumns,
                        onNavigate = onNavigate,
                        modifier = Modifier.testTag("home-state-content")
                    )
                }
                Spacer(Modifier.height(GtSpacing.xl))
            }
        }
    }
}

@Composable
private fun HomeHeader(greeting: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(GtSize.minimumTouchTarget)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(GtSize.iconMedium)
            )
        }
        Spacer(Modifier.width(GtSpacing.sm))
        Column {
            Text(
                text = stringResource(R.string.home_eyebrow),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(text = greeting, style = MaterialTheme.typography.headlineMedium)
            Text(
                text = stringResource(R.string.home_welcome),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StartWorkoutCard(onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(GtSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(GtSpacing.md)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.xs)) {
                Text(
                    text = stringResource(R.string.home_start_workout),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.home_start_workout_support),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(stringResource(R.string.home_start_workout))
                Spacer(Modifier.width(GtSpacing.xs))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

internal data class HomeLayoutConfig(
    val isExpanded: Boolean,
    val metricColumns: Int
)

internal fun homeLayoutConfig(width: Dp, fontScale: Float): HomeLayoutConfig {
    val isExpanded = width >= 720.dp
    val metricColumns = when {
        fontScale >= 1.6f -> 1
        isExpanded -> 4
        else -> 2
    }
    return HomeLayoutConfig(isExpanded = isExpanded, metricColumns = metricColumns)
}

@Composable
private fun DashboardContent(
    dashboard: HomeDashboardData,
    gridColumns: Int,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(GtSpacing.xl)) {
        val hasAnyData = dashboard.totalWorkouts > 0 ||
            dashboard.latestWeight != null || dashboard.todayNutritionEntryCount > 0
        if (!hasAnyData) {
            GtEmptyState(
                title = stringResource(R.string.home_empty_title),
                body = stringResource(R.string.home_empty_body),
                actionLabel = stringResource(R.string.home_empty_action),
                onAction = { onNavigate(Screen.Workout.route) }
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.md)) {
            GtSectionHeader(
                title = stringResource(R.string.home_section_metrics),
                supportingText = stringResource(R.string.home_section_metrics_support)
            )
            MetricGrid(
                items = dashboard.metricItems(),
                columns = gridColumns
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.md)) {
            GtSectionHeader(
                title = stringResource(R.string.home_section_quick_actions),
                supportingText = stringResource(R.string.home_section_quick_actions_support)
            )
            QuickActionGrid(
                items = quickActionItems(),
                columns = if (gridColumns == 1) 1 else 2,
                onNavigate = onNavigate
            )
        }

        dashboard.lastWorkoutName?.let { workoutName ->
            Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.md)) {
                GtSectionHeader(
                    title = stringResource(R.string.home_section_last_workout),
                    supportingText = stringResource(R.string.home_last_workout_support)
                )
                LastWorkoutCard(
                    name = workoutName,
                    date = dashboard.lastWorkoutDate
                )
            }
        }
    }
}

private data class MetricItem(
    val label: String,
    val value: String,
    val supportingText: String,
    val icon: ImageVector,
    val accent: Color
)

@Composable
private fun HomeDashboardData.metricItems(): List<MetricItem> = listOf(
    MetricItem(
        label = stringResource(R.string.home_metric_total_workouts),
        value = if (totalWorkouts == 0) stringResource(R.string.home_value_no_workout)
        else stringResource(R.string.home_value_workout_count, totalWorkouts),
        supportingText = stringResource(R.string.home_support_all_time),
        icon = Icons.Default.FitnessCenter,
        accent = MaterialTheme.colorScheme.primary
    ),
    MetricItem(
        label = stringResource(R.string.home_metric_week_workouts),
        value = if (weekWorkouts == 0) stringResource(R.string.home_value_no_workout)
        else stringResource(R.string.home_value_workout_count, weekWorkouts),
        supportingText = stringResource(R.string.home_support_last_seven_days),
        icon = Icons.Default.CalendarToday,
        accent = MaterialTheme.colorScheme.secondary
    ),
    MetricItem(
        label = stringResource(R.string.home_metric_weight),
        value = latestWeight?.let { stringResource(R.string.home_value_weight, it) }
            ?: stringResource(R.string.home_value_no_measurement),
        supportingText = stringResource(R.string.home_support_latest_measurement),
        icon = Icons.Default.MonitorWeight,
        accent = MaterialTheme.colorScheme.tertiary
    ),
    MetricItem(
        label = stringResource(R.string.home_metric_calories),
        value = if (todayNutritionEntryCount == 0) stringResource(R.string.home_value_no_meal)
        else pluralStringResource(R.plurals.home_value_calories, todayCalories, todayCalories),
        supportingText = stringResource(R.string.home_support_today),
        icon = Icons.Default.LocalFireDepartment,
        accent = MaterialTheme.colorScheme.secondary
    )
)

@Composable
private fun MetricGrid(items: List<MetricItem>, columns: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)) {
        items.chunked(columns).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GtSpacing.sm)
            ) {
                rowItems.forEach { item ->
                    GtMetricCard(
                        label = item.label,
                        value = item.value,
                        supportingText = item.supportingText,
                        icon = item.icon,
                        accent = item.accent,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private data class QuickActionItem(
    val labelRes: Int,
    val icon: ImageVector,
    val route: String
)

private fun quickActionItems() = listOf(
    QuickActionItem(R.string.home_action_measurement, Icons.Default.Straighten, Screen.Measurements.route),
    QuickActionItem(R.string.home_action_meal, Icons.Default.Restaurant, Screen.Nutrition.route),
    QuickActionItem(R.string.home_action_photo, Icons.Default.AddPhotoAlternate, Screen.ProgressPhotos.route),
    QuickActionItem(R.string.home_action_programs, Icons.AutoMirrored.Filled.List, Screen.Programs.route),
    QuickActionItem(R.string.home_action_muscle_map, Icons.Default.AccessibilityNew, Screen.Fatigue.route)
)

@Composable
private fun QuickActionGrid(
    items: List<QuickActionItem>,
    columns: Int,
    onNavigate: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)) {
        items.chunked(columns).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GtSpacing.sm)
            ) {
                rowItems.forEach { item ->
                    GtQuickActionCard(
                        label = stringResource(item.labelRes),
                        icon = item.icon,
                        onClick = { onNavigate(item.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun LastWorkoutCard(name: String, date: Long?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(GtSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(GtSize.minimumTouchTarget)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.width(GtSpacing.sm))
            Column {
                Text(text = name, style = MaterialTheme.typography.titleMedium)
                date?.let {
                    Text(
                        text = SimpleDateFormat("dd MMM yyyy", Locale("tr")).format(Date(it)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
