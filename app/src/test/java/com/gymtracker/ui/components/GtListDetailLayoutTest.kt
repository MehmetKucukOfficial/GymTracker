package com.gymtracker.ui.components

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class GtListDetailLayoutTest {
    @Test
    fun belowExpandedBreakpoint_usesRouteBasedListOnlyLayout() {
        assertEquals(GtListDetailLayout.LIST_ONLY, listDetailLayoutForWidth(839.dp))
    }

    @Test
    fun expandedBreakpoint_keepsListAndPreviewVisible() {
        assertEquals(GtListDetailLayout.LIST_DETAIL, listDetailLayoutForWidth(840.dp))
        assertEquals(GtListDetailLayout.LIST_DETAIL, listDetailLayoutForWidth(1_200.dp))
    }
}
