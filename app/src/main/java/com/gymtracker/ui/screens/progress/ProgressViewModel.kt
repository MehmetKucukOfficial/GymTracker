package com.gymtracker.ui.screens.progress

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymtracker.data.dao.ProgressPhotoDao
import com.gymtracker.data.entities.PhotoAngle
import com.gymtracker.data.entities.ProgressPhoto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val dao: ProgressPhotoDao
) : ViewModel() {

    val photos = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun savePhoto(uri: Uri, angle: PhotoAngle, weightKg: Float?, notes: String) {
        viewModelScope.launch {
            dao.insert(ProgressPhoto(
                photoUri = uri.toString(),
                angle = angle,
                weightKg = weightKg,
                notes = notes
            ))
        }
    }

    fun deletePhoto(photo: ProgressPhoto) {
        viewModelScope.launch { dao.delete(photo) }
    }
}
