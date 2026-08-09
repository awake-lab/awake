// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.core.colors

data class Color(val r: Float = 0f, val g: Float = 0f, val b: Float = 0f, val a: Float = 1f) {
    operator fun get(index: Int): Float = when (index) {
        0 -> r
        1 -> g
        2 -> b
        3 -> a
        else -> throw IndexOutOfBoundsException("Color channel index $index is out of bounds.")
    }

    fun withAlpha(alpha: Float): Color = copy(a = alpha.coerceIn(0f, 1f))

    fun brighten(multiplier: Float): Color = Color(
        r = (r * multiplier).coerceAtMost(1f),
        g = (g * multiplier).coerceAtMost(1f),
        b = (b * multiplier).coerceAtMost(1f),
        a = a,
    )

    fun isTransparent(): Boolean = a <= 0f

    fun lerp(other: Color, fraction: Float): Color = Color(
        r = r + (other.r - r) * fraction.coerceIn(0f, 1f),
        g = g + (other.g - g) * fraction.coerceIn(0f, 1f),
        b = b + (other.b - b) * fraction.coerceIn(0f, 1f),
        a = a + (other.a - a) * fraction.coerceIn(0f, 1f),
    )

    fun toFloatArray(): FloatArray = floatArrayOf(r, g, b, a)

    companion object {
        val Transparent = Color(0f, 0f, 0f, 0f)
        val Black = Color(0f, 0f, 0f, 1f)
        val White = Color(1f, 1f, 1f, 1f)

        fun fromChannels(channels: FloatArray): Color = Color(
            r = channels.getOrElse(0) { 0f },
            g = channels.getOrElse(1) { 0f },
            b = channels.getOrElse(2) { 0f },
            a = channels.getOrElse(3) { 1f },
        )

        /**
         * Parses a hex string like "#RRGGBB" or "#RRGGBBAA".
         */
        fun fromHex(hex: String): Color {
            val h = hex.removePrefix("#")
            return when (h.length) {
                6 -> {
                    val r = h.substring(0, 2).toInt(16) / 255f
                    val g = h.substring(2, 4).toInt(16) / 255f
                    val b = h.substring(4, 6).toInt(16) / 255f
                    Color(r, g, b, 1f)
                }
                8 -> {
                    val r = h.substring(0, 2).toInt(16) / 255f
                    val g = h.substring(2, 4).toInt(16) / 255f
                    val b = h.substring(4, 6).toInt(16) / 255f
                    val a = h.substring(6, 8).toInt(16) / 255f
                    Color(r, g, b, a)
                }
                else -> throw IllegalArgumentException("Invalid hex color: $hex")
            }
        }
    }
}
