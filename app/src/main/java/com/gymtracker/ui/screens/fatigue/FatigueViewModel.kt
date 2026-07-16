package com.gymtracker.ui.screens.fatigue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.dao.WorkoutDao
import com.gymtracker.data.home.homeDashboardPeriod
import com.gymtracker.domain.MuscleLoadCalculator
import com.gymtracker.domain.MuscleLoadSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
    private val clock: Clock
) : ViewModel() {

    private val retryGeneration = MutableStateFlow(0)

    val uiState = retryGeneration
        .flatMapLatest { loadPeriods() }
        .flatMapLatest { period ->
            workoutDao.observeCompletedSetsForLoadSince(period.weekStartMillis)
                .map { sets ->
                    val content: MuscleLoadUiState = MuscleLoadUiState.Content(
                        MuscleLoadCalculator.calculate(sets, clock.millis())
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

    private fun loadPeriods() = flow {
        while (true) {
            val period = homeDashboardPeriod(clock.instant(), clock.zone)
            emit(period)
            delay((period.nextDayStartMillis - clock.millis()).coerceAtLeast(1_000L))
        }
    }
}
