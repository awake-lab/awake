// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.inspectSemanticNodes
import io.github.ronjunevaldoz.awake.testing.ui.requireSemanticNode
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.progress
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * [progress] eases its fill toward [value] via `animateFloat` rather than snapping there --
 * per docs/reference/ui-validation.md's animated-component rule, this samples rest/in-flight/
 * settled frames instead of asserting only the final state.
 */
class ProgressTest {

    private fun UiContext.progressFillWidth(): Float {
        // The track paints as a plain Quad (+ separate border edges); progress() hardcodes
        // Pill for the fill regardless of theme, so it's the only RoundedQuad this emits, and
        // only once its animated fraction has moved off zero.
        return endFrame().filterIsInstance<UiDrawPrimitive.RoundedQuad>().firstOrNull()?.w ?: 0f
    }

    @Test
    fun fillWidensAcrossFramesUntilItSettlesAtTarget() {
        val ui = UiContext()
        val modifier = Modifier.width(200f.px).height(8f.px)

        // Rest: the very first frame, one animation step in from `initial = 0f`.
        ui.beginFrame(220f, 40f, testSnapshot(), deltaSeconds = 1f / 60f)
        ui.createAbsolute(x = 10f, y = 10f).progress("p", value = 1f, modifier = modifier)
        val restWidth = ui.progressFillWidth()

        // In-flight: a few more frames in, clearly past rest but not yet converged.
        repeat(3) {
            ui.beginFrame(220f, 40f, testSnapshot(), deltaSeconds = 1f / 60f)
            ui.createAbsolute(x = 10f, y = 10f).progress("p", value = 1f, modifier = modifier)
        }
        val inFlightWidth = ui.progressFillWidth()

        // Settled: enough elapsed time for the spring to snap exactly onto its target.
        repeat(30) {
            ui.beginFrame(220f, 40f, testSnapshot(), deltaSeconds = 1f / 20f)
            ui.createAbsolute(x = 10f, y = 10f).progress("p", value = 1f, modifier = modifier)
        }
        val settledWidth = ui.progressFillWidth()

        assertTrue(
            restWidth < inFlightWidth,
            "fill should widen between rest and in-flight: rest=$restWidth inFlight=$inFlightWidth",
        )
        assertTrue(
            inFlightWidth < settledWidth,
            "fill should keep widening between in-flight and settled: inFlight=$inFlightWidth settled=$settledWidth",
        )
        assertEquals(
            200f,
            settledWidth,
            absoluteTolerance = 1f,
            message = "settled fill should have converged on the full track width",
        )
    }

    @Test
    fun recordsProgressSemanticRoleAndPercentLabel() {
        val ui = UiContext()
        ui.beginFrame(220f, 40f, testSnapshot())
        ui.createAbsolute(x = 10f, y = 10f)
            .progress("p", value = 0.5f, modifier = Modifier.width(200f.px).height(8f.px))

        val semantics = ui.semanticNodes()
        inspectSemanticNodes(semantics).requireClean()
        val node = requireSemanticNode(semantics, id = "p", role = UiSemanticRole.Progress)
        assertEquals("50%", node.label)
    }
}
