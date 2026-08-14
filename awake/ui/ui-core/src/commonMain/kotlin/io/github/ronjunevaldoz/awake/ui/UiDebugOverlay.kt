// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext

/** Colors for [UiSemanticNode.debugOverlayPrimitives] -- one lane per rect kind so bounds,
 * content padding, and clip regions stay visually distinguishable when overlaid together. */
object UiDebugOverlayColors {
    val Bounds = Color(0.2f, 0.6f, 1f, 0.9f)
    val ContentBounds = Color(0.3f, 0.85f, 0.35f, 0.9f)
    val ClippedBounds = Color(1f, 0.35f, 0.3f, 0.9f)
}

private fun rectOutline(slot: UiBounds): UiPath = uiPath {
    moveTo(slot.x, slot.y)
    lineTo(slot.x + slot.width, slot.y)
    lineTo(slot.x + slot.width, slot.y + slot.height)
    lineTo(slot.x, slot.y + slot.height)
    close()
}

/** Renders this node's [UiSemanticNode.bounds] (blue), [UiSemanticNode.contentBounds] (green,
 * if present), and [UiSemanticNode.clippedBounds] (red, if present) as stroked outlines --
 * the wireframe/layout debug overlay for bounds, padding, and clip rects. Emit these primitives
 * AFTER a frame's normal primitives so the wireframe paints on top. */
fun UiSemanticNode.debugOverlayPrimitives(strokeWidth: Dp = 1f.dp): List<UiDrawPrimitive> = buildList {
    add(UiDrawPrimitive.StrokedPath(rectOutline(bounds), UiStroke(strokeWidth), UiDebugOverlayColors.Bounds))
    contentBounds?.let { add(UiDrawPrimitive.StrokedPath(rectOutline(it), UiStroke(strokeWidth), UiDebugOverlayColors.ContentBounds)) }
    clippedBounds?.let { add(UiDrawPrimitive.StrokedPath(rectOutline(it), UiStroke(strokeWidth), UiDebugOverlayColors.ClippedBounds)) }
}

/** Wireframe overlay for every semantic node recorded this frame -- call after [io.github.ronjunevaldoz.awake.ui.context.UiContext.endFrame]
 * (semantic nodes are only cleared on the next [io.github.ronjunevaldoz.awake.ui.context.UiContext.beginFrame]) and append the result to
 * the frame's own primitives to paint the debug wireframe on top. */
fun UiContext.debugOverlayPrimitives(strokeWidth: Dp = 1f.dp): List<UiDrawPrimitive> =
    finishFrame().semantics.flatMap { it.debugOverlayPrimitives(strokeWidth) }
