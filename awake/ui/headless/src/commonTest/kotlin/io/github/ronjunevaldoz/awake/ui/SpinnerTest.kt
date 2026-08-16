// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.UiComponentFrame
import io.github.ronjunevaldoz.awake.testing.ui.inspectSemanticNodes
import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.testing.ui.requireSemanticNode
import io.github.ronjunevaldoz.awake.testing.ui.uiTestSession
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.spinner
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertNotEquals

/**
 * [spinner] orbits its dots continuously off accumulated `frameDeltaSeconds()` -- a single
 * rest-frame render can't prove it actually animates, so per docs/reference/ui-validation.md's
 * animated-component rule this samples several frames and confirms the dots keep moving.
 */
class SpinnerTest {

    private fun UiComponentFrame.firstDotCenter(): Pair<Float, Float> {
        val dot = primitives.filterIsInstance<UiDrawPrimitive.RoundedQuad>().first()
        return (dot.x + dot.w / 2f) to (dot.y + dot.h / 2f)
    }

    @Test
    fun dotsOrbitAcrossSampledFrames() {
        val modifier = Modifier.width(24f.px).height(24f.px)

        val (rest, inFlight, later) = uiTestSession(width = 60f, height = 60f) {
            val rest = frame(deltaSeconds = 0.1f) {
                primitive.context.createAbsolute(x = 10f, y = 10f).spinner("s", modifier = modifier)
            }.firstDotCenter()
            val inFlight = frame(deltaSeconds = 0.1f) {
                primitive.context.createAbsolute(x = 10f, y = 10f).spinner("s", modifier = modifier)
            }.firstDotCenter()
            val later = frame(deltaSeconds = 0.1f) {
                primitive.context.createAbsolute(x = 10f, y = 10f).spinner("s", modifier = modifier)
            }.firstDotCenter()
            Triple(rest, inFlight, later)
        }

        assertNotEquals(
            rest,
            inFlight,
            "spinner dot should have moved between the rest and next sampled frame",
        )
        assertNotEquals(inFlight, later, "spinner dot should keep moving on a third sampled frame")
        assertNotEquals(rest, later, "spinner dot should not already be back at its rest position")
    }

    @Test
    fun recordsSpinnerSemanticRole() {
        val frame = renderUiComponent(width = 60f, height = 60f) {
            primitive.context.createAbsolute(x = 10f, y = 10f)
                .spinner("s", modifier = Modifier.width(24f.px).height(24f.px))
        }

        val semantics = frame.semantics
        inspectSemanticNodes(semantics).requireClean()
        requireSemanticNode(semantics, id = "s", role = UiSemanticRole.Spinner)
    }
}
