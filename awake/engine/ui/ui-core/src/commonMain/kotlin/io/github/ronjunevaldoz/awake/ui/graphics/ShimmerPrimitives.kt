// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.graphics

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.RepeatMode
import io.github.ronjunevaldoz.awake.ui.UiLinearGradient
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.animateFloatRepeatable
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.px

/**
 * The moving diagonal-free horizontal band a shimmer sweeps -- [x]/[width] are already in the
 * same pixel space as the [UiBounds] the shimmer plays over, [phase] is the raw 0..1 sweep
 * fraction (see [shimmerBand]) exposed for callers that need to derive their own per-sample
 * falloff, the way [io.github.ronjunevaldoz.awake.ui.unstyled.input.text.renderTextBlock]'s
 * per-glyph 3-point lerp does.
 */
data class ShimmerBand(val x: Float, val width: Float, val phase: Float)

/**
 * Widget-agnostic shimmer phase/band math -- the part of a shimmer effect that has nothing to do
 * with what's being drawn under it. Any widget (skeleton, card, avatar, or
 * [io.github.ronjunevaldoz.awake.ui.unstyled.input.text.renderTextBlock]'s own glyph sweep) can
 * drive its own draw pass off this. One-directional sweep loop (0 -> 1, snap back to 0, repeat)
 * -- not a ping-pong bounce -- via [RepeatMode.Restart]; see `UiAnimationTest.kt`'s
 * `shimmerSweepPhaseIsAOneDirectionalLoopNotAPingPongBounce`.
 */
fun UiScope.shimmerBand(id: String, slot: UiBounds, durationMs: Float = 1200f): ShimmerBand {
    val phase = animateFloatRepeatable(
        id = "__shimmer_phase__$id",
        initialValue = 0f,
        targetValue = 1f,
        durationMs = durationMs,
        repeatMode = RepeatMode.Restart
    )
    val width = (slot.width * 1.5f).coerceAtLeast(160f)
    val x = slot.x - width + (slot.width + width) * phase
    return ShimmerBand(x = x, width = width, phase = phase)
}

/**
 * Real shadcn-compose's shimmer uses GPU offscreen compositing (Offscreen graphicsLayer +
 * BlendMode.SrcAtop) so it works over ANY already-drawn content without knowing its shape in
 * advance. Awake has no offscreen-render-target/blend-mode primitive on either backend yet, so
 * this is a clip-based workaround: it needs the shape declared upfront (shapeSpec/radiusPx), and
 * can't shimmer arbitrary content like an icon glyph. Fine for skeletons/cards/avatars (shape is
 * always known). TODO: real offscreen-render-target + SrcAtop-blend primitive on both backends,
 * ported from Modifier.shimmer's actual mechanism, if arbitrary-content shimmer is ever needed.
 */
fun UiScope.emitShimmerOverlay(
    id: String,
    slot: UiBounds,
    shapeSpec: UiShapeSpec? = null,
    radiusPx: Float = 0f,
    highlightColor: Color = Color.White.withAlpha(0.6f),
    durationMs: Float = 1200f
) {
    val band = shimmerBand(id, slot, durationMs)
    clip(shapeSpec ?: UiShapeSpec.RoundedRectangle(radiusPx.px), slot) {
        gradientRect(
            slot = UiBounds(x = band.x, y = slot.y, width = band.width, height = slot.height),
            gradient = UiLinearGradient(
                topLeft = Color.Transparent,
                topRight = highlightColor,
                bottomRight = highlightColor,
                bottomLeft = Color.Transparent
            )
        )
    }
}
