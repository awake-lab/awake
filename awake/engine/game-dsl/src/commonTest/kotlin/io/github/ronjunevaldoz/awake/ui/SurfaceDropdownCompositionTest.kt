// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.headless.input.dropdown
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Proves the `surface`/`row`/`dropdown` composition path -- a slot-based property row built
 * from public DSL primitives, not a dedicated widget of its own. */
class SurfaceDropdownCompositionTest {

    @Test
    fun dslCanComposeInspectorPanelFromPublicFacade() {
        val ui = UiContext()
        ui.beginFrame(320f, 240f, testSnapshot())

        var panelSlot: UiBounds? = null
        var controlSlot: UiBounds? = null

        ui.pushFont(BitmapFont())
        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(200f.dp).height(220f.dp)) {
            surface(id = "inspector", modifier = Modifier.height(120f.toDimension())) { slot ->
                panelSlot = slot
                text("Inspector")
                row( modifier = Modifier.height(28f.dp)) { propertySlot ->
                    controlSlot = propertySlot
                    dropdown(
                        id = "mode",
                        options = listOf("Mesh", "Light"),
                        selectedIndex = 0,
                        modifier = Modifier.width(propertySlot.width.px)
                            .height(propertySlot.height.px)
                    )
                }
            }
        }

        val primitives = ui.endFrame()
        val resolvedPanelSlot = assertNotNull(panelSlot)
        assertNotNull(controlSlot)
        assertEquals(20f, resolvedPanelSlot.x)
        assertEquals(20f, resolvedPanelSlot.y)
        assertTrue(
            primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(),
            "dsl content should render text through the shared widget pipeline"
        )
    }

    @Test
    fun propertyRowSupportsSlotBasedLabels() {
        val ui = UiContext()
        ui.beginFrame(320f, 160f, testSnapshot())

        var controlSlot: UiBounds? = null

        ui.column(modifier = Modifier.offset(20f.dp, 20f.dp).width(220f.dp).height(160f.dp)) {
            surface(id = "slot-panel", modifier = Modifier.height(100f.toDimension())) {
                row( modifier = Modifier.height(28f.dp)) { slot ->
                    controlSlot = slot
                    dropdown(
                        id = "camera-mode",
                        options = listOf("Orbit", "Fly"),
                        selectedIndex = 0,
                        modifier = Modifier.width(slot.width.px).height(slot.height.px)
                    )
                }
            }
        }

        assertNotNull(controlSlot)
        val primitives = ui.endFrame()
        assertTrue(
            primitives.filterIsInstance<UiDrawPrimitive.Glyph>().isNotEmpty(),
            "slot-based property labels should still render through the shared text pipeline"
        )
    }
}
