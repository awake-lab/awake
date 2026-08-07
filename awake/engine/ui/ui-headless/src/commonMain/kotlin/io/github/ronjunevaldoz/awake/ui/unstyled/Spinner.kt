// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.scope.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.frameDeltaSeconds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.scope.resolveStyle
import io.github.ronjunevaldoz.awake.ui.theme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

private const val SPINNER_REVOLUTIONS_PER_SECOND = 1f
private const val SPINNER_DOT_COUNT = 8

/**
 * Real shadcn's `Spinner` is a Lucide loader icon CSS-rotated 360 degrees per second -- this
 * engine has no SVG-icon-rotation pipeline to reproduce that exactly, so this draws the same
 * "orbiting dots fading around a ring" look most spinner components render as instead (a real,
 * distinct animation, not a static substitute -- same honesty bar as [skeleton]'s actual pulse).
 */
fun UiScope.spinner(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
) {
    val slot = claimModifiedSlot(modifier.withSizeFallback(Dimension.Fixed(24f.dp), Dimension.Fixed(24f.dp)))
    val resolved = resolveStyle(style = style)
    val dotColor = resolved.foreground ?: theme.colors.foreground
    val state = widgetState(id)
    val elapsed = state.get("spinnerElapsed", 0f) + frameDeltaSeconds()
    state.set("spinnerElapsed", elapsed)
    val baseAngle = elapsed * SPINNER_REVOLUTIONS_PER_SECOND * 2f * PI.toFloat()

    val centerX = slot.x + slot.width / 2f
    val centerY = slot.y + slot.height / 2f
    val orbitRadius = (minOf(slot.width, slot.height) / 2f) * 0.75f
    val dotDiameter = minOf(slot.width, slot.height) * 0.16f

    for (i in 0 until SPINNER_DOT_COUNT) {
        val angle = baseAngle + (2f * PI.toFloat() * i / SPINNER_DOT_COUNT)
        val alpha = 1f - (i.toFloat() / SPINNER_DOT_COUNT)
        val dotX = centerX + orbitRadius * cos(angle)
        val dotY = centerY + orbitRadius * sin(angle)
        emitFillAndBorder(
            slot = io.github.ronjunevaldoz.awake.ui.layout.UiBounds(
                dotX - dotDiameter / 2f,
                dotY - dotDiameter / 2f,
                dotDiameter,
                dotDiameter
            ),
            fillColor = dotColor.withAlpha(dotColor.a * alpha),
            radiusPx = 0f,
            borderWidth = UiShape.none,
            borderColor = Color.Transparent,
            shapeSpec = UiShapeSpec.Circle
        )
    }

    recordSemantic(
        role = UiSemanticRole.Spinner,
        id = id,
        bounds = slot
    )
}
