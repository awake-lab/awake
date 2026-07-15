// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UiDslTest {

    @Test
    fun dslCanComposeInspectorPanelFromPublicFacade() {
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(320f, 240f)

        var panelSlot: UiSlot? = null
        var controlSlot: UiSlot? = null

        ui.ui(x = 20f, y = 20f, width = 200f, font = BitmapFont()) {
            panel(id = "inspector", height = 120f.toDimension()) { slot ->
                panelSlot = slot
                text("Inspector")
                propertyRow(label = "Mode", height = 28f) { propertySlot ->
                    controlSlot = propertySlot
                    dropdown(
                        id = "mode",
                        options = listOf("Mesh", "Light"),
                        selectedIndex = 0,
                        width = propertySlot.width,
                        height = propertySlot.height
                    )
                }
                propertyCheckbox(
                    id = "visible",
                    checked = true,
                    label = "Visible",
                    height = 28f
                )
            }
        }

        val primitives = ui.endFrame()
        val resolvedPanelSlot = assertNotNull(panelSlot)
        val resolvedControlSlot = assertNotNull(controlSlot)
        assertEquals(20f, resolvedPanelSlot.x)
        assertEquals(20f, resolvedPanelSlot.y)
        assertEquals(100f, resolvedControlSlot.x, "property controls should start after the label column and gap")
        assertEquals(64f + 8f, resolvedControlSlot.x - (resolvedPanelSlot.x + UiSpacing.sm.toPx()))
        assertIs<UiDrawPrimitive.RoundedQuad>(primitives.first(), "panel background should use the themed rounded shape")
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(), "dsl content should render text through the shared widget pipeline")
    }

    @Test
    fun dslRowSpacerPreservesHorizontalLayoutProgression() {
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(240f, 100f)

        var first: UiButtonResult? = null
        var second: UiButtonResult? = null

        ui.ui(x = 10f, y = 20f, width = 220f, font = BitmapFont(), gap = 0f) {
            row(height = 30f, gap = 4f) {
                first = buttonSlot(id = "one", label = "One", width = 60f, height = 30f)
                spacer(width = 12f)
                second = buttonSlot(id = "two", label = "Two", width = 60f, height = 30f)
            }
        }

        val firstSlot = assertNotNull(first).slot
        val secondSlot = assertNotNull(second).slot
        assertEquals(UiSlot(10f, 20f, 60f, 30f), firstSlot)
        assertEquals(UiSlot(90f, 20f, 60f, 30f), secondSlot)
    }

    @Test
    fun dslToggleUsesSharedWidgetBehavior() {
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(180f, 80f)

        var checked = true
        ui.ui(x = 20f, y = 20f, width = 140f, font = BitmapFont()) {
            checked = toggle(id = "grid", checked = checked, height = 32f, label = "GRID")
        }

        val primitives = ui.endFrame()
        assertTrue(primitives.any { it is UiDrawPrimitive.Quad || it is UiDrawPrimitive.RoundedQuad }, "toggle should emit its background shape")
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(), "toggle should render its label through the shared glyph pipeline")
        assertFalse(!checked, "toggle should remain unchanged without interaction")
    }

    @Test
    fun dslSupportsReusableSectionHelpers() {
        Input.setPointer(down = false, x = -100f, y = -100f)
        val ui = UiContext()
        ui.beginFrame(260f, 180f)

        ui.ui(x = 20f, y = 20f, width = 220f, font = BitmapFont()) {
            panel(id = "custom", height = 120f.toDimension()) {
                sectionTitle("Scene")
                propertyRow("Mode", 28f) { slot ->
                    dropdown("scene-mode", listOf("Play", "Pause"), 0, slot.width, slot.height)
                }
                propertyToggle("grid", "Show Grid", checked = true)
            }
        }

        val primitives = ui.endFrame()
        assertTrue(primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(), "custom helpers should still render through the public DSL surface")
        assertTrue(primitives.any { it is UiDrawPrimitive.RoundedQuad || it is UiDrawPrimitive.Quad }, "custom helpers should be able to compose built-in panel and control primitives")
    }
}

private fun UiColumnDslScope.sectionTitle(title: String) {
    text(
        title,
        style = Style {
            foreground(DefaultUiTheme.tokens.mutedForeground)
        }
    )
}

private fun UiColumnDslScope.propertyToggle(
    id: String,
    label: String,
    checked: Boolean
): Boolean = propertyCheckbox(id, checked, label, height = 28f)
