package com.gymtracker.data.photos

enum class ProgressPhotoValidationError {
    WEIGHT_OUT_OF_RANGE,
    NOTES_TOO_LONG
}

const val MAX_PROGRESS_PHOTO_NOTES_LENGTH = 500

fun validateProgressPhotoInput(
    weightKg: Float?,
    notes: String
): ProgressPhotoValidationError? {
    if (weightKg?.let { !it.isFinite() || it !in 20f..400f } == true) {
        return ProgressPhotoValidationError.WEIGHT_OUT_OF_RANGE
    }
    if (notes.length > MAX_PROGRESS_PHOTO_NOTES_LENGTH) {
        return ProgressPhotoValidationError.NOTES_TOO_LONG
    }
    return null
}
