// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont

/** [button] with the resolved [UiSlot] alongside the click result -- [toggle] needs the slot
 * to draw its checkmark at the same rect [button] just claimed, without claiming a second
 * (different) slot. Plain [button] below is the common case that only needs the [Boolean]. */
data class UiButtonResult(val clicked: Boolean, val slot: UiSlot)

/** Returns true exactly on the frame the button is released while still hovered (press sets
 * the scope's active id, release+hover fires the click -- standard immediate-mode semantics,
 * avoids "click fires on press over a different widget than release"). [label] is drawn
 * centered over the button's quad when given (a plain colored rectangle otherwise has no
 * indication of what it does). */
fun UiScope.buttonSlot(
    id: String,
    width: Float,
    height: Float,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: UiStyle? = null
): UiButtonResult {
    val slot = claimSlot(modifier.width ?: width, modifier.height ?: height)
    val hovered = hitTest(slot)
    tryClaimActive(id, hovered)
    // Latch on press (tryClaimActive, above); a click fires exactly on the frame the pointer
    // releases while activeId still matched this id AND it's still hovered.
    val wasActiveBeforeRelease = isActive(id)
    releaseActiveIfMatches(id)
    val clicked = wasActiveBeforeRelease && !isActive(id) && hovered
    val resolvedStyle = style ?: theme.button
    emit(UiDrawPrimitive.Quad(slot.x, slot.y, slot.width, slot.height, resolvedStyle.colorFor(UiWidgetState(hovered, isActive(id)))))
    if (label != null && font != null) {
        text(label, slot, font = font, color = theme.labelColor, centered = true)
    }
    return UiButtonResult(clicked, slot)
}

fun UiScope.button(
    id: String,
    width: Float,
    height: Float,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: UiStyle? = null
): Boolean = buttonSlot(id, width, height, label, modifier, style).clicked

/** Toggle/checkbox: returns the NEW checked value (flips on click). [checked] is caller-owned
 * (passed in, new value returned) -- no toggle state stored beyond the shared active-id latch
 * [button] already uses, matching real ImGui idiom (`ImGui::Checkbox(&myBool)` minus the
 * pointer, since Kotlin has no `&Boolean`). */
fun UiScope.toggle(
    id: String,
    checked: Boolean,
    width: Float,
    height: Float,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: UiStyle? = null
): Boolean {
    val (clicked, slot) = buttonSlot(id, width, height, label, modifier, style ?: theme.toggle)
    val newChecked = if (clicked) !checked else checked
    if (newChecked) {
        val inset = minOf(slot.width, slot.height) * 0.2f
        emit(UiDrawPrimitive.Quad(slot.x + inset, slot.y + inset, slot.width - inset * 2, slot.height - inset * 2, theme.checkColor))
    }
    return newChecked
}

/** Continuous drag control: returns the (possibly updated) value for this frame, same
 * caller-owns-the-state idiom [toggle] already uses ([value] passed in, new value returned).
 * Press-inside-track latches the active id (same as [button]'s press edge) so a drag that
 * briefly carries the pointer outside the track's X range (but the button is still held)
 * doesn't let go of the drag -- every frame the active id still matches and the pointer is
 * still down, [Input.pointerX] is remapped to a value in `[min, max]` regardless of Y or
 * whether X is still within the track. */
fun UiScope.slider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    width: Float,
    height: Float,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: UiStyle? = null
): Float {
    val slot = claimSlot(modifier.width ?: width, modifier.height ?: height)
    val hovered = hitTest(slot)
    tryClaimActive(id, hovered)
    val dragging = isActive(id) && Input.pointerDown
    val newValue = if (dragging) sliderValueFromPointerX(Input.pointerX, slot.x, slot.width, min, max) else value
    releaseActiveIfMatches(id)

    val resolvedStyle = style ?: theme.slider
    emit(UiDrawPrimitive.Quad(slot.x, slot.y, slot.width, slot.height, resolvedStyle.colorFor(UiWidgetState(hovered, dragging))))
    val fraction = ((newValue - min) / (max - min)).coerceIn(0f, 1f)
    val handleWidth = (slot.width * fraction).coerceAtLeast(0f)
    if (handleWidth > 0f) {
        emit(UiDrawPrimitive.Quad(slot.x, slot.y, handleWidth, slot.height, theme.checkColor))
    }
    if (label != null && font != null) {
        text(label, slot, font = font, color = theme.labelColor, centered = true)
    }
    return newValue
}

/** Header renders as a [button] labeled with the currently-selected option; when expanded
 * (tracked per-id via [UiScope.widgetState]), one [button] per option (labeled with its own
 * text) is rendered below it, into the overlay layer so it can never be covered by -- or
 * cover -- a sibling widget drawn elsewhere in the same frame. Returns the clicked option's
 * index, or null if nothing was clicked this frame. */
fun UiScope.dropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    width: Float,
    height: Float,
    modifier: UiModifier = UiModifier(),
    style: UiStyle? = null
): Int? {
    val state = widgetState(id)
    val (clicked, slot) = buttonSlot(id, width, height, options.getOrNull(selectedIndex), modifier, style ?: theme.dropdown)
    if (clicked) {
        state.set("expanded", !state.get("expanded", false))
    }
    var picked: Int? = null
    if (state.get("expanded", false)) {
        options.forEachIndexed { index, option ->
            val optionSlot = UiSlot(slot.x, slot.y + slot.height * (index + 1), slot.width, slot.height)
            val optionHovered = hitTest(optionSlot)
            val optionId = "$id.option$index"
            tryClaimActive(optionId, optionHovered)
            val wasActiveBeforeRelease = isActive(optionId)
            releaseActiveIfMatches(optionId)
            val optionClicked = wasActiveBeforeRelease && !isActive(optionId) && optionHovered
            if (optionClicked) picked = index
            val resolvedStyle = style ?: theme.dropdown
            emitOverlay(UiDrawPrimitive.Quad(optionSlot.x, optionSlot.y, optionSlot.width, optionSlot.height, resolvedStyle.colorFor(UiWidgetState(optionHovered, isActive(optionId)))))
            val resolvedFont = font
            if (resolvedFont != null) {
                val textWidth = option.length * resolvedFont.cellSize
                var penX = optionSlot.x + (optionSlot.width - textWidth) / 2f
                val penY = optionSlot.y + (optionSlot.height - resolvedFont.cellSize) / 2f
                for (char in option) {
                    val uv = resolvedFont.uvFor(char)
                    if (uv != null) {
                        emitOverlay(UiDrawPrimitive.Glyph(penX, penY, resolvedFont.cellSize.toFloat(), resolvedFont.cellSize.toFloat(), uv.u0, uv.v0, uv.u1, uv.v1, theme.labelColor))
                    }
                    penX += resolvedFont.cellSize
                }
            }
        }
    }
    return picked
}

/** Draws [label] as a row of glyph quads -- theme-aware: [color] defaults to
 * `theme.labelColor` instead of every call site needing to pass its own color. [centered]
 * reproduces the classic centered-label positioning for [button]/[toggle]/[slider]'s own
 * labels; a standalone `text(...)` call (not attached to a widget) passes `centered = false`
 * to draw starting exactly at the claimed slot's origin. */
fun UiScope.text(
    label: String,
    slot: UiSlot = claimSlot(0f, this.font?.cellSize?.toFloat() ?: 0f),
    font: BitmapFont? = this.font,
    color: FloatArray = theme.labelColor,
    centered: Boolean = false
) {
    checkNotNull(font) { "text() requires a font, either from the UiScope or passed explicitly" }
    val textWidth = label.length * font.cellSize
    var penX = if (centered) slot.x + (slot.width - textWidth) / 2f else slot.x
    val penY = if (centered) slot.y + (slot.height - font.cellSize) / 2f else slot.y
    for (char in label) {
        val uv = font.uvFor(char)
        if (uv != null) {
            emit(UiDrawPrimitive.Glyph(penX, penY, font.cellSize.toFloat(), font.cellSize.toFloat(), uv.u0, uv.v0, uv.u1, uv.v1, color))
        }
        penX += font.cellSize
    }
}

/** Draws [material]'s sampled image (typically a `RenderTarget`-backed `Material` -- see
 * [UiDrawPrimitive.Texture]'s doc comment for why this is untyped) as a screen-space quad.
 * Unlike [text]/[button]/[toggle], this has no hit-testing/interaction state -- purely a
 * draw call, matching the "quad" half of [UiDrawPrimitive]'s existing kinds. */
fun UiScope.textureQuad(width: Float, height: Float, material: Any, modifier: UiModifier = UiModifier()) {
    val slot = claimSlot(modifier.width ?: width, modifier.height ?: height)
    emit(UiDrawPrimitive.Texture(slot.x, slot.y, slot.width, slot.height, material))
}
