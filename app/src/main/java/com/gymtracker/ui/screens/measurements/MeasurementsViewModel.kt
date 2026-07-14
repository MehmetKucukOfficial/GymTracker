package com.gymtracker.ui.screens.measurements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.dao.MeasurementDao
import com.gymtracker.data.entities.BodyMeasurement
import com.gymtracker.data.measurements.MeasurementValidationError
import com.gymtracker.data.measurements.WeightTrend
import com.gymtracker.data.measurements.validateMeasurement
import com.gymtracker.data.measurements.weightTrend
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MeasurementsUiState(
    val latest: BodyMeasurement? = null,
    val history: List<BodyMeasurement> = emptyList(),
    val weightTrend: WeightTrend = WeightTrend(null, null)
)

@HiltViewModel
class MeasurementsViewModel @Inject constructor(
    private val dao: MeasurementDao
) : ViewModel() {

    private val measurements = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<MeasurementsUiState> = measurements
        .map { items ->
            MeasurementsUiState(
                latest = items.firstOrNull(),
                history = items.drop(1),
                weightTrend = weightTrend(items)
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MeasurementsUiState())

    private val _validationError = MutableStateFlow<MeasurementValidationError?>(null)
    val validationError: StateFlow<MeasurementValidationError?> = _validationError.asStateFlow()

    fun saveMeasurement(measurement: BodyMeasurement, onSaved: () -> Unit = {}) {
        val error = validateMeasurement(measurement)
        if (error != null) {
            _validationError.value = error
            return
        }
        viewModelScope.launch {
            runCatching { dao.insert(measurement) }
                .onSuccess { onSaved() }
                .onFailure { _validationError.value = MeasurementValidationError.SAVE_FAILED }
        }
    }

    fun deleteMeasurement(measurement: BodyMeasurement) {
        viewModelScope.launch { dao.delete(measurement) }
    }

    fun clearValidationError() {
        _validationError.value = null
    }
}
