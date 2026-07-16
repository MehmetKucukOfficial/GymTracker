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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutUiState(
    val activeSession: WorkoutSession? = null,
    val activeSets: List<WorkoutSet> = emptyList(),
    val isLoading: Boolean = false,
    val mutationError: WorkoutMutationError? = null
)

enum class WorkoutMutationError {
    INVALID_SET,
    READ_ONLY_SESSION,
    FINISH_FAILED
}

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: Long? = savedStateHandle[Screen.WorkoutDetail.ARG_SESSION_ID]
    private val mutationError = MutableStateFlow<WorkoutMutationError?>(null)

    val completedSessions = repository.completedSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeSession = repository.activeSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val uiState: StateFlow<WorkoutUiState> = sessionId?.let { id ->
        combine(
            repository.observeSession(id),
            repository.observeSets(id),
            mutationError
        ) { session, sets, error ->
            WorkoutUiState(
                activeSession = session,
                activeSets = sets,
                mutationError = error
            )
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

    fun startOrResumeSession(name: String, onOpened: (Long) -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch {
            onOpened(repository.startOrResumeSession(name))
        }
    }

    fun addSet(exerciseName: String, setNumber: Int, reps: Int, weightKg: Float) {
        val id = sessionId ?: return
        viewModelScope.launch {
            runCatching {
                repository.addSet(
                    WorkoutSet(
                        sessionId = id,
                        exerciseName = exerciseName,
                        setNumber = setNumber,
                        reps = reps,
                        weightKg = weightKg
                    )
                )
            }.onSuccess {
                mutationError.value = null
            }.onFailure { error ->
                mutationError.value = if (error is IllegalArgumentException) {
                    WorkoutMutationError.INVALID_SET
                } else {
                    WorkoutMutationError.READ_ONLY_SESSION
                }
            }
        }
    }

    fun deleteSet(set: WorkoutSet) {
        viewModelScope.launch {
            runCatching { repository.deleteSet(set) }
                .onSuccess { mutationError.value = null }
                .onFailure { mutationError.value = WorkoutMutationError.READ_ONLY_SESSION }
        }
    }

    fun finishSession(durationMinutes: Int, notes: String, onFinished: () -> Unit) {
        val session = uiState.value.activeSession ?: return
        viewModelScope.launch {
            runCatching {
                repository.finishSession(session.id, durationMinutes, notes)
            }.onSuccess {
                mutationError.value = null
                onFinished()
            }.onFailure {
                mutationError.value = WorkoutMutationError.FINISH_FAILED
            }
        }
    }

    fun clearMutationError() {
        mutationError.value = null
    }

    fun getExerciseCategories() = ExerciseLibrary.getCategories()

    fun getExercisesForCategory(category: String) = ExerciseLibrary.getByCategory(category)
}
