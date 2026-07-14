package com.gymtracker.domain

private val DECIMAL_INPUT = Regex("^[+-]?(?:\\d+(?:[.,]\\d+)?|[.,]\\d+)$")

/**
 * Accepts the decimal separators users can enter with Turkish or Latin keyboards.
 * Grouping separators are intentionally unsupported because fitness forms contain
 * small scalar values where accepting an ambiguous `1.234` would be unsafe.
 */
fun parseLocalizedDecimal(value: String): Float? {
    val trimmed = value.trim()
    if (!DECIMAL_INPUT.matches(trimmed)) return null
    return trimmed
        .replace(',', '.')
        .toFloatOrNull()
        ?.takeIf(Float::isFinite)
}
