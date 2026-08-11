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
        // Skipped where previewMetadataFor returns the ios-dummy placeholder (no reflection).
        if (!previewMetadataIsReal()) return
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
// 2026-08-10: re-recorded after the shadcn parity pass -- Tabs track height 32->36dp (h-9),
// FieldTextField/FieldDropdown control height 40->36dp (h-9, was the only place using h-10).
// 2026-08-10 (2): re-recorded after the source-verified parity wave -- radius ladder switched
// from additive to Tailwind's real multiplicative one (theming), TabsTrigger px-3->px-2 with a
// flush p-[3px] track (tabs), Field gaps -> gap-3/gap-6/gap-7 (text-input), AccordionContent -> pb-4 (collapsible).
// 2026-08-12: re-recorded after the showcase production pages moved behind the public
// Headless/design-system API boundary. These signatures intentionally describe the migrated
// semantic tree; the legacy Core receiver fixtures remain isolated in test adapters.
private val expectedShowcaseLayoutSignatures = mapOf(
    "ui-showcase-overview" to 0x694eafcf978453a4uL,
    "ui-showcase-theming" to 0x807fdcca0ae9fe9auL,
    "ui-showcase-typography" to 0x2bfd01ea67762bc1uL,
    "ui-showcase-buttons" to 0xbad90713d8cf6ac3uL,
    "ui-showcase-avatar" to 0xaffdfcd0972548d2uL,
    "ui-showcase-breadcrumb" to 0xfd47f30939350b6buL,
    "ui-showcase-card" to 0x1ecc8b3edbb0ddbcuL,
    "ui-showcase-sidebar" to 0xa2479909eeecf73fuL,
    "ui-showcase-selection" to 0x4e0943bd3fe46778uL,
    "ui-showcase-range-slider" to 0x694eafcf978453a4uL,
    "ui-showcase-tabs" to 0x5c2c46d942a9b328uL,
    "ui-showcase-select" to 0x1964a356a795b904uL,
    "ui-showcase-kbd-separator" to 0xd36862033db5d030uL,
    "ui-showcase-feedback" to 0x1f31ea9af55efc4auL,
    "ui-showcase-alert" to 0x41ddae5f89ef3012uL,
    "ui-showcase-text-input" to 0xd944fe31a6bc1e43uL,
    "ui-showcase-popups" to 0xcbb50501a3eb4cauL,
    "ui-showcase-state" to 0x694eafcf978453a4uL,
    "ui-showcase-button-matrix" to 0xf68e31f3e7ea6a13uL,
    "ui-showcase-field-matrix" to 0x63c1d456b2ad6367uL,
    "ui-showcase-slider-matrix" to 0xae9ea19252891af7uL,
    "ui-showcase-dropdown-open" to 0xb150c2def465181uL,
    "ui-showcase-popover-open" to 0x2f9cc696ffdb4080uL,
    "ui-showcase-tooltip-open" to 0xf53ee2754cd0cfc2uL,
    "ui-showcase-alert-dialog" to 0xf0028ed13c59150duL,
    "ui-showcase-scroll-panel" to 0x2702d804d61435buL,
    "ui-showcase-shimmer" to 0x694eafcf978453a4uL,
    "ui-showcase-collapsible" to 0xfa2c476e25c83a0buL,
    "ui-showcase-collapsible-open" to 0xeb6e5790876e4d63uL,
    "ui-showcase-field-demo" to 0x694eafcf978453a4uL,
)
