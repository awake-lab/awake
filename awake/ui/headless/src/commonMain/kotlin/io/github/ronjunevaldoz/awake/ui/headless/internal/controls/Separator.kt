// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.internal.controls

import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.interact
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.UiInteraction
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.withIntrinsicLabelWidth

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.scope.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.scope.pixelPerfectPixel
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.toPx

/** Axis a [separator] draws along -- [Horizontal] (the default) fills width and is
 * [thickness] tall; [Vertical] fills height and is [thickness] wide. */
enum class SeparatorOrientation { Horizontal, Vertical }

fun UiPrimitiveScope.separator(
    thickness: Dp = 1f.dp,
    modifier: UiModifier = Modifier,
    color: Color = context.currentTheme.colors.border,
    orientation: SeparatorOrientation = SeparatorOrientation.Horizontal,
): UiBounds {
    val fallback = when (orientation) {
        SeparatorOrientation.Horizontal -> Dimension.FillMax to Dimension.Fixed(thickness)
        SeparatorOrientation.Vertical -> Dimension.Fixed(thickness) to Dimension.FillMax
    }
    val slot = claimModifiedSlot(modifier.withSizeFallback(fallback.first, fallback.second))
    // Pixel-snap the drawn line, the same way BasicText.kt's glyph emission already snaps every
    // glyph -- a 1px-nominal divider at a sub-pixel y/x position renders as a soft 2px
    // antialiased smear instead of a crisp hairline.
    when (orientation) {
        SeparatorOrientation.Horizontal -> {
            val lineHeight = pixelPerfectPixel(thickness.toPx()).coerceAtLeast(1f)
            val lineY = pixelPerfectPixel(slot.y + (slot.height - lineHeight) / 2f)
            emit(
                UiDrawPrimitive.Quad(
                    pixelPerfectPixel(slot.x),
                    lineY,
                    pixelPerfectPixel(slot.width).coerceAtLeast(1f),
                    lineHeight,
                    color,
                ),
            )
        }

        SeparatorOrientation.Vertical -> {
            val lineWidth = pixelPerfectPixel(thickness.toPx()).coerceAtLeast(1f)
            val lineX = pixelPerfectPixel(slot.x + (slot.width - lineWidth) / 2f)
            emit(
                UiDrawPrimitive.Quad(
                    lineX,
                    pixelPerfectPixel(slot.y),
                    lineWidth,
                    pixelPerfectPixel(slot.height).coerceAtLeast(1f),
                    color,
                ),
            )
        }
    }
    recordSemantic(
        role = UiSemanticRole.Separator,
        id = modifier.testTag,
        bounds = slot,
    )
    return slot
}
