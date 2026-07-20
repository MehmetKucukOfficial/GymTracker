package com.gymtracker.ui.screens.progress

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.entities.PhotoAngle
import com.gymtracker.data.entities.ProgressPhoto
import com.gymtracker.data.photos.InsufficientPhotoStorageException
import com.gymtracker.data.photos.ProgressPhotoRepository
import com.gymtracker.data.photos.ProgressPhotoValidationError
import com.gymtracker.data.photos.validateProgressPhotoInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProgressPhotoUiState(
    val isSaving: Boolean = false,
    val message: ProgressPhotoMessage? = null
)

enum class ProgressPhotoMessage {
    SAVED,
    UPGRADED,
    DELETED,
    SAVE_FAILED,
    STORAGE_FULL,
    UPGRADE_FAILED,
    DELETE_FAILED,
    MAINTENANCE_FAILED,
    WEIGHT_OUT_OF_RANGE,
    NOTES_TOO_LONG
}

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val repository: ProgressPhotoRepository
) : ViewModel() {

    val photos = repository.photos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(ProgressPhotoUiState())
    val uiState: StateFlow<ProgressPhotoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.reconcileStorage().onFailure {
                _uiState.update { state -> state.copy(message = ProgressPhotoMessage.MAINTENANCE_FAILED) }
            }
        }
    }

    fun savePhoto(
        uri: Uri,
        angle: PhotoAngle,
        weightKg: Float?,
        notes: String,
        onSaved: () -> Unit = {}
    ) {
        if (_uiState.value.isSaving) return
        when (validateProgressPhotoInput(weightKg, notes)) {
            ProgressPhotoValidationError.WEIGHT_OUT_OF_RANGE -> {
                _uiState.update { it.copy(message = ProgressPhotoMessage.WEIGHT_OUT_OF_RANGE) }
                return
            }
            ProgressPhotoValidationError.NOTES_TOO_LONG -> {
                _uiState.update { it.copy(message = ProgressPhotoMessage.NOTES_TOO_LONG) }
                return
            }
            null -> Unit
        }
        _uiState.update { it.copy(isSaving = true, message = null) }
        viewModelScope.launch {
            val result = repository.importPhoto(uri, angle, weightKg, notes)
            if (result.isSuccess) onSaved()
            _uiState.update {
                it.copy(
                    isSaving = false,
                    message = result.fold(
                        onSuccess = { ProgressPhotoMessage.SAVED },
                        onFailure = { error ->
                            if (error is InsufficientPhotoStorageException) {
                                ProgressPhotoMessage.STORAGE_FULL
                            } else {
                                ProgressPhotoMessage.SAVE_FAILED
                            }
                        }
                    )
                )
            }
        }
    }

    fun deletePhoto(photo: ProgressPhoto) {
        viewModelScope.launch {
            val result = repository.deletePhoto(photo)
            _uiState.update {
                it.copy(
                    message = result.fold(
                        onSuccess = { ProgressPhotoMessage.DELETED },
                        onFailure = { ProgressPhotoMessage.DELETE_FAILED }
                    )
                )
            }
        }
    }

    fun upgradePhoto(
        photo: ProgressPhoto,
        uri: Uri,
        onUpgraded: () -> Unit = {}
    ) {
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true, message = null) }
        viewModelScope.launch {
            val result = repository.upgradePhoto(photo, uri)
            if (result.isSuccess) onUpgraded()
            _uiState.update {
                it.copy(
                    isSaving = false,
                    message = result.fold(
                        onSuccess = { ProgressPhotoMessage.UPGRADED },
                        onFailure = { error ->
                            if (error is InsufficientPhotoStorageException) {
                                ProgressPhotoMessage.STORAGE_FULL
                            } else {
                                ProgressPhotoMessage.UPGRADE_FAILED
                            }
                        }
                    )
                )
            }
        }
    }

    fun messageShown() {
        _uiState.update { it.copy(message = null) }
    }
}
