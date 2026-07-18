// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreview
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewEntry
import io.github.ronjunevaldoz.awake.testing.ui.describeLayout
import io.github.ronjunevaldoz.awake.testing.ui.layoutSignature
import kotlin.test.Test
import kotlin.test.assertEquals

/** Sample-level golden-layout verification: each full showcase page preview's semantic-node
 * layout (widget roles, ids, and bounds) is fingerprinted and compared against a recorded
 * signature. Unlike a pixel screenshot, a pure style/color change (recoloring a theme) leaves
 * these signatures untouched; a real layout regression (a widget moving, resizing, or vanishing)
 * changes them -- catching drift at the whole-page level without screenshot flakiness. */
class UiShowcaseLayoutSignatureTest {

    @Test
    fun showcasePageLayoutsRemainStableAcrossTargets() {
        val actual = UiShowcasePreviewEntries.associate { entry ->
            val metadata = previewMetadataFor(entry)
            val frame = entry.render(metadata)
            metadata.id to layoutSignature(frame.semantics)
        }

        val mismatches = StringBuilder()
        assertEquals(expectedShowcaseLayoutSignatures.size, actual.size, "Preview page count changed. Refresh the expected matrix.")
        expectedShowcaseLayoutSignatures.forEach { (id, expectedSignature) ->
            val entry = requireNotNull(UiShowcasePreviewEntries.find { previewMetadataFor(it).id == id }) { "Missing preview $id" }
            val frame = entry.render(previewMetadataFor(entry))
            val actualSignature = actual.getValue(id)
            if (actualSignature != expectedSignature) {
                mismatches.append("$id actual=0x${actualSignature.toString(16)}\n${describeLayout(frame.semantics)}\n\n")
            }
        }
        assertEquals("", mismatches.toString(), "Layout drift detected")
    }
}

private val expectedShowcaseLayoutSignatures = mapOf(
    "ui-showcase-overview" to 0x44612df645c45491uL,
    "ui-showcase-reference" to 0x8aa119eaada2bffduL,
    "ui-showcase-theming" to 0x4bc26926abb4f5fduL,
    "ui-showcase-fonts" to 0xea0360db99b53b45uL,
    "ui-showcase-layout" to 0xb8537cfb6ec2c94cuL,
    "ui-showcase-slot-apis" to 0xee60c0ef52b671c4uL,
    "ui-showcase-buttons" to 0xe322cd27ce79b44euL,
    "ui-showcase-text-input" to 0x9d1859d8f90fc313uL,
    "ui-showcase-popups" to 0xc2124a527e286c9auL,
    "ui-showcase-state" to 0xc8da84b5eb581396uL,
    "ui-showcase-button-matrix" to 0x492648f5fe43ca6auL,
    "ui-showcase-field-matrix" to 0xc5551c72f7f6cc7buL,
    "ui-showcase-slider-matrix" to 0x6a7662e603131b54uL,
    "ui-showcase-dropdown-open" to 0x427858ad97504290uL,
    "ui-showcase-tooltip-open" to 0xdde4bee2cc9f4b86uL,
    "ui-showcase-alert-dialog" to 0x4aa15bc9c4dd0e88uL,
    "ui-showcase-scroll-panel" to 0xc898729cfcc6f93buL
)
