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
        assertEquals(
            expectedShowcaseLayoutSignatures.size,
            actual.size,
            "Preview page count changed. Refresh the expected matrix:\n${actual.toExpectedSignatureMatrix()}"
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

private val expectedShowcaseLayoutSignatures = mapOf(
    "ui-showcase-overview" to 0x8a0f1cda327975f7uL,
    "ui-showcase-reference" to 0xf9a88a43c62c49a4uL,
    "ui-showcase-theming" to 0x822a241556cd9cccuL,
    "ui-showcase-typography" to 0x5cdcf234712bdbf4uL,
    "ui-showcase-fonts" to 0x7df78a966a5dd9d7uL,
    "ui-showcase-layout" to 0xa72d3e943b22fb76uL,
    "ui-showcase-canvas" to 0xaff7b03f01da04duL,
    "ui-showcase-slot-apis" to 0x219836857fb3f928uL,
    "ui-showcase-buttons" to 0xde54a672783258c1uL,
    "ui-showcase-avatar" to 0x5b00b491b02e85b4uL,
    "ui-showcase-breadcrumb" to 0xf1a3411b971e00c1uL,
    "ui-showcase-card" to 0xaa581a998f1e299cuL,
    "ui-showcase-sidebar" to 0x50ed283003cd4785uL,
    "ui-showcase-selection" to 0x4ddbcec9ac33021auL,
    "ui-showcase-range-slider" to 0x8a0f1cda327975f7uL,
    "ui-showcase-tabs" to 0xbba0ff689c10fa07uL,
    "ui-showcase-select" to 0x226a281e269989e7uL,
    "ui-showcase-kbd-separator" to 0x5e2ff8a0320ed449uL,
    "ui-showcase-feedback" to 0xb1fbe55169802a6buL,
    "ui-showcase-alert" to 0x78debd105ca87c64uL,
    "ui-showcase-text-input" to 0x60817494ca0c3165uL,
    "ui-showcase-popups" to 0xc40ccc36cfabe842uL,
    "ui-showcase-state" to 0x8a0f1cda327975f7uL,
    "ui-showcase-button-matrix" to 0x66fe64b834938154uL,
    "ui-showcase-field-matrix" to 0x36a633e7b46c562euL,
    "ui-showcase-slider-matrix" to 0x8f088fdc8f54c31cuL,
    "ui-showcase-dropdown-open" to 0xbd27e6fb16c60b5fuL,
    "ui-showcase-popover-open" to 0x65ca2799d16bc711uL,
    "ui-showcase-tooltip-open" to 0x8540c472d501ae7uL,
    "ui-showcase-alert-dialog" to 0xa895395688df6fbuL,
    "ui-showcase-scroll-panel" to 0x8aebd745f72c6a74uL,
    "ui-showcase-shimmer" to 0x8a0f1cda327975f7uL,
    "ui-showcase-collapsible" to 0xd30342b336d8d62auL,
    "ui-showcase-collapsible-open" to 0x5391468f2f853542uL,
    "ui-showcase-easing-rest" to 0xccb7d0688a0ebea8uL,
    "ui-showcase-easing-in-flight" to 0xe5a3e3af70265d3cuL,
    "ui-showcase-easing-settled" to 0x93cecaaeb60a27d3uL,
    "ui-showcase-field-demo" to 0x8a0f1cda327975f7uL
)
