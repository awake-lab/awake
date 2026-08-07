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
private val expectedShowcaseLayoutSignatures = mapOf(
    "ui-showcase-overview" to 0x8a0f1cda327975f7uL,
    "ui-showcase-theming" to 0x9d0095bbab02a1b6uL,
    "ui-showcase-typography" to 0x5cdcf234712bdbf4uL,
    "ui-showcase-buttons" to 0x44538bdc29273616uL,
    "ui-showcase-avatar" to 0x8ea30ca38683afffuL,
    "ui-showcase-breadcrumb" to 0xf1a3411b971e00c1uL,
    "ui-showcase-card" to 0xfba54eb1186fca2cuL,
    "ui-showcase-sidebar" to 0x8610343505e6874duL,
    "ui-showcase-selection" to 0x9f4979b7bcf04bd7uL,
    "ui-showcase-range-slider" to 0x8a0f1cda327975f7uL,
    "ui-showcase-tabs" to 0xbba0ff689c10fa07uL,
    "ui-showcase-select" to 0x58c470e45506c2bfuL,
    "ui-showcase-kbd-separator" to 0x5e2ff8a0320ed449uL,
    "ui-showcase-feedback" to 0x37aeb8fecfaf71cauL,
    "ui-showcase-alert" to 0x78debd105ca87c64uL,
    "ui-showcase-text-input" to 0x8d5be4261c8c64euL,
    "ui-showcase-popups" to 0x8ef2ec7585c20826uL,
    "ui-showcase-state" to 0x8a0f1cda327975f7uL,
    "ui-showcase-button-matrix" to 0x66fe64b834938154uL,
    "ui-showcase-field-matrix" to 0x409492ffe72ab4euL,
    "ui-showcase-slider-matrix" to 0x5a90a7119bc91e3fuL,
    "ui-showcase-dropdown-open" to 0xa0033bfcf4279010uL,
    "ui-showcase-popover-open" to 0x65ca2799d16bc711uL,
    "ui-showcase-tooltip-open" to 0x8540c472d501ae7uL,
    "ui-showcase-alert-dialog" to 0xa895395688df6fbuL,
    "ui-showcase-scroll-panel" to 0x8aebd745f72c6a74uL,
    "ui-showcase-shimmer" to 0x8a0f1cda327975f7uL,
    "ui-showcase-collapsible" to 0x6f09095a91d2fdcauL,
    "ui-showcase-collapsible-open" to 0xff92a08ad917785euL,
    "ui-showcase-field-demo" to 0x8a0f1cda327975f7uL,
)
