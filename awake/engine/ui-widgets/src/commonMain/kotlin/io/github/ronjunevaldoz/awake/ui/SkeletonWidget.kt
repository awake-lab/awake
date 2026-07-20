// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import kotlin.math.PI
import kotlin.math.sin

private const val SKELETON_PULSE_PERIOD_SECONDS = 1.6f
private const val SKELETON_PULSE_MIN_ALPHA = 0.5f
private const val SKELETON_PULSE_MAX_ALPHA = 1f

/**
 * Real shadcn's `Skeleton`: a muted placeholder block that pulses opacity while content loads
 * -- same per-widget elapsed-time accumulation as [textField]'s caret blink (`caretBlinkElapsedSeconds`),
 * not a global animation clock, so multiple skeletons on screen don't visibly sync/desync
 * relative to when each was first composed.
 */
fun UiScope.skeleton(
    id: String,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
) {
    val slot = claimModifiedSlot(
        defaultWidth = Dimension.FillMax,
        defaultHeight = Dimension.Fixed(16f.dp),
        modifier = modifier
    )
    val resolved = resolveStyle(style = style, defaults = theme.components.slider)
    val state = widgetState(id)
    val elapsed = state.get("skeletonElapsed", 0f) + context.frameDeltaSeconds()
    state.set("skeletonElapsed", elapsed)
    val phase = (elapsed / SKELETON_PULSE_PERIOD_SECONDS) * (2f * PI.toFloat())
    val pulse = SKELETON_PULSE_MIN_ALPHA + (SKELETON_PULSE_MAX_ALPHA - SKELETON_PULSE_MIN_ALPHA) * ((sin(phase) + 1f) / 2f)
    val baseColor = resolved.background ?: theme.tokens.muted
    emitFillAndBorder(
        slot = slot,
        fillColor = baseColor.withAlpha(baseColor.a * pulse),
        radiusPx = resolved.shape.toPx(),
        borderWidth = UiShape.none,
        borderColor = Color.Transparent
    )

    recordSemantic(
        role = UiSemanticRole.Skeleton,
        id = id,
        bounds = slot
    )
}
