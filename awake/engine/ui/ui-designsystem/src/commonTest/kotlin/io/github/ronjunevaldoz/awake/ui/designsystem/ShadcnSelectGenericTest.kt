// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnSelect
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.offset
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/** Throwaway proof for the generic-`T` `shadcnSelect` overload: [onValueChange] must receive
 * the real typed [Fruit], not a stringified index -- the whole point of the audited gap. */
private enum class Fruit { Apple, Banana, Cherry }

class ShadcnSelectGenericTest {

    @Test
    fun genericSelectRoundTripsTypedValueNotIndex() {
        val ui = UiContext()
        ui.pushFont(BitmapFont())
        ui.pushTheme(ShadcnTheme)
        var picked: Fruit? = null

        fun frame(x: Float, y: Float, down: Boolean) {
            ui.beginFrame(300f, 200f, testSnapshot(x = x, y = y, down = down))
            ui.createAbsolute(x = 20f, y = 20f).shadcnSelect(
                id = "fruit",
                value = picked,
                options = Fruit.entries,
                onValueChange = { picked = it },
                label = { it.name },
                modifier = Modifier.width(200f.dp)
            )
        }

        // Closed trigger: find its bounds to click it open.
        frame(-1f, -1f, false)
        val triggerBounds = assertNotNull(ui.finishFrame().semantics.firstOrNull { it.id == "fruit" }).bounds
        val triggerX = triggerBounds.x + triggerBounds.width / 2f
        val triggerY = triggerBounds.y + triggerBounds.height / 2f

        // Press + release the trigger to open the popup.
        frame(triggerX, triggerY, true)
        ui.endFrame()
        frame(triggerX, triggerY, false)
        val opened = ui.finishFrame().semantics

        // Popup open: locate the "Cherry" item (index 2) to prove a non-first, non-string-coerced
        // value round-trips correctly.
        val itemBounds = assertNotNull(opened.firstOrNull { it.id == "fruit.dropdown.item.2" }).bounds
        val itemX = itemBounds.x + itemBounds.width / 2f
        val itemY = itemBounds.y + itemBounds.height / 2f

        frame(itemX, itemY, true)
        ui.endFrame()
        frame(itemX, itemY, false)
        ui.endFrame()

        assertEquals(Fruit.Cherry, picked)
    }
}
