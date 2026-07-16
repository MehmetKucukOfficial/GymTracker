package com.gymtracker.ui.navigation

import android.os.Bundle
import androidx.navigation.NavType
import java.net.URI

object DeepLinkContract {
    const val SCHEME = "gymtracker"
    const val HOST = "app"
    const val BASE_URI = "$SCHEME://$HOST"

    val destinations: List<Screen> = listOf(
        Screen.Home,
        Screen.Workout,
        Screen.WorkoutDetail,
        Screen.Programs,
        Screen.ProgramDetail,
        Screen.Nutrition,
        Screen.Progress,
        Screen.Measurements,
        Screen.ProgressPhotos,
        Screen.Fatigue
    )

    fun patternFor(screen: Screen): String = "$BASE_URI/${screen.route}"

    fun uriForRoute(route: String): String {
        require(isSupportedConcreteRoute(route)) { "Unsupported deep-link route: $route" }
        return "$BASE_URI/$route"
    }

    fun routeFromUri(uriValue: String): String? {
        val uri = runCatching { URI(uriValue) }.getOrNull() ?: return null
        if (!uri.scheme.equals(SCHEME, ignoreCase = true)) return null
        if (!uri.host.equals(HOST, ignoreCase = true)) return null
        val route = uri.path.orEmpty().trim('/')
        return route.takeIf(::isSupportedConcreteRoute)
    }

    private fun isSupportedConcreteRoute(route: String): Boolean {
        if (route.isBlank() || route.contains('{') || route.contains('}')) return false
        if (route in STATIC_ROUTES) return true
        return positiveDetailId(route, Screen.Workout.route) != null ||
            positiveDetailId(route, Screen.Programs.route) != null
    }

    private fun positiveDetailId(route: String, prefix: String): Long? {
        val segments = route.split('/')
        if (segments.size != 2 || segments.first() != prefix) return null
        return segments.last().toLongOrNull()?.takeIf { it > 0 }
    }

    private val STATIC_ROUTES = setOf(
        Screen.Home.route,
        Screen.Workout.route,
        Screen.Programs.route,
        Screen.Nutrition.route,
        Screen.Progress.route,
        Screen.Measurements.route,
        Screen.ProgressPhotos.route,
        Screen.Fatigue.route
    )
}

object PositiveLongNavType : NavType<Long>(isNullableAllowed = false) {
    override fun put(bundle: Bundle, key: String, value: Long) {
        bundle.putLong(key, value)
    }

    override fun get(bundle: Bundle, key: String): Long? =
        if (bundle.containsKey(key)) bundle.getLong(key) else null

    override fun parseValue(value: String): Long = value.toLong().also {
        require(it > 0) { "Navigation ID must be positive" }
    }
}
