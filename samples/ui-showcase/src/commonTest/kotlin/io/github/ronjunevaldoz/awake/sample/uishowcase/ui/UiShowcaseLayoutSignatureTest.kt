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
// 2026-08-08: re-recorded after em normalisation was corrected -- text is now its true size
// and slots size to the line box, so every page reflowed.
private val expectedShowcaseLayoutSignatures = mapOf(
    "ui-showcase-overview" to 0xd9b72a019af9f240uL,
    "ui-showcase-theming" to 0x28aeb8300528c759uL,
    "ui-showcase-typography" to 0x53f8cd263d433510uL,
    "ui-showcase-buttons" to 0x1e39505ab6f95389uL,
    "ui-showcase-avatar" to 0xb5d5e50d63e30cc8uL,
    "ui-showcase-breadcrumb" to 0x98e2c5c61ca0a3feuL,
    "ui-showcase-card" to 0xaa2e30b4fc56d256uL,
    "ui-showcase-sidebar" to 0x7b873256c3c67de5uL,
    "ui-showcase-selection" to 0x9851f324c711f52fuL,
    "ui-showcase-range-slider" to 0xd9b72a019af9f240uL,
    "ui-showcase-tabs" to 0x6cd1171213742b62uL,
    "ui-showcase-select" to 0x8afc8aaf9465140duL,
    "ui-showcase-kbd-separator" to 0x97a104d253c8e4b4uL,
    "ui-showcase-feedback" to 0xf879b78c98dbb68cuL,
    "ui-showcase-alert" to 0xbd163943a933bd33uL,
    "ui-showcase-text-input" to 0xba6c8030fad27874uL,
    "ui-showcase-popups" to 0x2e01bc254488b7uL,
    "ui-showcase-state" to 0xd9b72a019af9f240uL,
    "ui-showcase-button-matrix" to 0xeb99998df754bcc9uL,
    "ui-showcase-field-matrix" to 0xd50c39a857cbcc0auL,
    "ui-showcase-slider-matrix" to 0x81cab0941278ca9uL,
    "ui-showcase-dropdown-open" to 0xe05bcbf8b275af25uL,
    "ui-showcase-popover-open" to 0xb3664fad437490c4uL,
    "ui-showcase-tooltip-open" to 0xd99d584368254fe9uL,
    "ui-showcase-alert-dialog" to 0xb9672b746a727b67uL,
    "ui-showcase-scroll-panel" to 0x4fa59d69b536da09uL,
    "ui-showcase-shimmer" to 0xd9b72a019af9f240uL,
    "ui-showcase-collapsible" to 0xd5b55d0a57787a1euL,
    "ui-showcase-collapsible-open" to 0x7f286dd9db6f7592uL,
    "ui-showcase-field-demo" to 0xd9b72a019af9f240uL,
)
