// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont

/**
 * Minimal immediate-mode UI context -- ImGui's own architecture (hot/active id tracking, no
 * retained widget tree, no ECS entities), deliberately not declarative/Compose-style (no
 * composer/recomposer needed for this small a widget set) and not backed by [io.github
 * .ronjunevaldoz.awake.ecs.World] (widgets have no persistent gameplay state).
 *
 * Usage: call [beginFrame] once per real frame (not fixed-timestep -- see
 * `VulkanGameApplication.onRender`/`WebGpuGameApplication.onRender`), then widget functions
 * ([button]/[toggle]/[dropdown]) in any order, then [endFrame] to collect this frame's
 * [UiDrawPrimitive]s for the renderer.
 *
 * Ids are caller-supplied stable strings (e.g. `"debug-toggle"`) -- no auto-disambiguation
 * (`##` suffixes) yet, a known simplification for this widget count.
 */
class UiContext {
    private val widgetStates = HashMap<String, WidgetState>()
    private val primitives = ArrayList<UiDrawPrimitive>()

    /** The id whose press+release-while-hovered fires the next click -- persists across
     * frames until the pointer is released, so a press-drag-release still resolves against
     * the widget the press started on (not whatever's under the pointer at release). */
    private var activeId: String? = null

    fun beginFrame(screenWidth: Float, screenHeight: Float) {
        // screenWidth/screenHeight are unused by UiContext itself today (hit-testing only
        // needs Input.pointerX/Y, both already in the same pixel space) -- kept as
        // parameters so a future clip-rect/scissor feature has them without an API change.
        primitives.clear()
    }

    /** Returns true exactly on the frame the button is released while still hovered
     * (press sets [activeId], release+hover fires the click -- standard immediate-mode
     * semantics, avoids "click fires on press over a different widget than release").
     * [label]/[font] are optional -- when both are given, the label is drawn centered over
     * the button's quad (a plain colored rectangle otherwise has no indication of what it
     * does). */
    fun button(id: String, x: Float, y: Float, w: Float, h: Float, label: String? = null, font: BitmapFont? = null): Boolean {
        val hovered = hitTest(x, y, w, h)
        var clicked = false
        if (hovered && Input.pointerDown && activeId == null) {
            activeId = id
        }
        if (!Input.pointerDown && activeId == id) {
            clicked = hovered
            activeId = null
        }
        val active = activeId == id
        primitives += UiDrawPrimitive.Quad(x, y, w, h, colorFor(hovered, active))
        if (label != null && font != null) drawCenteredLabel(x, y, w, h, label, font)
        return clicked
    }

    /** Toggle/checkbox: returns the NEW checked value (flips on click). [checked] is
     * caller-owned (passed in, new value returned) -- [UiContext] itself stores no toggle
     * state, matching real ImGui idiom (`ImGui::Checkbox(&myBool)` minus the pointer, since
     * Kotlin has no `&Boolean`). [label]/[font] are optional, same as [button]. */
    fun toggle(
        id: String,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        checked: Boolean,
        label: String? = null,
        font: BitmapFont? = null
    ): Boolean {
        val clicked = button(id, x, y, w, h, label, font)
        val newChecked = if (clicked) !checked else checked
        if (newChecked) {
            val inset = minOf(w, h) * 0.2f
            primitives += UiDrawPrimitive.Quad(x + inset, y + inset, w - inset * 2, h - inset * 2, CHECK_COLOR)
        }
        return newChecked
    }

    /** Header renders as a [button] labeled with the currently-selected option; when
     * expanded (tracked per-[id] in [widgetStates]), one [button] per option (labeled with
     * its own text) is rendered below it. Returns the clicked option's index, or null if
     * nothing was clicked this frame. [font] is optional -- omit it to fall back to the old
     * unlabeled quad-only rendering. */
    fun dropdown(
        id: String,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        options: List<String>,
        selectedIndex: Int,
        font: BitmapFont? = null
    ): Int? {
        val state = widgetStates.getOrPut(id) { WidgetState() }
        if (button(id, x, y, w, h, options.getOrNull(selectedIndex), font)) {
            state.boolFlag = !state.boolFlag
        }
        var picked: Int? = null
        if (state.boolFlag) {
            options.forEachIndexed { index, option ->
                if (button("$id.option$index", x, y + h * (index + 1), w, h, option, font)) {
                    picked = index
                }
            }
        }
        return picked
    }

    /** Draws [text] as a row of [font]-sized glyph quads starting at ([x], [y]), advancing
     * by [font]'s cell size each character -- monospace only, no kerning. Characters not in
     * [font] are skipped (advance still applies) rather than drawn as a missing-glyph box. */
    fun text(x: Float, y: Float, text: String, color: FloatArray, font: BitmapFont) {
        var penX = x
        for (char in text) {
            val uv = font.uvFor(char)
            if (uv != null) {
                primitives += UiDrawPrimitive.Glyph(
                    x = penX,
                    y = y,
                    w = font.cellSize.toFloat(),
                    h = font.cellSize.toFloat(),
                    u0 = uv.u0,
                    v0 = uv.v0,
                    u1 = uv.u1,
                    v1 = uv.v1,
                    color = color
                )
            }
            penX += font.cellSize
        }
    }

    fun endFrame(): List<UiDrawPrimitive> = primitives.toList()

    /** Centers [label] (in [font]'s monospace cell metrics) within the ([x], [y], [w], [h])
     * rect -- used by [button]/[toggle]/[dropdown] to draw their own label, since a plain
     * colored quad gives no indication of what the widget does or which option is selected. */
    private fun drawCenteredLabel(x: Float, y: Float, w: Float, h: Float, label: String, font: BitmapFont) {
        val textWidth = label.length * font.cellSize
        val labelX = x + (w - textWidth) / 2f
        val labelY = y + (h - font.cellSize) / 2f
        text(labelX, labelY, label, LABEL_COLOR, font)
    }

    private fun hitTest(x: Float, y: Float, w: Float, h: Float): Boolean =
        Input.pointerX in x..(x + w) && Input.pointerY in y..(y + h)

    private fun colorFor(hovered: Boolean, active: Boolean): FloatArray = when {
        active -> ACTIVE_COLOR
        hovered -> HOVER_COLOR
        else -> BASE_COLOR
    }

    private companion object {
        val BASE_COLOR = floatArrayOf(0.25f, 0.25f, 0.28f, 0.9f)
        val HOVER_COLOR = floatArrayOf(0.35f, 0.35f, 0.4f, 0.9f)
        val ACTIVE_COLOR = floatArrayOf(0.5f, 0.5f, 0.6f, 0.9f)
        val CHECK_COLOR = floatArrayOf(0.2f, 0.8f, 0.3f, 1f)
        val LABEL_COLOR = floatArrayOf(1f, 1f, 1f, 1f)
    }
}
