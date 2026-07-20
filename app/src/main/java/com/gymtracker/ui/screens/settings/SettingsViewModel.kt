package com.gymtracker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.gymtracker.data.portability.DataExportRepository
import com.gymtracker.data.portability.DataExportSummary
import com.gymtracker.data.portability.InsufficientExportStorageException
import com.gymtracker.data.portability.MissingManagedPhotoException
import com.gymtracker.data.settings.AppSettings
import com.gymtracker.data.settings.SettingsRepository
import com.gymtracker.data.settings.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val dataExportRepository: DataExportRepository
) : ViewModel() {
    val settings: StateFlow<AppSettings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppSettings()
    )

    private val _dataExportState = MutableStateFlow<DataExportState>(DataExportState.Idle)
    val dataExportState: StateFlow<DataExportState> = _dataExportState.asStateFlow()

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(themeMode)
        }
    }

    fun exportData(destination: Uri) {
        if (_dataExportState.value == DataExportState.Exporting) return
        _dataExportState.value = DataExportState.Exporting
        viewModelScope.launch {
            _dataExportState.value = dataExportRepository.exportTo(destination).fold(
                onSuccess = DataExportState::Success,
                onFailure = { error ->
                    DataExportState.Failure(
                        when (error) {
                            is InsufficientExportStorageException ->
                                DataExportFailure.INSUFFICIENT_STORAGE
                            is MissingManagedPhotoException ->
                                DataExportFailure.MISSING_MANAGED_PHOTO
                            else -> DataExportFailure.GENERIC
                        }
                    )
                }
            )
        }
    }
}

sealed interface DataExportState {
    data object Idle : DataExportState
    data object Exporting : DataExportState
    data class Success(val summary: DataExportSummary) : DataExportState
    data class Failure(val reason: DataExportFailure) : DataExportState
}

enum class DataExportFailure {
    INSUFFICIENT_STORAGE,
    MISSING_MANAGED_PHOTO,
    GENERIC
}
