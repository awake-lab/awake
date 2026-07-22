// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled.input

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.core.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.recordSemantic
import io.github.ronjunevaldoz.awake.ui.unstyled.paintSurface
import io.github.ronjunevaldoz.awake.ui.unstyled.resolveSurface

private const val PROGRESS_TRACK_HEIGHT_DP = 8f

/**
 * Non-interactive progress track -- [slider]'s track/fill painting without the knob or drag
 * handling, since a real shadcn `Progress` bar has neither. [value] is a 0f..1f fraction, not
 * a min/max pair like [slider]: `Progress` has no user-facing range concept, only "how much
 * is done."
 */
fun UiScope.progressBar(
    id: String,
    value: Float,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
) {
    val theme = context.currentTheme
    val surface = resolveSurface(
        defaultWidth = Dimension.FillMax,
        defaultHeight = Dimension.Fixed(PROGRESS_TRACK_HEIGHT_DP.dp),
        modifier = modifier,
        style = style,
        defaults = theme.components.slider
    )
    paintSurface(slot = surface.slot, resolved = surface.resolved)
    val fraction = value.coerceIn(0f, 1f)
    val fillWidth = (surface.slot.width * fraction).coerceAtLeast(0f)
    if (fillWidth > 0f) {
        emitFillAndBorder(
            slot = UiSlot(surface.slot.x, surface.slot.y, fillWidth, surface.slot.height),
            fillColor = surface.resolved.foreground ?: theme.tokens.primary,
            radiusPx = 0f,
            borderWidth = UiShape.none,
            borderColor = Color.Transparent,
            shapeSpec = UiShapeSpec.Pill
        )
    }
    recordSemantic(
        role = UiSemanticRole.Text,
        id = id,
        label = "${(fraction * 100).toInt()}%",
        bounds = surface.slot
    )
}
