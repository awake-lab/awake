// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.combobox
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/** Controlled combobox proof while the legacy Core bridge remains on the test classpath. */

class ShadcnSelectGenericTest {

    @Test
    fun genericSelectRoundTripsTypedValueNotIndex() = shadcnTestSession(
        width = 300f,
        height = 200f,
        font = BitmapFont(),
    ) {
        var picked: Int? = null

        fun render(x: Float, y: Float, down: Boolean) = frame(x = x, y = y, down = down) {
            val result = combobox(
                id = "fruit",
                options = listOf("Apple", "Banana", "Cherry"),
                selectedIndex = picked,
                modifier = Modifier.width(200f.dp),
            )
            if (result != null) picked = result
        }

        // Closed trigger: find its bounds to click it open.
        val triggerBounds = assertNotNull(render(-1f, -1f, false).semantics.firstOrNull { it.id == "fruit" }).bounds
        val triggerX = triggerBounds.x + triggerBounds.width / 2f
        val triggerY = triggerBounds.y + triggerBounds.height / 2f

        // Press + release the trigger to open the popup.
        render(triggerX, triggerY, true)
        val opened = render(triggerX, triggerY, false).semantics

        // Popup open: locate the "Cherry" item (index 2) to prove a non-first, non-string-coerced
        // value round-trips correctly.
        val itemBounds = assertNotNull(opened.firstOrNull { it.id == "fruit.option.2" }).bounds
        val itemX = itemBounds.x + itemBounds.width / 2f
        val itemY = itemBounds.y + itemBounds.height / 2f

        render(itemX, itemY, true)
        render(itemX, itemY, false)

        assertEquals(2, picked)
    }
}
