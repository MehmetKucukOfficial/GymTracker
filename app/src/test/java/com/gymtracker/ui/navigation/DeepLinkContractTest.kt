package com.gymtracker.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class DeepLinkContractTest {

    @Test
    fun everyDestination_hasOneStableExternalPattern() {
        assertEquals(
            listOf(
                "gymtracker://app/home",
                "gymtracker://app/workouts",
                "gymtracker://app/workouts/{sessionId}",
                "gymtracker://app/programs",
                "gymtracker://app/programs/{programId}",
                "gymtracker://app/nutrition",
                "gymtracker://app/progress",
                "gymtracker://app/progress/measurements",
                "gymtracker://app/progress/photos",
                "gymtracker://app/progress/load"
            ),
            DeepLinkContract.destinations.map(DeepLinkContract::patternFor)
        )
    }

    @Test
    fun supportedUris_resolveToInternalRoutes() {
        assertEquals("home", DeepLinkContract.routeFromUri("gymtracker://app/home"))
        assertEquals("workouts/42", DeepLinkContract.routeFromUri("gymtracker://app/workouts/42"))
        assertEquals("programs/5", DeepLinkContract.routeFromUri("GYMTRACKER://APP/programs/5"))
        assertEquals(
            "progress/photos",
            DeepLinkContract.routeFromUri("gymtracker://app/progress/photos?source=notification")
        )
    }

    @Test
    fun malformedOrUntrustedUris_areRejected() {
        assertNull(DeepLinkContract.routeFromUri("https://app/progress"))
        assertNull(DeepLinkContract.routeFromUri("gymtracker://evil/progress"))
        assertNull(DeepLinkContract.routeFromUri("gymtracker://app/workouts/0"))
        assertNull(DeepLinkContract.routeFromUri("gymtracker://app/programs/-1"))
        assertNull(DeepLinkContract.routeFromUri("gymtracker://app/unknown"))
        assertThrows(IllegalArgumentException::class.java) {
            DeepLinkContract.uriForRoute("workouts/{sessionId}")
        }
    }

    @Test
    fun positiveNavigationType_rejectsZeroNegativeAndNonNumericIds() {
        assertEquals(42L, PositiveLongNavType.parseValue("42"))
        assertThrows(IllegalArgumentException::class.java) { PositiveLongNavType.parseValue("0") }
        assertThrows(IllegalArgumentException::class.java) { PositiveLongNavType.parseValue("-1") }
        assertThrows(NumberFormatException::class.java) { PositiveLongNavType.parseValue("abc") }
    }
}
