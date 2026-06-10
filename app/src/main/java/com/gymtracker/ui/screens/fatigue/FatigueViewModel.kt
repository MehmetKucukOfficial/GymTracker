package com.gymtracker.ui.screens.fatigue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.dao.WorkoutDao
import com.gymtracker.domain.FatigueCalculator
import com.gymtracker.domain.MuscleGroup
import com.gymtracker.domain.MuscleFatigue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class FatigueViewModel @Inject constructor(
    private val workoutDao: WorkoutDao
) : ViewModel() {

    private val _fatigueData = MutableStateFlow<List<MuscleFatigue>>(emptyFatigue())
    val fatigueData = _fatigueData.asStateFlow()

    init {
        loadFatigue()
    }

    private fun loadFatigue() {
        viewModelScope.launch {
            val sevenDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
            val sets = workoutDao.getSetsFromSessionsSince(sevenDaysAgo)
            _fatigueData.value = FatigueCalculator.calculate(sets)
        }
    }

    private fun emptyFatigue() = MuscleGroup.values().map {
        MuscleFatigue(muscle = it, fatiguePercent = 0f, totalVolume = 0f)
    }
}
