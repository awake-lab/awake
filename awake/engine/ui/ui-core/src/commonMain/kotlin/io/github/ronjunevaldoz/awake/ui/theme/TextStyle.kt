// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.theme

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Sp
import io.github.ronjunevaldoz.awake.ui.sp

/**
 * Groups text-related styling properties.
 */
data class TextStyle(
    val color: Color? = null,
    val size: Sp? = null,
    val scale: Float = 1f,
    val weight: FontWeight = FontWeight.Normal,
    val letterSpacing: Sp = 0f.sp,
) {
    companion object {
        val Default = TextStyle()
    }

    infix fun then(other: TextStyle): TextStyle = TextStyle(
        color = other.color ?: color,
        size = other.size ?: size,
        scale = other.scale,
        weight = other.weight,
        letterSpacing = other.letterSpacing,
    )
}
