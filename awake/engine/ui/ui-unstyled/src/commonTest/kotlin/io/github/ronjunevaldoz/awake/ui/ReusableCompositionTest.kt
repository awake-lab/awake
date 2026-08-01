// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.unstyled.buttonSlot
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

class ReusableCompositionTest {

    @Test
    fun customWidgetCanResolveStyleFromPublicApi() {
        val ui = UiContext()
        ui.beginFrame(200f, 80f, testSnapshot())

        ui.createAbsolute(modifier = Modifier.offset(20f.dp, 20f.dp), font = BitmapFont()).badge("status", "READY", emphasized = true)

        val primitives = ui.endFrame()
        assertIs<UiDrawPrimitive.RoundedQuad>(primitives.first(), "custom widget should be able to emit a styled rounded border")
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(), "custom widget should be able to reuse text() for its label")
    }

    @Test
    fun customLayoutCanDriveBuiltInWidgets() {
        val ui = UiContext()
        ui.beginFrame(240f, 120f, testSnapshot())
        ui.pushFont(BitmapFont())
        ui.pushTheme(CoreUiTheme)

        val scope = DiagonalScope(ui, startX = 10f, startY = 20f, stepX = 15f, stepY = 10f)
        val first = scope.buttonSlot("one", label = "ONE", modifier = Modifier.width(100f.px).height(30f.px))
        val second = scope.buttonSlot("two", label = "TWO", modifier = Modifier.width(100f.px).height(30f.px))

        assertEquals(UiBounds(10f, 20f, 100f, 30f), first.slot)
        assertEquals(UiBounds(25f, 30f, 100f, 30f), second.slot)
    }

    @Test
    fun buttonSlotCanHostCustomComposedContent() {
        val ui = UiContext()
        ui.beginFrame(260f, 120f, testSnapshot())

        val result = ui.createAbsolute(modifier = Modifier.offset(20f.dp, 20f.dp), font = BitmapFont()).buttonSlot(
            id = "launch",
            modifier = Modifier.width(180f.px).height(40f.px),
            style = Style {
                contentPadding(start = 12f.dp, top = 0f.dp, end = 12f.dp, bottom = 0f.dp)
            }
        ) { slot ->
            text(
                label = ">",
                slot = UiBounds(slot.x, slot.y, 12f, slot.height),
                font = font,
                centered = false,
                verticallyCentered = true
            )
            text(
                label = "Launch",
                slot = UiBounds(slot.x + 18f, slot.y, 72f, slot.height),
                font = font,
                centered = false,
                verticallyCentered = true
            )
        }

        val glyphs = ui.endFrame().filterIsInstance<UiDrawPrimitive.Glyph>()
        assertEquals(UiBounds(20f, 20f, 180f, 40f), result.slot)
        assertTrue(glyphs.size >= 7, "custom button slot content should be able to emit multiple glyph runs")
        assertTrue(glyphs.first().x >= 32f, "slot content should render inside the padded content region, not against the outer button edge")
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
        fillColor = resolved.background ?: Color.Transparent,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: theme.tokens.border
    )
    if (font != null) {
        text(label, slot.toBounds(), font = font, color = resolved.foreground ?: theme.tokens.foreground, centered = true)
    }
}

private class DiagonalScope(
    context: UiContext,
    private val startX: Float,
    private val startY: Float,
    private val stepX: Float,
    private val stepY: Float
) : io.github.ronjunevaldoz.awake.ui.layouts.AbstractUiScope(context) {
    private var index = 0

    override fun claimSlot(width: Dimension, height: Dimension, weight: LayoutWeight?): UiSlot {
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
