// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreview
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewEntry
import io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewMetadata
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

private fun previewMetadataFor(entry: AwakeUiPreviewEntry): AwakeUiPreviewMetadata {
    val annotation = requireNotNull(entry.javaClass.getAnnotation(AwakeUiPreview::class.java)) {
        "missing @AwakeUiPreview on ${entry.javaClass.name}"
    }
    return AwakeUiPreviewMetadata(
        id = annotation.id,
        title = annotation.title,
        group = annotation.group,
        summary = annotation.summary,
        width = annotation.width,
        height = annotation.height
    )
}

private val expectedShowcaseLayoutSignatures = mapOf(
    "ui-showcase-overview" to 0x44612df645c45491uL,
    "ui-showcase-reference" to 0x8aa119eaada2bffduL,
    "ui-showcase-theming" to 0x4bc26926abb4f5fduL,
    "ui-showcase-fonts" to 0x794e83b39e69d69buL,
    "ui-showcase-layout" to 0xb3d64c9c5e74ede5uL,
    "ui-showcase-slot-apis" to 0xd6113229cbc9eb0uL,
    "ui-showcase-buttons" to 0x68c81904fe89789duL,
    "ui-showcase-popups" to 0xc2124a527e286c9auL,
    "ui-showcase-state" to 0xc8da84b5eb581396uL
)
