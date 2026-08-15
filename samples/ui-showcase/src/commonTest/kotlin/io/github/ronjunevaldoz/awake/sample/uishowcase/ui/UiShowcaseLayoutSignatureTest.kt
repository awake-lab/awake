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
    "ui-showcase-introduction" to 0x73300535625b60a7uL,
    "ui-showcase-theming" to 0x34b9ea6b47576315uL,
    "ui-showcase-button" to 0x61ec52dc42cd4973uL,
    "ui-showcase-badge" to 0x25f276b762315cfduL,
    "ui-showcase-text-input" to 0x35e62fa3a8019eafuL,
    "ui-showcase-text-area" to 0x535da6a6a9201e14uL,
    "ui-showcase-input-otp" to 0xd05e498f81d17a2fuL,
    "ui-showcase-input-group" to 0x14096feef985fe9uL,
    "ui-showcase-checkbox" to 0xa89c8677e8fd70e4uL,
    "ui-showcase-radio-group" to 0xdaf48b80e550f1f5uL,
    "ui-showcase-switch" to 0x984793bb36c1261euL,
    "ui-showcase-toggle" to 0x1ff2ebd7b9d3f848uL,
    "ui-showcase-toggle-group" to 0x536d7f5e4571e349uL,
    "ui-showcase-slider" to 0x5af620f98ef38e97uL,
    "ui-showcase-range-slider" to 0x9123ace1e8749679uL,
    "ui-showcase-select" to 0x5579cbaa37af1f2buL,
    "ui-showcase-combobox" to 0x73aa146fcf59cf3uL,
    "ui-showcase-field" to 0x12ec4cb03dae7352uL,
    "ui-showcase-card" to 0xa0a8e74f344fe9a3uL,
    "ui-showcase-collapsible-card" to 0xc1a61f0dc84ba924uL,
    "ui-showcase-tabs" to 0xb156b8d2bf6e8cbauL,
    "ui-showcase-accordion" to 0x31bf76a1ab62e769uL,
    "ui-showcase-collapsible" to 0x59137cffa6037f74uL,
    "ui-showcase-breadcrumb" to 0x7d1e2bb1d60dbd3auL,
    "ui-showcase-sidebar" to 0x2fde22a2a39c8aa9uL,
    "ui-showcase-resizable" to 0xa42afd03e9884647uL,
    "ui-showcase-table" to 0x77a47b2c79073ef2uL,
    "ui-showcase-scroll-area" to 0xdc2847d19e004da5uL,
    "ui-showcase-separator" to 0x354db75d31c202fduL,
    "ui-showcase-surface" to 0x761508d87efa6963uL,
    "ui-showcase-canvas" to 0x78535fd0b4197c1euL,
    "ui-showcase-dialog" to 0xb6cb95a8c43e0ff8uL,
    "ui-showcase-alert-dialog" to 0x889aa091f2e7b690uL,
    "ui-showcase-drawer" to 0x73f1eb5662fc26b2uL,
    "ui-showcase-sheet" to 0xa3a5d9c1eed89cf5uL,
    "ui-showcase-popover" to 0x4d688c2468e9d4b7uL,
    "ui-showcase-dropdown-menu" to 0xe5328174592c4502uL,
    "ui-showcase-context-menu" to 0xac20f06506b159duL,
    "ui-showcase-tooltip" to 0xcb1f9f4aaab7db10uL,
    "ui-showcase-alert" to 0xdbd5e7b26b97de7uL,
    "ui-showcase-avatar" to 0xb0c32de389683d34uL,
    "ui-showcase-progress" to 0x2c7f6f7a107fa5c9uL,
    "ui-showcase-skeleton" to 0xf93cc2945a9fe78duL,
    "ui-showcase-spinner" to 0x27a3668d8df68b1euL,
    "ui-showcase-toast" to 0x1d9a2d45b4be37ecuL,
    "ui-showcase-kbd" to 0xf64fb46ca756f600uL,
    "ui-showcase-empty" to 0x10a0510de429ab30uL,
    "ui-showcase-typography" to 0x8fc1b6cb4d10e9c3uL,
    "ui-showcase-form" to 0xafc68c13eaec6055uL,
    "ui-showcase-button-group" to 0xa67cd63a81af6f3cuL,
    "ui-showcase-item" to 0x9ee801cce32d1522uL,
    "ui-showcase-chart" to 0x694de8f84cc92427uL,
    "ui-showcase-carousel" to 0xaffc794df06230cbuL,
    "ui-showcase-date-picker" to 0x62db3d697c1b1bfcuL,
)
