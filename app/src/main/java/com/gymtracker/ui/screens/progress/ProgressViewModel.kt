package com.gymtracker.ui.screens.progress

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.entities.PhotoAngle
import com.gymtracker.data.entities.ProgressPhoto
import com.gymtracker.data.photos.ProgressPhotoRepository
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
    val message: String? = null
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val repository: ProgressPhotoRepository
) : ViewModel() {

    val photos = repository.photos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(ProgressPhotoUiState())
    val uiState: StateFlow<ProgressPhotoUiState> = _uiState.asStateFlow()

    fun savePhoto(uri: Uri, angle: PhotoAngle, weightKg: Float?, notes: String) {
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true, message = null) }
        viewModelScope.launch {
            val result = repository.importPhoto(uri, angle, weightKg, notes)
            _uiState.update {
                it.copy(
                    isSaving = false,
                    message = result.fold(
                        onSuccess = { "Fotoğraf güvenli şekilde kaydedildi." },
                        onFailure = { error -> error.message ?: "Fotoğraf kaydedilemedi." }
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
                        onSuccess = { "Fotoğraf silindi." },
                        onFailure = { error -> error.message ?: "Fotoğraf silinemedi." }
                    )
                )
            }
        }
    }

    fun messageShown() {
        _uiState.update { it.copy(message = null) }
    }
}
