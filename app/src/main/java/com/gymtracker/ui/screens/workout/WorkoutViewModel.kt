package com.gymtracker.ui.screens.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.entities.WorkoutSession
import com.gymtracker.data.entities.WorkoutSet
import com.gymtracker.data.workout.WorkoutRepository
import com.gymtracker.domain.ExerciseLibrary
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

data class WorkoutUiState(
    val activeSession: WorkoutSession? = null,
    val activeSets: List<WorkoutSet> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: Long? = savedStateHandle[Screen.WorkoutDetail.ARG_SESSION_ID]

    val sessions = repository.sessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<WorkoutUiState> = sessionId?.let { id ->
        combine(
            repository.observeSession(id),
            repository.observeSets(id)
        ) { session, sets ->
            WorkoutUiState(activeSession = session, activeSets = sets)
        }.onStart {
            emit(WorkoutUiState(isLoading = true))
        }
    }?.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        WorkoutUiState(isLoading = true)
    ) ?: flowOf(WorkoutUiState()).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        WorkoutUiState()
    )

    fun startSession(name: String, onCreated: (Long) -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch {
            onCreated(repository.startSession(name))
        }
    }

    fun addSet(exerciseName: String, setNumber: Int, reps: Int, weightKg: Float) {
        val id = sessionId ?: return
        viewModelScope.launch {
            repository.addSet(
                WorkoutSet(
                    sessionId = id,
                    exerciseName = exerciseName,
                    setNumber = setNumber,
                    reps = reps,
                    weightKg = weightKg
                )
            )
        }
    }

    fun deleteSet(set: WorkoutSet) {
        viewModelScope.launch { repository.deleteSet(set) }
    }

    fun finishSession(durationMinutes: Int, notes: String, onFinished: () -> Unit) {
        val session = uiState.value.activeSession ?: return
        viewModelScope.launch {
            repository.finishSession(session, durationMinutes, notes)
            onFinished()
        }
    }

    fun getExerciseCategories() = ExerciseLibrary.getCategories()

    fun getExercisesForCategory(category: String) = ExerciseLibrary.getByCategory(category)
}
