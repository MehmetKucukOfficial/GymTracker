package com.gymtracker.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.home.HomeDashboardRepository
import com.gymtracker.data.home.homeDashboardPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Content(val dashboard: com.gymtracker.data.home.HomeDashboardData) : HomeUiState
    data object Error : HomeUiState
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel @Inject constructor(
    private val repository: HomeDashboardRepository
) : ViewModel() {
    private val retryGeneration = MutableStateFlow(0)

    val uiState = retryGeneration.flatMapLatest { observeCurrentDashboard() }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState.Loading
    )

    fun retry() {
        retryGeneration.update { it + 1 }
    }

    private fun observeCurrentDashboard(): Flow<HomeUiState> = dashboardPeriods()
        .flatMapLatest { period ->
            repository.observeDashboard(
                weekStartMillis = period.weekStartMillis,
                dayStartMillis = period.dayStartMillis,
                nextDayStartMillis = period.nextDayStartMillis
            )
        }
        .map<com.gymtracker.data.home.HomeDashboardData, HomeUiState>(HomeUiState::Content)
        .onStart { emit(HomeUiState.Loading) }
        .catch { emit(HomeUiState.Error) }

    private fun dashboardPeriods() = flow {
        while (true) {
            val period = homeDashboardPeriod()
            emit(period)
            val remainingToday = period.nextDayStartMillis - System.currentTimeMillis()
            delay(remainingToday.coerceAtLeast(1_000L))
        }
    }
}
