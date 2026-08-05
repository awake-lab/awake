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
        assertEquals("", mismatches.toString(), "Layout drift detected")
    }
}

private fun Map<String, ULong>.toExpectedSignatureMatrix(): String =
    entries.joinToString(separator = "\n") { (id, signature) ->
        "\"$id\" to 0x${signature.toString(16)}uL,"
    }

private val expectedShowcaseLayoutSignatures = mapOf(
    "ui-showcase-overview" to 0xac12db2fe045a416uL,
    "ui-showcase-reference" to 0x67d6f957ae8cce70uL,
    "ui-showcase-theming" to 0x636b16fdb20fddauL,
    "ui-showcase-typography" to 0x180993dc6ef77852uL,
    "ui-showcase-fonts" to 0xb5c4cad26f0b70b4uL,
    "ui-showcase-layout" to 0x286d54ed5b15bbdfuL,
    "ui-showcase-canvas" to 0x6f4568dab2bb568buL,
    "ui-showcase-slot-apis" to 0xfd9795dfe4f35312uL,
    "ui-showcase-buttons" to 0x578a8acdb2ac88cuL,
    "ui-showcase-avatar" to 0x5eeaaf1bfd496d3euL,
    "ui-showcase-breadcrumb" to 0xff3625bc22cba365uL,
    "ui-showcase-card" to 0xc6e896bf790cb624uL,
    "ui-showcase-sidebar" to 0xe253fcb383c2f667uL,
    "ui-showcase-selection" to 0x1a06ccc9c4b0707duL,
    "ui-showcase-range-slider" to 0x89240bc688e4a5b5uL,
    "ui-showcase-tabs" to 0xf9d2d2a9583436fcuL,
    "ui-showcase-select" to 0x6d0a0729fce1c1f4uL,
    "ui-showcase-kbd-separator" to 0x2ae01b0142056ae0uL,
    "ui-showcase-feedback" to 0xab5b188865f630e8uL,
    "ui-showcase-alert" to 0x80a50661f8af0d87uL,
    "ui-showcase-text-input" to 0x99f88dbd1c9adbc2uL,
    "ui-showcase-popups" to 0xcec55adbb6173237uL,
    "ui-showcase-state" to 0xebe905593907302buL,
    "ui-showcase-button-matrix" to 0x5812fb0da17d474fuL,
    "ui-showcase-field-matrix" to 0x251e913547bf1162uL,
    "ui-showcase-slider-matrix" to 0xf377de0cfe75f70euL,
    "ui-showcase-dropdown-open" to 0xa0457d6b37d05093uL,
    "ui-showcase-popover-open" to 0x50a1e32a8f663808uL,
    "ui-showcase-tooltip-open" to 0x93aa56f6c68f138fuL,
    "ui-showcase-alert-dialog" to 0xc3e854daba9436aeuL,
    "ui-showcase-scroll-panel" to 0x544e4f3d7ba3368uL,
    "ui-showcase-shimmer" to 0x916ca1899d866166uL,
    "ui-showcase-collapsible" to 0xecd50d0060f1897buL,
    "ui-showcase-collapsible-open" to 0xe4c1e149795ae2d5uL,
    "ui-showcase-easing-rest" to 0x9dc94198b03f5212uL,
    "ui-showcase-easing-in-flight" to 0xcbe4c004987f13deuL,
    "ui-showcase-easing-settled" to 0xbbf2c8f747ef0af7uL,
    "ui-showcase-field-demo" to 0x441070cbc2a865dcuL
)
