// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.heightIn
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.modifier.widthIn
import io.github.ronjunevaldoz.awake.ui.scope.claimModifiedSlot
import kotlin.test.Test
import kotlin.test.assertEquals
import io.github.ronjunevaldoz.awake.ui.context.UiFrameInput

/**
 * `widthIn`/`heightIn` bound a resolved size, mirroring Compose. Without them the only sizing
 * choices were an exact size or fill, so "as wide as the content needs, but never below X" --
 * the shape shadcn's controls actually have -- could only be faked by pinning a fixed width at
 * every call site.
 */
class SizeConstraintModifierTest {

    private fun claim(modifier: io.github.ronjunevaldoz.awake.ui.modifier.UiModifier): UiBounds {
        val ui = UiContext()
        ui.beginFrame(UiFrameInput(viewportWidth = 500f, viewportHeight = 300f, input = testSnapshot()))
        return ui.createAbsolute(x = 0f, y = 0f).claimModifiedSlot(modifier)
    }

    @Test
    fun minimumRaisesASmallerResolvedSize() {
        val bounds = claim(Modifier.width(20f.dp).height(10f.dp).widthIn(min = 80f.dp))
        assertEquals(80f, bounds.width)
    }

    @Test
    fun maximumLowersALargerResolvedSize() {
        val bounds = claim(Modifier.width(400f.dp).height(10f.dp).widthIn(max = 120f.dp))
        assertEquals(120f, bounds.width)
    }

    @Test
    fun aSizeAlreadyInRangeIsUntouched() {
        val bounds = claim(Modifier.width(100f.dp).height(10f.dp).widthIn(min = 40f.dp, max = 200f.dp))
        assertEquals(100f, bounds.width)
    }

    @Test
    fun minimumWinsWhenTheRangeIsInverted() {
        // Compose resolves a contradictory range in favour of the minimum; matching that
        // avoids a widget silently collapsing when two callers each supply one end.
        val bounds = claim(Modifier.width(10f.dp).height(10f.dp).widthIn(min = 90f.dp, max = 50f.dp))
        assertEquals(90f, bounds.width)
    }

    @Test
    fun heightConstraintsApplyOnTheOtherAxis() {
        val bounds = claim(Modifier.width(10f.dp).height(4f.dp).heightIn(min = 36f.dp))
        assertEquals(36f, bounds.height)
    }
}
