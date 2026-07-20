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
    "ui-showcase-overview" to 0xdb73fa3ce2c0336euL,
    "ui-showcase-reference" to 0xa3f7df2ee2ab5fc8uL,
    "ui-showcase-theming" to 0xb062605c8a59bd8auL,
    "ui-showcase-fonts" to 0x27e0b63ac2ca638uL,
    "ui-showcase-layout" to 0xb3ccf472ef367febuL,
    "ui-showcase-slot-apis" to 0x5e9bebca181ed7dfuL,
    "ui-showcase-buttons" to 0x7951ef0c4776d215uL,
    "ui-showcase-text-input" to 0xb05ee83c9004f900uL,
    "ui-showcase-popups" to 0x325ecf706695c581uL,
    "ui-showcase-state" to 0x62af572b2808b631uL,
    "ui-showcase-button-matrix" to 0x93a68f5998cc7acbuL,
    "ui-showcase-field-matrix" to 0x1024093ed18c54eauL,
    "ui-showcase-slider-matrix" to 0x45c6a2e040913314uL,
    "ui-showcase-dropdown-open" to 0x9cc95bab02b03960uL,
    "ui-showcase-tooltip-open" to 0x8dba379989660404uL,
    "ui-showcase-alert-dialog" to 0x6c9b815bc67de57auL,
    "ui-showcase-scroll-panel" to 0x589e0dfc44f9479fuL
)
