// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.graphics.emitShimmerOverlay
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.scope.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.scope.frameDeltaSeconds
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.scope.resolveStyle
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.toPx
import kotlin.math.PI
import kotlin.math.sin

private const val SKELETON_PULSE_PERIOD_SECONDS = 1.6f
private const val SKELETON_PULSE_MIN_ALPHA = 0.5f
private const val SKELETON_PULSE_MAX_ALPHA = 1f

/**
 * Real shadcn's `Skeleton`: a muted placeholder block that pulses opacity while content loads
 * -- same per-widget elapsed-time accumulation as [io.github.ronjunevaldoz.awake.ui.unstyled.input.text.textField]'s caret blink (`caretBlinkElapsedSeconds`),
 * not a global animation clock, so multiple skeletons on screen don't visibly sync/desync
 * relative to when each was first composed.
 */
fun UiScope.skeleton(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    shimmer: Boolean = false,
) {
    val slot =
        claimModifiedSlot(modifier.withSizeFallback(Dimension.FillMax, Dimension.Fixed(16f.dp)))
    val resolved = resolveStyle(style = style, defaults = theme.components.slider)
    val state = widgetState(id)
    val elapsed = state.get("skeletonElapsed", 0f) + frameDeltaSeconds()
    state.set("skeletonElapsed", elapsed)
    val phase = (elapsed / SKELETON_PULSE_PERIOD_SECONDS) * (2f * PI.toFloat())
    val pulse =
        SKELETON_PULSE_MIN_ALPHA + (SKELETON_PULSE_MAX_ALPHA - SKELETON_PULSE_MIN_ALPHA) * (
            (
                sin(
                    phase,
                ) + 1f
                ) / 2f
            )
    val baseColor = resolved.background ?: theme.colors.muted
    val radiusPx = resolved.shape.toPx()
    emitFillAndBorder(
        slot = slot,
        fillColor = baseColor.withAlpha(baseColor.a * pulse),
        radiusPx = radiusPx,
        borderWidth = UiShape.none,
        borderColor = Color.Transparent,
    )
    if (shimmer) {
        emitShimmerOverlay(id = id, slot = slot, radiusPx = radiusPx)
    }

    recordSemantic(
        role = UiSemanticRole.Skeleton,
        id = id,
        bounds = slot,
    )
}
