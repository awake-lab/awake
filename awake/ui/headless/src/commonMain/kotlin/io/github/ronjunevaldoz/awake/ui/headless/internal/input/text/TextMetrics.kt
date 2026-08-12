// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.internal.input.text

import io.github.ronjunevaldoz.awake.ui.UiDensity
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle

/** The line advance and inter-line gap used consistently by measurement and painting. */
internal data class UiTextLineMetrics(
    val lineHeightPx: Float,
    val lineGapPx: Float,
)

internal fun resolveTextLineMetrics(
    font: UiFont,
    glyphPx: Float,
    textStyle: TextStyle,
): UiTextLineMetrics {
    val intrinsicLineHeightPx = glyphPx * font.lineHeightEm
    val authoredLineHeightPx = textStyle.lineHeight?.let { it.value * UiDensity.scale * UiDensity.fontScale }
    return if (authoredLineHeightPx != null) {
        // An authored line-height is the complete line box/advance, so it already includes
        // inter-line spacing. Never add the fallback gap on top of it.
        UiTextLineMetrics(
            lineHeightPx = maxOf(intrinsicLineHeightPx, authoredLineHeightPx),
            lineGapPx = 0f,
        )
    } else {
        UiTextLineMetrics(
            lineHeightPx = intrinsicLineHeightPx,
            lineGapPx = glyphPx * 0.25f,
        )
    }
}
