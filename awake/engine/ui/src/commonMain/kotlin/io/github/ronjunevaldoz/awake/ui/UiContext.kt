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
 * `VulkanGameApplication.onRender`/`WebGpuGameApplication.onRender`), then get a [UiScope]
 * from [column]/[absolute] and call widget functions (`button`/`toggle`/etc, defined as
 * extension functions on [UiScope] in `Widgets.kt`) in any order, then [endFrame] to collect
 * this frame's [UiDrawPrimitive]s for the renderer.
 *
 * Ids are caller-supplied stable strings (e.g. `"debug-toggle"`) -- no auto-disambiguation
 * (`##` suffixes) yet, a known simplification for this widget count.
 */
class UiContext {
    private var activeId: String? = null
    private val widgetStates = HashMap<String, WidgetState>()
    private val primitives = ArrayList<UiDrawPrimitive>()
    private val overlayPrimitives = ArrayList<UiDrawPrimitive>()

    fun beginFrame(screenWidth: Float, screenHeight: Float) {
        // screenWidth/screenHeight are unused by UiContext itself today (hit-testing only
        // needs Input.pointerX/Y, both already in the same pixel space) -- kept as
        // parameters so a future clip-rect/scissor feature has them without an API change.
        primitives.clear()
        overlayPrimitives.clear()
    }

    /** Reserves a vertical auto-stacking layout region -- see [ColumnScope]. */
    fun column(x: Float, y: Float, width: Float, font: BitmapFont? = null, theme: UiTheme = DefaultUiTheme, gap: Float = 8f): ColumnScope =
        ColumnScope(this, font, theme, x, y, width, gap)

    /** One-shot manual placement at an exact x/y -- e.g. the HUD text readout or a minimap
     * thumbnail that isn't part of any auto-layout column. Goes through the exact same
     * [UiScope] surface as every other widget; not a special case. */
    fun absolute(x: Float, y: Float, font: BitmapFont? = null, theme: UiTheme = DefaultUiTheme): AbsoluteScope =
        AbsoluteScope(this, font, theme, x, y)

    /** Publishes this frame's [activeId] state to [Input.pointerCapturedByUi] before handing
     * back the frame's draw primitives, so scene-facing drag consumers (
     * [io.github.ronjunevaldoz.awake.scene.systems.OrbitCameraSystem]/`FreeFlyCameraSystem`)
     * know a widget already claimed the pointer this frame. Done here (end of the UI's own
     * frame) rather than in [beginFrame] (start of the *next* UI frame) so the flag reflects
     * "as of the widgets that just ran," not a stale value carried from two frames ago.
     *
     * Overlay primitives are appended last -- always painted on top of this frame's regular
     * primitives, regardless of which widget called [UiScope.emit] vs [UiScope.emitOverlay]
     * or in what order. This is the structural fix for dropdown option lists overlapping
     * sibling widgets: the option rows always go through [UiScope.emitOverlay]. */
    fun endFrame(): List<UiDrawPrimitive> {
        Input.pointerCapturedByUi = activeId != null
        return primitives + overlayPrimitives
    }

    // --- Shared state accessors, delegated to by AbstractUiScope in Layout.kt. Internal, not
    // part of the widget-authoring surface -- that's UiScope itself.

    internal fun hitTestInternal(slot: UiSlot): Boolean =
        Input.pointerX in slot.x..(slot.x + slot.width) && Input.pointerY in slot.y..(slot.y + slot.height)

    internal fun isActiveInternal(id: String): Boolean = activeId == id

    internal fun tryClaimActiveInternal(id: String, hovered: Boolean) {
        if (hovered && Input.pointerDown && activeId == null) activeId = id
    }

    internal fun releaseActiveIfMatchesInternal(id: String) {
        if (!Input.pointerDown && activeId == id) activeId = null
    }

    internal fun emitInternal(p: UiDrawPrimitive) {
        primitives += p
    }

    internal fun emitOverlayInternal(p: UiDrawPrimitive) {
        overlayPrimitives += p
    }

    internal fun widgetStateInternal(id: String): WidgetState = widgetStates.getOrPut(id) { WidgetState() }
}

/** Pure value-from-pointer-position math for `Widgets.kt`'s `slider`, pulled out to a
 * top-level function so it's unit-testable without an [Input]/GPU-backed [UiContext]
 * instance (see this project's "no app-layer test doubles, push logic into pure functions"
 * convention). Maps [pointerX]'s position within the track `[trackX, trackX + trackW]` to a
 * value in `[min, max]`, clamping (not extrapolating) for a [pointerX] outside the track's
 * bounds -- a drag that overshoots the track while still held should pin at [min]/[max], not
 * keep increasing past them. */
fun sliderValueFromPointerX(pointerX: Float, trackX: Float, trackW: Float, min: Float, max: Float): Float {
    if (trackW <= 0f) return min
    val fraction = ((pointerX - trackX) / trackW).coerceIn(0f, 1f)
    return min + fraction * (max - min)
}
