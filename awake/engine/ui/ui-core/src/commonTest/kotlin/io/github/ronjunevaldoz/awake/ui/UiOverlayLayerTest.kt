// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.graphics.clip
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Regression coverage for the dropdown/dialog "text renders under its own background" bug:
 * overlay content must always sort after ALL base-layer content in the final frame, no matter
 * what order it was emitted in during composition -- [io.github.ronjunevaldoz.awake.ui.context.UiRenderCollector.endFrame]
 * concatenates base primitives before overlay primitives, so anything that silently falls back
 * to the base layer while nested inside an overlay scope (a popup, a dialog) paints UNDER
 * content emitted later in the same overlay scope, even though it was drawn first.
 */
class UiOverlayLayerTest {

    @Test
    fun childAbsoluteInheritsOverlayFromItsCallingScope() {
        val ui = UiContext()
        ui.beginFrame(200f, 100f, testSnapshot())

        // A base-layer marker, emitted first -- e.g. ordinary page content behind a popup.
        ui.createColumn(x = 0f, y = 0f, width = 100f)
            .emit(UiDrawPrimitive.Quad(0f, 0f, 10f, 10f, Color(1f, 0f, 0f, 1f)))

        // buttonSlot's content-lambda overload (used by dropdown menu items, among others)
        // routes its content through UiScope.childAbsolute(...) -- this must inherit the
        // overlay flag from whatever scope is doing the popup rendering.
        val overlayColumn = ui.createColumn(x = 0f, y = 0f, width = 100f, overlayOnly = true)
        val overlayMarker = UiDrawPrimitive.Quad(0f, 0f, 10f, 10f, Color(0f, 0f, 1f, 1f))
        overlayColumn.childAbsolute(UiBounds(0f, 0f, 10f, 10f)).emit(overlayMarker)

        val primitives = ui.endFrame()
        assertEquals(
            overlayMarker,
            primitives.last(),
            "childAbsolute() must inherit emitsToOverlay from its calling scope -- if it silently " +
                "falls back to the base layer, overlay content painted through it (e.g. dropdown " +
                "menu item text via buttonSlot's content-lambda overload) sorts before, and gets " +
                "painted under, its own popup surface.",
        )
    }

    @Test
    fun emitFillAndBorderDefaultsToItsScopesOverlayLayer() {
        val ui = UiContext()
        ui.beginFrame(200f, 100f, testSnapshot())

        // A base-layer marker, emitted first.
        ui.createColumn(x = 0f, y = 0f, width = 100f)
            .emit(UiDrawPrimitive.Quad(0f, 0f, 10f, 10f, Color(1f, 0f, 0f, 1f)))

        // A fill/border call from an overlay scope with no explicit `overlay` argument -- this
        // is exactly how surface()/paintSurface() call it.
        val overlayColumn = ui.createColumn(x = 0f, y = 0f, width = 100f, overlayOnly = true)
        overlayColumn.emitFillAndBorder(
            slot = UiBounds(0f, 0f, 10f, 10f),
            fillColor = Color(0f, 0f, 1f, 1f),
            radiusPx = 0f,
            borderWidth = 0f.dp,
        )

        val primitives = ui.endFrame()
        assertEquals(
            1,
            primitives.count { it is UiDrawPrimitive.Quad && (it as UiDrawPrimitive.Quad).color == Color(0f, 0f, 1f, 1f) },
            "emitFillAndBorder's default overlay parameter must follow the calling scope's " +
                "emitsToOverlay instead of hardcoding false -- otherwise every surface's own " +
                "fill/border paints on the base layer regardless of whether it's inside a popup.",
        )
        assertEquals(
            UiDrawPrimitive.Quad(0f, 0f, 10f, 10f, Color(0f, 0f, 1f, 1f)),
            primitives.last(),
            "the overlay-scope fill must sort after the base-layer marker",
        )
    }

    /**
     * Regression coverage for the "dialog wrap-height content clipped/missing" bug: a dialog/
     * popup's overlay content is always painted last, independent of the base layer -- but
     * before this fix, `clip()`'s [UiScope.pushClipInternal] resolved every clip rect against ONE
     * shared stack regardless of [UiScope.emitsToOverlay]. A popup's own composition still runs
     * synchronously nested inside whatever base-layer ancestor's content lambda called it (e.g. a
     * WrapContent card with rounded corners, auto-clipped per Surface.kt), so that ancestor's own
     * (possibly much smaller -- it's sized only around its normal-flow content, which never
     * includes a nested popup's real height) clip rect got baked into the popup's own clip via
     * `current.intersect(rect)`, permanently truncating the popup no matter how correctly its own
     * wrap-height was measured.
     */
    @Test
    fun overlayClipIsIndependentOfAmbientBaseLayerClip() {
        val ui = UiContext()
        ui.beginFrame(200f, 100f, testSnapshot())

        // A small base-layer ancestor clip -- e.g. a WrapContent card only tall enough for its
        // own normal-flow content, textually wrapping a nested popup call.
        val baseColumn = ui.createColumn(x = 0f, y = 0f, width = 100f)
        baseColumn.clip(UiBounds(0f, 0f, 100f, 10f)) {
            // The popup's own overlay content, composed synchronously nested inside the base
            // ancestor's still-active (10px-tall) clip -- but its OWN clip is much taller (its
            // real, correctly-measured wrap height).
            val overlayColumn = ui.createColumn(x = 0f, y = 0f, width = 100f, overlayOnly = true)
            overlayColumn.clip(UiBounds(0f, 0f, 100f, 80f)) {
                overlayColumn.emit(UiDrawPrimitive.Quad(0f, 60f, 10f, 10f, Color(0f, 0f, 1f, 1f)))
            }
        }

        val primitives = ui.endFrame()
        val overlayClipPush = primitives
            .filterIsInstance<UiDrawPrimitive.ClipPush>()
            .last()
        assertEquals(
            80f,
            overlayClipPush.rect.height,
            "a popup/dialog's own clip must resolve against its own (overlay) clip stack, not " +
                "intersect down to whatever ambient clip a synchronously-nesting base-layer " +
                "ancestor happens to have active -- otherwise a WrapContent card only tall enough " +
                "for its own normal-flow content silently truncates every popup composed inside it.",
        )
    }
}
