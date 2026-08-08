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
// 2026-08-08: re-recorded after the glyph-advance fix -- advances were inflated by the atlas
// cell padding, so every text run's measured width changed and reflowed the layouts.
private val expectedShowcaseLayoutSignatures = mapOf(
    "ui-showcase-overview" to 0x0bfa8972284f0d1buL,
    "ui-showcase-theming" to 0xab19d6e342a0b2a1uL,
    "ui-showcase-typography" to 0xaca35defa185fbcduL,
    "ui-showcase-buttons" to 0x482ca48984a52104uL,
    "ui-showcase-avatar" to 0x7b9e1fd65870a432uL,
    "ui-showcase-breadcrumb" to 0x9edc7ed9c1af05d8uL,
    "ui-showcase-card" to 0xaa2e8ba9fa64c7abuL,
    "ui-showcase-sidebar" to 0x6622f60edce13e0euL,
    "ui-showcase-selection" to 0xe2c6c214804620cbuL,
    "ui-showcase-range-slider" to 0x0bfa8972284f0d1buL,
    "ui-showcase-tabs" to 0xff80337374b556d8uL,
    "ui-showcase-select" to 0xabb50caaad3dd03euL,
    "ui-showcase-kbd-separator" to 0x9ecc92ed768d9e2cuL,
    "ui-showcase-feedback" to 0x24b30517abbec4dauL,
    "ui-showcase-alert" to 0xbccba7437ff2ce9auL,
    "ui-showcase-text-input" to 0x8be6c69a7c85b76auL,
    "ui-showcase-popups" to 0xfc12f2555f512db7uL,
    "ui-showcase-state" to 0x0bfa8972284f0d1buL,
    "ui-showcase-button-matrix" to 0xbd8c46b6479e6010uL,
    "ui-showcase-field-matrix" to 0xfe14d88511ba95a7uL,
    "ui-showcase-slider-matrix" to 0x78304b7953e7509duL,
    "ui-showcase-dropdown-open" to 0x32e89370a7819ad3uL,
    "ui-showcase-popover-open" to 0x07097eea2b3d88e4uL,
    "ui-showcase-tooltip-open" to 0xb88a3ff4ace7a8bauL,
    "ui-showcase-alert-dialog" to 0x6b4186684fcab7e7uL,
    "ui-showcase-scroll-panel" to 0x6cba630a8f76f2a2uL,
    "ui-showcase-shimmer" to 0x0bfa8972284f0d1buL,
    "ui-showcase-collapsible" to 0x3e3c789930c7fe20uL,
    "ui-showcase-collapsible-open" to 0xb80091f3a2e9c04euL,
    "ui-showcase-field-demo" to 0x0bfa8972284f0d1buL,
)
