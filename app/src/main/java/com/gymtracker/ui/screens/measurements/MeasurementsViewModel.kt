package com.gymtracker.ui.screens.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.dao.MeasurementDao
import com.gymtracker.data.entities.BodyMeasurement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeasurementsViewModel @Inject constructor(
    private val dao: MeasurementDao
) : ViewModel() {

    val measurements = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveMeasurement(measurement: BodyMeasurement) {
        viewModelScope.launch { dao.insert(measurement) }
    }

    fun deleteMeasurement(measurement: BodyMeasurement) {
        viewModelScope.launch { dao.delete(measurement) }
    }
}
