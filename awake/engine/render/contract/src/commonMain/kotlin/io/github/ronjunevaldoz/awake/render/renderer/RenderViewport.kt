// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.renderer

/**
 * A sub-rect of the render surface, in framebuffer pixels with the origin at the top-left --
 * the same coordinate space a UI frame's own bounds are in, so an editor can hand a panel's
 * bounds straight to [Renderer.sceneViewport].
 *
 * [aspect] is the rect's own, not the surface's: a scene confined to a narrow panel must be
 * projected for that panel, otherwise its content is stretched by however much the surrounding
 * chrome takes up.
 */
data class RenderViewport(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
) {
    val aspect: Float get() = width / height

    /**
     * This rect trimmed to a [surfaceWidth] x [surfaceHeight] surface, or `null` when nothing
     * of it is left inside.
     *
     * Backends reject a viewport that leaves the framebuffer, and a caller supplying panel
     * bounds cannot always know the surface size for the frame the renderer is about to use --
     * a window resize changes the swapchain before the next UI frame is measured.
     */
    fun clampedTo(surfaceWidth: Float, surfaceHeight: Float): RenderViewport? {
        val left = x.coerceIn(0f, surfaceWidth)
        val top = y.coerceIn(0f, surfaceHeight)
        val right = (x + width).coerceIn(0f, surfaceWidth)
        val bottom = (y + height).coerceIn(0f, surfaceHeight)
        if (right <= left || bottom <= top) return null
        return RenderViewport(left, top, right - left, bottom - top)
    }
}
