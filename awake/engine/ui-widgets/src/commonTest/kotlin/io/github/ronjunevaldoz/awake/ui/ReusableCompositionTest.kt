// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ReusableCompositionTest {

    @Test
    fun customWidgetCanResolveStyleFromPublicApi() {
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(200f, 80f)

        ui.absolute(20f, 20f, font = BitmapFont()).badge("status", "READY", emphasized = true)

        val primitives = ui.endFrame()
        assertIs<UiDrawPrimitive.RoundedQuad>(primitives.first(), "custom widget should be able to emit a styled rounded border")
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(), "custom widget should be able to reuse text() for its label")
    }

    @Test
    fun customLayoutCanDriveBuiltInWidgets() {
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(240f, 120f)

        val scope = DiagonalScope(ui, BitmapFont(), CoreUiTheme, startX = 10f, startY = 20f, stepX = 15f, stepY = 10f)
        val first = scope.buttonSlot("one", 100f, 30f, label = "ONE")
        val second = scope.buttonSlot("two", 100f, 30f, label = "TWO")

        assertEquals(UiSlot(10f, 20f, 100f, 30f), first.slot)
        assertEquals(UiSlot(25f, 30f, 100f, 30f), second.slot)
    }
}

private val BadgeToneKey = StyleStateKey(false)

private fun UiScope.badge(
    id: String,
    label: String,
    emphasized: Boolean,
    width: Float = 100f,
    height: Float = 28f,
    style: Style = Style.Empty
) {
    val slot = claimSlot(width.toDimension(), height.toDimension())
    val hovered = hitTest(slot)
    tryClaimActive(id, hovered)
    releaseActiveIfMatches(id)
    val active = isActive(id)
    val resolved = resolveStyle(
        style = style,
        defaults = Style {
            background(theme.tokens.background)
            foreground(theme.tokens.foreground)
            shape(UiShape.sm)
            borderWidth(1f.dp)
            borderColor(theme.tokens.border)
            hovered { background(theme.tokens.muted) }
            state(BadgeToneKey, true) { borderWidth(2f.dp) }
        },
        state = MutableStyleState(hovered = hovered, active = active).set(BadgeToneKey, emphasized)
    )
    emitFillAndBorder(
        slot = slot,
        fillColor = resolved.background ?: TransparentColor,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: theme.tokens.border
    )
    if (font != null) {
        text(label, slot, font = font, color = resolved.foreground ?: theme.tokens.foreground, centered = true)
    }
}

private class DiagonalScope(
    context: UiContext,
    font: BitmapFont?,
    theme: UiTheme,
    private val startX: Float,
    private val startY: Float,
    private val stepX: Float,
    private val stepY: Float
) : AbstractUiScope(context, font, theme) {
    private var index = 0

    override fun claimSlot(width: Dimension, height: Dimension): UiSlot {
        val slot = UiSlot(
            x = startX + stepX * index,
            y = startY + stepY * index,
            width = resolve(width, fallback = 100f),
            height = resolve(height, fallback = 30f)
        )
        index++
        return slot
    }

    private fun resolve(dimension: Dimension, fallback: Float): Float = when (dimension) {
        is Dimension.Fixed -> dimension.dp.toPx()
        Dimension.FillMax -> fallback
        Dimension.WrapContent -> fallback
    }
}
