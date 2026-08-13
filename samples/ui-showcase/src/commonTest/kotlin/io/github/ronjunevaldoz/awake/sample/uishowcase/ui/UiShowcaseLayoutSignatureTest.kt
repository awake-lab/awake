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
// 2026-08-13: re-recorded after ShadcnSpacing and ShadcnTypography connected to awake:ui:tailwind tokens.
private val expectedShowcaseLayoutSignatures = mapOf(
    "ui-showcase-overview" to 0x79188b3c808a23a6uL,
    "ui-showcase-theming" to 0xb3c522fc421acd1auL,
    "ui-showcase-typography" to 0xa92190762ab5fcf0uL,
    "ui-showcase-buttons" to 0x96cb1fa965125481uL,
    "ui-showcase-avatar" to 0x6d728153633ac22uL,
    "ui-showcase-breadcrumb" to 0x7d2f897749db0173uL,
    "ui-showcase-card" to 0x4a8d4075b4d5dde4uL,
    "ui-showcase-sidebar" to 0xc8b88bc13b0ac248uL,
    "ui-showcase-selection" to 0x29b900b9656222bfuL,
    "ui-showcase-range-slider" to 0x79188b3c808a23a6uL,
    "ui-showcase-tabs" to 0x270b2e3470b3f800uL,
    "ui-showcase-select" to 0xf5adf224ab56ded9uL,
    "ui-showcase-kbd-separator" to 0x31d98453328e6294uL,
    "ui-showcase-feedback" to 0xad6ee71c2a2e8a12uL,
    "ui-showcase-alert" to 0x578280880356da3fuL,
    "ui-showcase-text-input" to 0x8ad66ff9f55804acuL,
    "ui-showcase-popups" to 0x6063f501bd02ed56uL,
    "ui-showcase-state" to 0x79188b3c808a23a6uL,
    "ui-showcase-button-matrix" to 0x1d06f7c87c59c48cuL,
    "ui-showcase-field-matrix" to 0xd354b39ceba4fa04uL,
    "ui-showcase-slider-matrix" to 0xa384db218f399fe2uL,
    "ui-showcase-dropdown-open" to 0xcabb5d30b5f17698uL,
    "ui-showcase-popover-open" to 0x88a9dcd3456880b9uL,
    "ui-showcase-tooltip-open" to 0x104714f7f619242fuL,
    "ui-showcase-alert-dialog" to 0xd51710cce4cdbb28uL,
    "ui-showcase-scroll-panel" to 0x1af4e701a70132ebuL,
    "ui-showcase-shimmer" to 0x79188b3c808a23a6uL,
    "ui-showcase-collapsible" to 0x366104cb75ae1ddbuL,
    "ui-showcase-collapsible-open" to 0x478be940b1ff6fd6uL,
    "ui-showcase-field-demo" to 0x79188b3c808a23a6uL,
)
