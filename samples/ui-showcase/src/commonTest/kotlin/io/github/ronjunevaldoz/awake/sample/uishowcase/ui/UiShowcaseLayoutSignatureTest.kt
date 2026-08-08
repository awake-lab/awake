// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

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
        assertEquals(
            expectedShowcaseLayoutSignatures.size,
            actual.size,
            "Preview page count changed. Refresh the expected matrix:\n${actual.toExpectedSignatureMatrix()}",
        )
        expectedShowcaseLayoutSignatures.forEach { (id, expectedSignature) ->
            val entry = requireNotNull(UiShowcasePreviewEntries.find { previewMetadataFor(it).id == id }) { "Missing preview $id" }
            val frame = entry.render(previewMetadataFor(entry))
            val actualSignature = actual.getValue(id)
            if (actualSignature != expectedSignature) {
                mismatches.append("$id actual=0x${actualSignature.toString(16)}\n${describeLayout(frame.semantics)}\n\n")
            }
        }
        assertEquals("", mismatches.toString(), "Layout drift detected. New matrix:\n${actual.toExpectedSignatureMatrix()}")
    }
}

private fun Map<String, ULong>.toExpectedSignatureMatrix(): String =
    entries.joinToString(separator = "\n") { (id, signature) ->
        "\"$id\" to 0x${signature.toString(16)}uL,"
    }

// 2026-08-08: re-recorded -- new semantic roles (Separator/Avatar/Progress/Toast) now
// record nodes, switch claims measured label width, and the ac03b490/c9d00df7 text/accordion
// changes were never re-recorded.
// 2026-08-08: re-recorded after the shadcn token/radius value pass -- card/dialog padding
// 16->24dp (p-6) reflows content on every page that uses a card.
private val expectedShowcaseLayoutSignatures = mapOf(
    "ui-showcase-overview" to 0x48959cc0d646071buL,
    "ui-showcase-theming" to 0x6fd0ff48e7aa593buL,
    "ui-showcase-typography" to 0xbed85cf2b89cc002uL,
    "ui-showcase-buttons" to 0xb09bd382c4ab7914uL,
    "ui-showcase-avatar" to 0x68be83971ffb960buL,
    "ui-showcase-breadcrumb" to 0x8f415d5e95f3d40buL,
    "ui-showcase-card" to 0x44412e9576a3adf5uL,
    "ui-showcase-sidebar" to 0xa6673c4992dee1b6uL,
    "ui-showcase-selection" to 0x42e646752e460291uL,
    "ui-showcase-range-slider" to 0x48959cc0d646071buL,
    "ui-showcase-tabs" to 0x9aafffcd5ac7c36buL,
    "ui-showcase-select" to 0xf41aa54b82b62e64uL,
    "ui-showcase-kbd-separator" to 0x5935d0c071402b43uL,
    "ui-showcase-feedback" to 0xc61533236f6cfe6cuL,
    "ui-showcase-alert" to 0x9d2a32e5a61c9824uL,
    "ui-showcase-text-input" to 0x808e46001fed95fuL,
    "ui-showcase-popups" to 0xc5afcd1b3ad293aduL,
    "ui-showcase-state" to 0x48959cc0d646071buL,
    "ui-showcase-button-matrix" to 0x33bcf45359ffb8b1uL,
    "ui-showcase-field-matrix" to 0xbb2cb0ed75dd31a8uL,
    "ui-showcase-slider-matrix" to 0xdfd8dc68c10169cduL,
    "ui-showcase-dropdown-open" to 0x58c2237ef4cfa8c2uL,
    "ui-showcase-popover-open" to 0x27fa66f9f5bcffc9uL,
    "ui-showcase-tooltip-open" to 0xc30ac612b2d358cduL,
    "ui-showcase-alert-dialog" to 0x9de38dec4f0c59dcuL,
    "ui-showcase-scroll-panel" to 0xa0ba937d3653d5f7uL,
    "ui-showcase-shimmer" to 0x48959cc0d646071buL,
    "ui-showcase-collapsible" to 0x1fcc163406b5701auL,
    "ui-showcase-collapsible-open" to 0x98d3cb96b46e9e39uL,
    "ui-showcase-field-demo" to 0x48959cc0d646071buL,
)
