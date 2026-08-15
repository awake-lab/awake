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
    "ui-showcase-introduction" to 0x3ee1e5b9566d40fbuL,
    "ui-showcase-theming" to 0x5f36e3e90f228f82uL,
    "ui-showcase-button" to 0x2221e2bcb1aa56abuL,
    "ui-showcase-badge" to 0x283c1c73b3257b6fuL,
    "ui-showcase-text-input" to 0xfd7ac5421229b1b3uL,
    "ui-showcase-text-area" to 0xb5eed143485dd9a1uL,
    "ui-showcase-input-otp" to 0x437f0c808e7116d4uL,
    "ui-showcase-input-group" to 0x8da16edc61166781uL,
    "ui-showcase-checkbox" to 0x71df5a9b56c770f4uL,
    "ui-showcase-radio-group" to 0x6c37ae86e02a68e8uL,
    "ui-showcase-switch" to 0x9a70d53d8f45d334uL,
    "ui-showcase-toggle" to 0x8ba60245b135ebfeuL,
    "ui-showcase-toggle-group" to 0x8693b4671ca5e1bauL,
    "ui-showcase-slider" to 0x6589b325d0955ab0uL,
    "ui-showcase-range-slider" to 0xc55175b2e478b301uL,
    "ui-showcase-select" to 0xc33f67a282a18976uL,
    "ui-showcase-combobox" to 0x3284c5ab29030508uL,
    "ui-showcase-field" to 0x156d17f328fb5ef1uL,
    "ui-showcase-card" to 0x12cbf46f4341a6eduL,
    "ui-showcase-collapsible-card" to 0x6fc585bf377ff12uL,
    "ui-showcase-tabs" to 0xe99dda364918865auL,
    "ui-showcase-accordion" to 0xfba612c550a965f4uL,
    "ui-showcase-collapsible" to 0x1fe1892ba068ebc7uL,
    "ui-showcase-breadcrumb" to 0x70d6c6a2c7a0a5cuL,
    "ui-showcase-sidebar" to 0x78eb579eb3fe47fduL,
    "ui-showcase-resizable" to 0x2b5148f9cf6112b2uL,
    "ui-showcase-table" to 0x59557ec18906d9a5uL,
    "ui-showcase-scroll-area" to 0x4751f4b603318f30uL,
    "ui-showcase-separator" to 0xe0d497573f2c05c4uL,
    "ui-showcase-surface" to 0x4a962d0fa964d75duL,
    "ui-showcase-canvas" to 0xdf8b1bdc101aab40uL,
    "ui-showcase-dialog" to 0x88f6399a0ac4dd8duL,
    "ui-showcase-alert-dialog" to 0x57b34fc6cb41271euL,
    "ui-showcase-drawer" to 0xc17849a0ee99ee73uL,
    "ui-showcase-sheet" to 0xb80434c5a2edfb3buL,
    "ui-showcase-popover" to 0x7d16dc64976f104uL,
    "ui-showcase-dropdown-menu" to 0xbee079811ae7ae79uL,
    "ui-showcase-context-menu" to 0xc9482d0232247c19uL,
    "ui-showcase-tooltip" to 0x2af0a668aecf9b41uL,
    "ui-showcase-alert" to 0x8357727823815da5uL,
    "ui-showcase-avatar" to 0xd910c3970f10c12auL,
    "ui-showcase-progress" to 0xbfb41f13cf824a98uL,
    "ui-showcase-skeleton" to 0x8a0924e8e51d9b94uL,
    "ui-showcase-spinner" to 0xc5e2b398dacb43eeuL,
    "ui-showcase-toast" to 0xf5da38eb8e86335uL,
    "ui-showcase-kbd" to 0x8e01365bead6f038uL,
    "ui-showcase-empty" to 0x9a5f9fb8233c77d7uL,
    "ui-showcase-typography" to 0x5cbc025e90a358d1uL,
    "ui-showcase-form" to 0x4bbd977fd6da81e1uL,
    "ui-showcase-button-group" to 0xb21b6b5fb5f5120uL,
    "ui-showcase-item" to 0x8d2e095eb43dc24fuL,
    "ui-showcase-chart" to 0x8253d3e2d8d9c333uL,
    "ui-showcase-carousel" to 0xeedc402cc428e412uL,
    "ui-showcase-date-picker" to 0x140c3b57a6182446uL,
)
