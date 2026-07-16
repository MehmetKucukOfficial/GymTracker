package com.gymtracker.ui.screens.programs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.entities.Program
import com.gymtracker.data.programs.ProgramRepository
import com.gymtracker.data.programs.ProgramWorkoutPlan
import com.gymtracker.data.programs.ProgramWorkoutStartResult
import com.gymtracker.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProgramDetailUiState(
    val program: Program? = null,
    val workouts: List<ProgramWorkoutPlan> = emptyList(),
    val isLoading: Boolean = false,
    val isStarting: Boolean = false,
    val message: ProgramActionMessage? = null
)

enum class ProgramActionMessage {
    EXISTING_WORKOUT_RESUMED,
    START_FAILED
}

sealed interface ProgramsListUiState {
    data object Loading : ProgramsListUiState
    data class Content(val programs: List<Program>) : ProgramsListUiState
    data object Error : ProgramsListUiState
}

@HiltViewModel
class ProgramsViewModel @Inject constructor(
    private val repository: ProgramRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val programId: Long? = savedStateHandle[Screen.ProgramDetail.ARG_PROGRAM_ID]
    private val isStarting = MutableStateFlow(false)
    private val message = MutableStateFlow<ProgramActionMessage?>(null)
    private val listRetry = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val listState = listRetry
        .flatMapLatest {
            repository.programs
                .map<List<Program>, ProgramsListUiState> { ProgramsListUiState.Content(it) }
                .onStart { emit(ProgramsListUiState.Loading) }
                .catch { emit(ProgramsListUiState.Error) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgramsListUiState.Loading)

    val detailState: StateFlow<ProgramDetailUiState> = programId?.let { id ->
        combine(
            repository.observeProgram(id),
            repository.observeWorkoutPlans(id),
            isStarting,
            message
        ) { program, workouts, starting, currentMessage ->
            ProgramDetailUiState(
                program = program,
                workouts = workouts,
                isStarting = starting,
                message = currentMessage
            )
        }.onStart {
            emit(ProgramDetailUiState(isLoading = true))
        }
    }?.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ProgramDetailUiState(isLoading = true)
    ) ?: flowOf(ProgramDetailUiState()).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ProgramDetailUiState()
    )

    fun purchaseProgram(program: Program) {
        viewModelScope.launch { repository.purchase(program) }
    }

    fun startWorkout(programWorkoutId: Long, onOpened: (Long) -> Unit) {
        val id = programId ?: return
        if (isStarting.value) return
        viewModelScope.launch {
            isStarting.value = true
            runCatching { repository.startWorkout(id, programWorkoutId) }
                .onSuccess { result ->
                    message.value = if (result is ProgramWorkoutStartResult.ExistingActive) {
                        ProgramActionMessage.EXISTING_WORKOUT_RESUMED
                    } else {
                        null
                    }
                    onOpened(result.sessionId)
                }
                .onFailure { message.value = ProgramActionMessage.START_FAILED }
            isStarting.value = false
        }
    }

    fun clearMessage() {
        message.value = null
    }

    fun retryPrograms() {
        listRetry.value += 1
    }
}
