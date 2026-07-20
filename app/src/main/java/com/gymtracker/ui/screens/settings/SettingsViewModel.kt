package com.gymtracker.ui.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.portability.DataDeletionRepository
import com.gymtracker.data.portability.DataDeletionRollbackException
import com.gymtracker.data.portability.DataDeletionSummary
import com.gymtracker.data.portability.CorruptPortableArchiveException
import com.gymtracker.data.portability.DataExportRepository
import com.gymtracker.data.portability.DataExportSummary
import com.gymtracker.data.portability.InsufficientExportStorageException
import com.gymtracker.data.portability.InsufficientRestoreStorageException
import com.gymtracker.data.portability.InvalidPortableArchiveException
import com.gymtracker.data.portability.MissingManagedPhotoException
import com.gymtracker.data.portability.DataRestoreRepository
import com.gymtracker.data.portability.DataRestoreSummary
import com.gymtracker.data.portability.RestoreRollbackException
import com.gymtracker.data.settings.AppSettings
import com.gymtracker.data.settings.SettingsRepository
import com.gymtracker.data.settings.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val dataExportRepository: DataExportRepository,
    private val dataRestoreRepository: DataRestoreRepository,
    private val dataDeletionRepository: DataDeletionRepository
) : ViewModel() {
    val settings: StateFlow<AppSettings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppSettings()
    )

    private val _dataExportState = MutableStateFlow<DataExportState>(DataExportState.Idle)
    val dataExportState: StateFlow<DataExportState> = _dataExportState.asStateFlow()
    private val _dataRestoreState = MutableStateFlow<DataRestoreState>(DataRestoreState.Idle)
    val dataRestoreState: StateFlow<DataRestoreState> = _dataRestoreState.asStateFlow()
    private val _dataDeletionState = MutableStateFlow<DataDeletionState>(DataDeletionState.Idle)
    val dataDeletionState: StateFlow<DataDeletionState> = _dataDeletionState.asStateFlow()

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(themeMode)
        }
    }

    fun exportData(destination: Uri) {
        if (
            _dataExportState.value == DataExportState.Exporting ||
            _dataRestoreState.value == DataRestoreState.Restoring ||
            _dataDeletionState.value == DataDeletionState.Deleting
        ) return
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

    fun restoreData(source: Uri) {
        if (
            _dataRestoreState.value == DataRestoreState.Restoring ||
            _dataExportState.value == DataExportState.Exporting ||
            _dataDeletionState.value == DataDeletionState.Deleting
        ) return
        _dataRestoreState.value = DataRestoreState.Restoring
        viewModelScope.launch {
            _dataRestoreState.value = dataRestoreRepository.restoreFrom(source).fold(
                onSuccess = DataRestoreState::Success,
                onFailure = { error ->
                    DataRestoreState.Failure(
                        when (error) {
                            is InsufficientRestoreStorageException ->
                                DataRestoreFailure.INSUFFICIENT_STORAGE
                            is CorruptPortableArchiveException,
                            is InvalidPortableArchiveException ->
                                DataRestoreFailure.INVALID_ARCHIVE
                            is RestoreRollbackException ->
                                DataRestoreFailure.ROLLBACK_FAILED
                            else -> DataRestoreFailure.GENERIC
                        }
                    )
                }
            )
        }
    }

    fun deleteAllData() {
        if (
            _dataDeletionState.value == DataDeletionState.Deleting ||
            _dataExportState.value == DataExportState.Exporting ||
            _dataRestoreState.value == DataRestoreState.Restoring
        ) return
        _dataDeletionState.value = DataDeletionState.Deleting
        viewModelScope.launch {
            _dataDeletionState.value = dataDeletionRepository.deleteAll().fold(
                onSuccess = DataDeletionState::Success,
                onFailure = { error ->
                    DataDeletionState.Failure(
                        if (error is DataDeletionRollbackException) {
                            DataDeletionFailure.ROLLBACK_FAILED
                        } else {
                            DataDeletionFailure.GENERIC
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

sealed interface DataRestoreState {
    data object Idle : DataRestoreState
    data object Restoring : DataRestoreState
    data class Success(val summary: DataRestoreSummary) : DataRestoreState
    data class Failure(val reason: DataRestoreFailure) : DataRestoreState
}

enum class DataRestoreFailure {
    INSUFFICIENT_STORAGE,
    INVALID_ARCHIVE,
    ROLLBACK_FAILED,
    GENERIC
}

sealed interface DataDeletionState {
    data object Idle : DataDeletionState
    data object Deleting : DataDeletionState
    data class Success(val summary: DataDeletionSummary) : DataDeletionState
    data class Failure(val reason: DataDeletionFailure) : DataDeletionState
}

enum class DataDeletionFailure {
    ROLLBACK_FAILED,
    GENERIC
}
