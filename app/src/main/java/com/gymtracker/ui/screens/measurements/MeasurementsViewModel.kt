package com.gymtracker.ui.screens.measurements

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.entities.BodyMeasurement
import com.gymtracker.data.measurements.MeasurementChartPeriod
import com.gymtracker.data.measurements.MeasurementRepository
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MeasurementsUiState(
    val latest: BodyMeasurement? = null,
    val history: List<BodyMeasurement> = emptyList(),
    val weightTrend: WeightTrend = WeightTrend(null, null),
    val chartPeriod: MeasurementChartPeriod = MeasurementChartPeriod.THIRTY_DAYS,
    val chartMeasurements: List<BodyMeasurement> = emptyList()
)

@HiltViewModel
class MeasurementsViewModel @Inject constructor(
    private val repository: MeasurementRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val measurements = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _chartPeriod = MutableStateFlow(
        savedStateHandle.get<String>(CHART_PERIOD_KEY)
            ?.let { runCatching { MeasurementChartPeriod.valueOf(it) }.getOrNull() }
            ?: MeasurementChartPeriod.THIRTY_DAYS
    )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val chartMeasurements = _chartPeriod
        .flatMapLatest(repository::observeChart)

    val uiState: StateFlow<MeasurementsUiState> = combine(
        measurements,
        _chartPeriod,
        chartMeasurements
    ) { items, period, chartItems ->
            MeasurementsUiState(
                latest = items.firstOrNull(),
                history = items.drop(1),
                weightTrend = weightTrend(items),
                chartPeriod = period,
                chartMeasurements = chartItems
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MeasurementsUiState())

    private val _validationError = MutableStateFlow<MeasurementValidationError?>(null)
    val validationError: StateFlow<MeasurementValidationError?> = _validationError.asStateFlow()

    private val _deletedMeasurement = MutableStateFlow<BodyMeasurement?>(null)
    val deletedMeasurement: StateFlow<BodyMeasurement?> = _deletedMeasurement.asStateFlow()

    fun saveMeasurement(measurement: BodyMeasurement, onSaved: () -> Unit = {}) {
        val error = validateMeasurement(measurement)
        if (error != null) {
            _validationError.value = error
            return
        }
        viewModelScope.launch {
            runCatching { repository.save(measurement) }
                .onSuccess { onSaved() }
                .onFailure { _validationError.value = MeasurementValidationError.SAVE_FAILED }
        }
    }

    fun updateMeasurement(measurement: BodyMeasurement, onSaved: () -> Unit = {}) {
        val error = validateMeasurement(measurement)
        if (error != null) {
            _validationError.value = error
            return
        }
        viewModelScope.launch {
            runCatching { repository.update(measurement) }
                .onSuccess { onSaved() }
                .onFailure { _validationError.value = MeasurementValidationError.SAVE_FAILED }
        }
    }

    fun deleteMeasurement(measurement: BodyMeasurement) {
        viewModelScope.launch {
            runCatching { repository.delete(measurement) }
                .onSuccess { _deletedMeasurement.value = measurement }
                .onFailure { _validationError.value = MeasurementValidationError.SAVE_FAILED }
        }
    }

    fun undoDelete(measurement: BodyMeasurement) {
        if (_deletedMeasurement.value?.id != measurement.id) return
        viewModelScope.launch {
            runCatching { repository.save(measurement) }
                .onSuccess { _deletedMeasurement.value = null }
                .onFailure { _validationError.value = MeasurementValidationError.SAVE_FAILED }
        }
    }

    fun consumeDeletedMeasurement(measurement: BodyMeasurement) {
        if (_deletedMeasurement.value?.id == measurement.id) _deletedMeasurement.value = null
    }

    fun selectChartPeriod(period: MeasurementChartPeriod) {
        savedStateHandle[CHART_PERIOD_KEY] = period.name
        _chartPeriod.value = period
    }

    fun clearValidationError() {
        _validationError.value = null
    }

    private companion object {
        const val CHART_PERIOD_KEY = "measurements_chart_period"
    }
}
