package com.gymtracker.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.dao.WorkoutDao
import com.gymtracker.data.entities.WorkoutSession
import com.gymtracker.data.entities.WorkoutSet
import com.gymtracker.domain.ExerciseLibrary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutUiState(
    val sessions: List<WorkoutSession> = emptyList(),
    val activeSession: WorkoutSession? = null,
    val activeSets: List<WorkoutSet> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutDao: WorkoutDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState = _uiState.asStateFlow()

    val sessions = workoutDao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startSession(name: String) {
        viewModelScope.launch {
            val session = WorkoutSession(name = name)
            val id = workoutDao.insertSession(session)
            val created = workoutDao.getSessionById(id)
            _uiState.update { it.copy(activeSession = created, activeSets = emptyList()) }
        }
    }

    fun loadSession(sessionId: Long) {
        viewModelScope.launch {
            val session = workoutDao.getSessionById(sessionId)
            val sets = workoutDao.getSetsBySessionSync(sessionId)
            _uiState.update { it.copy(activeSession = session, activeSets = sets) }
        }
    }

    fun addSet(exerciseName: String, setNumber: Int, reps: Int, weightKg: Float) {
        val sessionId = _uiState.value.activeSession?.id ?: return
        viewModelScope.launch {
            val set = WorkoutSet(
                sessionId = sessionId,
                exerciseName = exerciseName,
                setNumber = setNumber,
                reps = reps,
                weightKg = weightKg
            )
            workoutDao.insertSet(set)
            val updatedSets = workoutDao.getSetsBySessionSync(sessionId)
            _uiState.update { it.copy(activeSets = updatedSets) }
        }
    }

    fun deleteSet(set: WorkoutSet) {
        viewModelScope.launch {
            workoutDao.deleteSet(set)
            val sessionId = _uiState.value.activeSession?.id ?: return@launch
            val updatedSets = workoutDao.getSetsBySessionSync(sessionId)
            _uiState.update { it.copy(activeSets = updatedSets) }
        }
    }

    fun finishSession(durationMinutes: Int, notes: String) {
        val session = _uiState.value.activeSession ?: return
        viewModelScope.launch {
            workoutDao.updateSession(session.copy(durationMinutes = durationMinutes, notes = notes))
            _uiState.update { it.copy(activeSession = null, activeSets = emptyList()) }
        }
    }

    fun getExerciseCategories() = ExerciseLibrary.getCategories()
    fun getExercisesForCategory(cat: String) = ExerciseLibrary.getByCategory(cat)
}
