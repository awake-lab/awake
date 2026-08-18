// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.inspectSemanticContentFit
import io.github.ronjunevaldoz.awake.testing.ui.inspectSemanticNodes
import io.github.ronjunevaldoz.awake.testing.ui.inspectTextTruncation
import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.testing.ui.requireSemanticNode
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.button
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.button
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.text
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement as HeadlessArrangement
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier as HeadlessModifier
import io.github.ronjunevaldoz.awake.ui.headless.column as headlessColumn
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxSize as headlessFillMaxSize
import io.github.ronjunevaldoz.awake.ui.modifier.width as headlessWidth
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UiSemanticWidgetsTest {

    @Test
    fun buttonEmitsSemanticButtonAndLabelNodes() {
        val font = UiFonts.default()
        val frame = renderUiComponent(width = 240f, height = 96f, font = font) {
            primitive.context.createAbsolute(x = 20f, y = 20f).button(
                id = "primary-action",
                modifier = Modifier.width(180f.px).height(44f.px),
                label = "Awake Button",
            )
        }

        val semantics = frame.semantics

        inspectSemanticNodes(semantics).requireClean()
        inspectSemanticContentFit(semantics, tolerancePx = 1f).requireClean()
        val buttonNode =
            requireSemanticNode(semantics, id = "primary-action", role = UiSemanticRole.Button)
        val labelNode =
            requireSemanticNode(semantics, id = "primary-action.label", role = UiSemanticRole.Text)

        assertEquals("Awake Button", buttonNode.label)
        assertEquals("Awake Button", labelNode.label)
        assertTrue(labelNode.contentBounds != null)
    }

    /** [io.github.ronjunevaldoz.awake.ui.headless.button]'s label overload is now sugar over the
     * slot API (`button(id) { text(label) }`) -- proves the intrinsic-width-from-label sizing it
     * relied on before that refactor survived: a longer label must still measure wider, not
     * collapse to a shared FillMax/WrapContent default now that nothing sets an explicit width. */
    @Test
    fun labelButtonHugsIntrinsicWidthWithoutExplicitWidth() {
        val font = UiFonts.default()
        val frame = renderUiComponent(width = 400f, height = 120f, font = font) {
            headlessColumn(modifier = HeadlessModifier.headlessFillMaxSize(), verticalArrangement = HeadlessArrangement.spacedBy(8f.dp)) {
                button(id = "short", label = "Go")
                button(id = "long", label = "A Much Longer Button Label")
            }
        }

        val semantics = frame.semantics
        inspectSemanticNodes(semantics).requireClean()

        val shortNode = requireSemanticNode(semantics, id = "short", role = UiSemanticRole.Button)
        val longNode = requireSemanticNode(semantics, id = "long", role = UiSemanticRole.Button)

        assertTrue(
            longNode.bounds.width > shortNode.bounds.width,
            "expected the longer label to measure wider (short=${shortNode.bounds.width}px, long=${longNode.bounds.width}px) -- " +
                "intrinsic width must survive routing the label overload through the slot API",
        )
        assertTrue(
            shortNode.bounds.width < 400f,
            "short-label button hugged nothing and instead claimed the full 400px frame width " +
                "(${shortNode.bounds.width}px) -- FillMax leaked through instead of the intrinsic label width",
        )
    }

    /** A button's label can still be repositioned via [centered] -- proves the slot-API-based
     * label overload didn't lose per-call control over where the label draws inside the button. */
    @Test
    fun labelButtonRespectsCenteredForRepositioning() {
        val font = UiFonts.default()
        val frame = renderUiComponent(width = 300f, height = 120f, font = font) {
            headlessColumn(modifier = HeadlessModifier.headlessFillMaxSize(), verticalArrangement = HeadlessArrangement.spacedBy(8f.dp)) {
                button(id = "centered-btn", label = "Label", modifier = HeadlessModifier.headlessWidth(200f.dp))
                button(id = "start-btn", label = "Label", modifier = HeadlessModifier.headlessWidth(200f.dp), centered = false)
            }
        }

        val semantics = frame.semantics
        inspectSemanticNodes(semantics).requireClean()

        val centeredLabel = requireSemanticNode(semantics, id = "centered-btn.label", role = UiSemanticRole.Text)
        val startLabel = requireSemanticNode(semantics, id = "start-btn.label", role = UiSemanticRole.Text)
        // .bounds is the full claimed slot (always the button's own content rect, x=0 either
        // way) -- the actual glyph position centered/left-alignment moves is .contentBounds.
        val centeredX = requireNotNull(centeredLabel.contentBounds) { "centered label has no contentBounds" }.x
        val startX = requireNotNull(startLabel.contentBounds) { "start-aligned label has no contentBounds" }.x

        assertTrue(
            startX < centeredX,
            "centered=false must draw the label closer to the button's left edge than centered=true " +
                "(centered.x=$centeredX, start.x=$startX) -- label position must stay caller-controllable",
        )
    }

    @Test
    fun ellipsizedTextIsMarkedAsTruncated() {
        val font = UiFonts.default()
        val frame = renderUiComponent(width = 180f, height = 64f, font = font) {
            primitive.context.createAbsolute(x = 12f, y = 12f).text(
                label = "This label is intentionally too wide for the slot",
                slot = UiBounds(12f, 12f, 80f, 16f),
                overflow = UiTextOverflow.Ellipsis,
                semanticId = "truncated.copy",
            )
        }

        val semantics = frame.semantics

        inspectSemanticNodes(semantics).requireClean()
        assertTrue(inspectTextTruncation(semantics).issues.any { it.nodeId == "truncated.copy" })
    }
}
