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
 * changes them -- catching drift at the whole-page level without screenshot flakiness.
 *
 * Every entry is derived from [ShowcasePages], so a page cannot be covered here without being
 * published in the app, and two entries can no longer share a fingerprint by both falling back
 * to the same page. */
class UiShowcaseLayoutSignatureTest {

    @Test
    fun showcasePageLayoutsRemainStableAcrossTargets() {
        val actual = UiShowcasePreviewEntries.associate { entry ->
            entry.metadata.id to layoutSignature(entry.render(entry.metadata).semantics)
        }

        assertEquals(
            expectedShowcaseLayoutSignatures.size,
            actual.size,
            "Preview page count changed. Refresh the expected matrix:\n${actual.toExpectedSignatureMatrix()}",
        )

        val mismatches = StringBuilder()
        expectedShowcaseLayoutSignatures.forEach { (id, expectedSignature) ->
            val entry = requireNotNull(UiShowcasePreviewEntries.find { it.metadata.id == id }) {
                "Missing preview $id"
            }
            if (actual.getValue(id) != expectedSignature) {
                val frame = entry.render(entry.metadata)
                mismatches.append(
                    "$id actual=0x${actual.getValue(id).toString(16)}\n${describeLayout(frame.semantics)}\n\n",
                )
            }
        }
        assertEquals("", mismatches.toString(), "Layout drift detected. New matrix:\n${actual.toExpectedSignatureMatrix()}")
    }

    /** A page whose fingerprint equals another page's is almost always a dispatch bug, not a
     * coincidence -- that is exactly how the old catalog hid five fixtures rendering the
     * Introduction page. */
    @Test
    fun everyPageProducesADistinctLayout() {
        val bySignature = UiShowcasePreviewEntries
            .groupBy { layoutSignature(it.render(it.metadata).semantics) }
            .filterValues { it.size > 1 }
            .mapValues { (_, entries) -> entries.map { it.page.id } }
        assertEquals(emptyMap(), bySignature, "Pages share a layout fingerprint: $bySignature")
    }
}

private fun Map<String, ULong>.toExpectedSignatureMatrix(): String =
    entries.joinToString(separator = "\n") { (id, signature) ->
        "\"$id\" to 0x${signature.toString(16)}uL,"
    }

// Recorded by `./gradlew :samples:ui-showcase:desktopTest --tests '*UiShowcaseLayoutSignatureTest*'`
// and pasting the printed matrix. Re-record only after reviewing why the layout moved.
private val expectedShowcaseLayoutSignatures = mapOf(
    "ui-showcase-introduction" to 0x8a51549d4a297537uL,
    "ui-showcase-theming" to 0x2112678dfe2efcc9uL,
    "ui-showcase-button" to 0x142e94a00d686c9fuL,
    "ui-showcase-badge" to 0x8a523832fa84eed9uL,
    "ui-showcase-text-input" to 0x9a85c2fde5e45043uL,
    "ui-showcase-text-area" to 0x12173df50c3c438uL,
    "ui-showcase-input-otp" to 0x25ec022c8dbdf883uL,
    "ui-showcase-input-group" to 0xd493d1c169738eaduL,
    "ui-showcase-checkbox" to 0x6d6a0d113a0c2c58uL,
    "ui-showcase-radio-group" to 0x9b0bb9d84ba12249uL,
    "ui-showcase-switch" to 0xc730603597788abauL,
    "ui-showcase-toggle" to 0x6419b459aa75b4uL,
    "ui-showcase-toggle-group" to 0x6f1cf99b1a14b8d5uL,
    "ui-showcase-slider" to 0x43a987e33f0d24a3uL,
    "ui-showcase-range-slider" to 0xf0aa667a3d81ffb5uL,
    "ui-showcase-select" to 0x1af1f728e000f49fuL,
    "ui-showcase-combobox" to 0x4a7dc950546551efuL,
    "ui-showcase-field" to 0x6d21e0ac695d3c06uL,
    "ui-showcase-card" to 0x5dddbb775edcf537uL,
    "ui-showcase-collapsible-card" to 0x97c0a7b9b2bf6ff0uL,
    "ui-showcase-tabs" to 0x9b387e3d516de71euL,
    "ui-showcase-accordion" to 0x8e21cff75a5650bduL,
    "ui-showcase-collapsible" to 0xdacdff8990b1e068uL,
    "ui-showcase-breadcrumb" to 0xd364ffc680527b16uL,
    "ui-showcase-sidebar" to 0x6a2f9a9c67b81cc5uL,
    "ui-showcase-resizable" to 0x10d27118889ab9b3uL,
    "ui-showcase-table" to 0x2073182b1039b4d6uL,
    "ui-showcase-scroll-area" to 0x36a1206354c845e1uL,
    "ui-showcase-separator" to 0x634876b925bd93b9uL,
    "ui-showcase-surface" to 0x10f57375cf7b4e0fuL,
    "ui-showcase-canvas" to 0x57943864db953412uL,
    "ui-showcase-dialog" to 0x3dd23bd345188634uL,
    "ui-showcase-alert-dialog" to 0x2fe1b2a317dbaf2cuL,
    "ui-showcase-drawer" to 0xb1ea5de4023e7e16uL,
    "ui-showcase-sheet" to 0x222b2a06c5918701uL,
    "ui-showcase-popover" to 0x48593e3a176593d3uL,
    "ui-showcase-dropdown-menu" to 0xf444ea2b2193d916uL,
    "ui-showcase-context-menu" to 0x839b5fbba0923d09uL,
    "ui-showcase-tooltip" to 0x36486ac6b1a4431cuL,
    "ui-showcase-alert" to 0xf820e3bcf8288ffbuL,
    "ui-showcase-avatar" to 0x60c7c7d143953af0uL,
    "ui-showcase-progress" to 0x7b1c5ba08662b0dduL,
    "ui-showcase-skeleton" to 0x3b7dde50cac61a51uL,
    "ui-showcase-spinner" to 0x7eff83aba2c368a2uL,
    "ui-showcase-toast" to 0x8d45b3fab5533a30uL,
    "ui-showcase-kbd" to 0xea2b94c4507aaf64uL,
    "ui-showcase-empty" to 0x48b50f2e270e107cuL,
    "ui-showcase-typography" to 0x6f3e20d589d74d17uL,
    "ui-showcase-form" to 0xc3b6b1bb69fa9491uL,
    "ui-showcase-button-group" to 0x756af5d8fda3e968uL,
    "ui-showcase-item" to 0xf66fd9521ea08146uL,
    "ui-showcase-chart" to 0x2c43bd692ce80f2buL,
    "ui-showcase-carousel" to 0x5f57a615951f44e7uL,
    "ui-showcase-date-picker" to 0x89df9422a8722dc8uL,
)
