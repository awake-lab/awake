// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.inspectSemanticNodes
import io.github.ronjunevaldoz.awake.testing.ui.inspectTextTruncation
import io.github.ronjunevaldoz.awake.testing.ui.requireSemanticNode
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.headless.input.selection.switch
import io.github.ronjunevaldoz.awake.ui.style.Style
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Locks the label-measurement fix in
 * [io.github.ronjunevaldoz.awake.ui.headless.input.selection.switch]: a bare switch (no
 * explicit `.width()`) must claim enough width for track+gap+label that the label's own
 * semantic content is never truncated, for both a short and a long label.
 *
 * [shortLabelAtRegressionTextSizeIsNotTruncated] pins the exact conditions (label "On" at a
 * 13sp text size) of a real production failure: the claimed width round-trips through Dp and
 * back (see `Switch.kt`'s `fallbackWidthPx` comment), which landed a couple ULP short of the
 * width `layoutBitmapText` needed with zero margin to spare.
 */
class SwitchLabelWidthTest {

    @Test
    fun shortLabelAtRegressionTextSizeIsNotTruncated() {
        val ui = UiContext()
        ui.beginFrame(240f, 60f, testSnapshot())
        ui.createAbsolute(x = 0f, y = 0f).switch(
            "switch-on",
            checked = true,
            label = "On",
            style = Style { textSize(13f.sp) },
        )

        val semantics = ui.semanticNodes()
        inspectSemanticNodes(semantics).requireClean()
        inspectTextTruncation(semantics).requireClean()

        val label = requireSemanticNode(semantics, id = "switch-on.label", role = UiSemanticRole.Text)
        assertTrue((label.contentBounds?.width ?: 0f) > 0f, "label must have measured, non-empty content bounds")
    }

    @Test
    fun longLabelIsNotTruncated() {
        val ui = UiContext()
        ui.beginFrame(400f, 60f, testSnapshot())
        ui.createAbsolute(x = 0f, y = 0f).switch(
            "switch-long",
            checked = false,
            label = "A genuinely long switch label for width testing",
        )

        val semantics = ui.semanticNodes()
        inspectSemanticNodes(semantics).requireClean()
        inspectTextTruncation(semantics).requireClean()

        val label = requireSemanticNode(semantics, id = "switch-long.label", role = UiSemanticRole.Text)
        assertTrue((label.contentBounds?.width ?: 0f) > 0f, "label must have measured, non-empty content bounds")
    }
}
