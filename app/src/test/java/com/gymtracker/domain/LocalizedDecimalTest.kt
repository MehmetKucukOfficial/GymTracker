package com.gymtracker.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocalizedDecimalTest {

    @Test
    fun parsesCommaAndDotDecimalInputs() {
        assertEquals(81.5f, parseLocalizedDecimal("81,5"))
        assertEquals(81.5f, parseLocalizedDecimal("81.5"))
        assertEquals(0.75f, parseLocalizedDecimal(",75"))
    }

    @Test
    fun rejectsAmbiguousOrNonFiniteInputs() {
        assertNull(parseLocalizedDecimal("1.234,5"))
        assertNull(parseLocalizedDecimal("NaN"))
        assertNull(parseLocalizedDecimal(""))
    }
}
