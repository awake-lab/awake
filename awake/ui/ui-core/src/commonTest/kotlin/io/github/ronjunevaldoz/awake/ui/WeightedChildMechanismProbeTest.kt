// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.LayoutWeight
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement

import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Same parent, two ways of asking for weight -- which one is broken.
 *
 * `LayoutTest.columnWeightSplitsRemainingSpaceEvenlyForEqualWeights` passes calling `claimSlot`
 * directly; `LayoutSizingMatrixTest` fails going through `column(modifier = weight(1f))`, which
 * reaches `claimSlot` via `claimModifiedSlot`. Holding the parent identical isolates the child
 * mechanism as the only variable.
 */
class WeightedChildMechanismProbeTest {

    private fun parent(body: io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope.() -> Unit) {
        val ui = UiContext()
        ui.beginFrame(FRAME, FRAME, testSnapshot())
        ui.createBox(x = 0f, y = 0f, width = FRAME, height = FRAME).column(
            id = "parent",
            verticalArrangement = Arrangement.spacedBy(0f.px),
            modifier = Modifier.width(Dimension.FillMax).height(Dimension.Fixed(PARENT.px)),
            content = { body() },
        )
        ui.endFrame()
    }

    @Test
    fun aDirectClaimSlotWeightGetsItsShare() {
        var middle: UiBounds? = null
        parent {
            claimSlot(Dimension.FillMax, Dimension.Fixed(FIXED.px), null)
            middle = claimSlot(Dimension.FillMax, Dimension.FillMax, LayoutWeight(1f))
            claimSlot(Dimension.FillMax, Dimension.Fixed(FIXED.px), null)
        }
        assertEquals(PARENT - FIXED * 2f, middle?.height, "direct claimSlot weight")
    }

    @Test
    fun aModifierWeightOnAColumnChildGetsTheSameShare() {
        var middle: UiBounds? = null
        parent {
            column(id = "a", modifier = Modifier.width(Dimension.FillMax).height(FIXED.px)) { }
            middle = column(id = "b", modifier = Modifier.width(Dimension.FillMax).weight(1f)) { }
            column(id = "c", modifier = Modifier.width(Dimension.FillMax).height(FIXED.px)) { }
        }
        assertEquals(PARENT - FIXED * 2f, middle?.height, "Modifier.weight on a column child")
    }

    private companion object {
        const val FRAME = 400f
        const val PARENT = 400f
        const val FIXED = 48f
    }
}
