package com.gymtracker.ui.screens.home

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeLayoutConfigTest {

    @Test
    fun compactWidth_usesTwoMetricColumnsAtDefaultFontScale() {
        val config = homeLayoutConfig(width = 360.dp, fontScale = 1f)

        assertFalse(config.isExpanded)
        assertEquals(2, config.metricColumns)
    }

    @Test
    fun expandedWidth_usesFourMetricColumnsAtDefaultFontScale() {
        val config = homeLayoutConfig(width = 840.dp, fontScale = 1f)

        assertTrue(config.isExpanded)
        assertEquals(4, config.metricColumns)
    }

    @Test
    fun largeFontScale_usesOneColumnAtEveryWidth() {
        assertEquals(1, homeLayoutConfig(width = 360.dp, fontScale = 2f).metricColumns)
        assertEquals(1, homeLayoutConfig(width = 1_200.dp, fontScale = 2f).metricColumns)
    }
}
