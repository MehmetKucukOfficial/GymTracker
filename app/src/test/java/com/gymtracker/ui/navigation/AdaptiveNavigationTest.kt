package com.gymtracker.ui.navigation

import androidx.compose.ui.unit.dp
import com.gymtracker.ui.components.GtNavigationType
import com.gymtracker.ui.components.navigationTypeForWidth
import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveNavigationTest {
    @Test
    fun compactWidthUsesBottomBarAndMediumWidthUsesRail() {
        assertEquals(GtNavigationType.BOTTOM_BAR, navigationTypeForWidth(599.dp))
        assertEquals(GtNavigationType.RAIL, navigationTypeForWidth(600.dp))
        assertEquals(GtNavigationType.RAIL, navigationTypeForWidth(840.dp))
    }
}
