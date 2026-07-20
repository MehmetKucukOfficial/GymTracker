package com.gymtracker.ui.screens.fatigue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.dao.WorkoutDao
import com.gymtracker.data.dao.MeasurementDao
import com.gymtracker.data.home.currentDashboardPeriods
import com.gymtracker.domain.MuscleLoadCalculator
import com.gymtracker.domain.MuscleLoadSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

sealed interface MuscleLoadUiState {
    data object Loading : MuscleLoadUiState
    data class Content(val summary: MuscleLoadSummary) : MuscleLoadUiState
    data object Error : MuscleLoadUiState
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class FatigueViewModel @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val measurementDao: MeasurementDao,
    private val clock: Clock
) : ViewModel() {

    private val retryGeneration = MutableStateFlow(0)

    val uiState = retryGeneration
        .flatMapLatest { currentDashboardPeriods(clock) }
        .flatMapLatest { period ->
            combine(
                workoutDao.observeCompletedSetsForLoadSince(period.weekStartMillis),
                measurementDao.observeLatestWithWeight()
            ) { sets, latestMeasurement ->
                    val content: MuscleLoadUiState = MuscleLoadUiState.Content(
                        MuscleLoadCalculator.calculate(
                            sets = sets,
                            nowMillis = clock.millis(),
                            personalBodyweightKg = latestMeasurement?.weightKg
                        )
                    )
                    content
            }
        }
        .onStart { emit(MuscleLoadUiState.Loading) }
        .catch { emit(MuscleLoadUiState.Error) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MuscleLoadUiState.Loading
        )

    fun retry() {
        retryGeneration.update { it + 1 }
    }
}
