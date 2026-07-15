package com.gymtracker.data.photos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProgressPhotoRulesTest {

    @Test
    fun validateProgressPhotoInput_acceptsOptionalOrRealisticWeight() {
        assertNull(validateProgressPhotoInput(null, ""))
        assertNull(validateProgressPhotoInput(82.5f, "Haftalık kontrol"))
        assertEquals(
            ProgressPhotoValidationError.WEIGHT_OUT_OF_RANGE,
            validateProgressPhotoInput(10f, "")
        )
    }

    @Test
    fun validateProgressPhotoInput_rejectsOverlongNotes() {
        assertEquals(
            ProgressPhotoValidationError.NOTES_TOO_LONG,
            validateProgressPhotoInput(null, "x".repeat(MAX_PROGRESS_PHOTO_NOTES_LENGTH + 1))
        )
    }
}
