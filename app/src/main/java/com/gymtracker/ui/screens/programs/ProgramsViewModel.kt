package com.gymtracker.ui.screens.programs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.entities.Program
import com.gymtracker.data.entities.ProgramWorkout
import com.gymtracker.data.programs.ProgramRepository
import com.gymtracker.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProgramDetailUiState(
    val program: Program? = null,
    val workouts: List<ProgramWorkout> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class ProgramsViewModel @Inject constructor(
    private val repository: ProgramRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val programId: Long? = savedStateHandle[Screen.ProgramDetail.ARG_PROGRAM_ID]

    val programs = repository.programs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val detailState: StateFlow<ProgramDetailUiState> = programId?.let { id ->
        combine(
            repository.observeProgram(id),
            repository.observeWorkouts(id)
        ) { program, workouts ->
            ProgramDetailUiState(program = program, workouts = workouts)
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
}
