package io.github.ronjunevaldoz.awake.core.fonts

import io.github.ronjunevaldoz.awake.core.math.Vec2

/**
 * @param code character ascii code
 * @param size width and height of the glyph
 * @param bearing offset from baseline to left and top of glyph
 * @param advance offset to advance to next glyph
 */
data class Glyph(
    val code: Int,
    val size: Vec2,
    val bearing: Vec2,
    val advance: Int
)