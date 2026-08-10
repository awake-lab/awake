// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import io.github.ronjunevaldoz.awake.core.utils.summarizePixels
import io.github.ronjunevaldoz.awake.testing.ui.inspectUiFrame
import io.github.ronjunevaldoz.awake.testing.ui.rasterize
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import kotlin.test.Test
import kotlin.test.assertEquals

class UiSnapshotSignatureTest {

    @Test
    fun reviewSnapshotsRemainStableAcrossTargets() {
        assertSnapshotSignatures(reviewSnapshotScenes(), expectedReviewSnapshotSignatures)
    }

    @Test
    fun tutorialSnapshotsRemainStableAcrossTargets() {
        assertSnapshotSignatures(tutorialSnapshotScenes(), expectedTutorialSnapshotSignatures)
    }
}

private fun assertSnapshotSignatures(
    scenes: List<UiSnapshotScene>,
    expected: Map<String, ULong>,
) {
    val actual = scenes.associate { scene ->
        scene.name to scene.snapshotSignature().also { signature ->
            println("ui-snapshot-signature ${scene.name}=${signature.toHexString()}")
        }
    }

    assertEquals(expected.size, actual.size, "Snapshot scene count changed. Refresh the expected matrix.")
    expected.keys.forEach { name ->
        assertEquals(true, actual.containsKey(name), "Missing snapshot scene $name in actual results.")
    }

    scenes.forEach { scene ->
        val inspection = inspectUiFrame(
            primitives = scene.primitives,
            frame = UiBounds(0f, 0f, scene.width.toFloat(), scene.height.toFloat()),
            font = scene.font,
        )
        assertEquals(true, inspection.isClean, "UI inspection failed for ${scene.name}:\n${inspection.summary()}")
        val pixels = scene.primitives.rasterize(scene.width, scene.height, scene.background, scene.font)
        val summary = summarizePixels(pixels, scene.width, scene.height)
        val actualSignature = actual.getValue(scene.name)
        assertEquals(
            expected.getValue(scene.name),
            actualSignature,
            "Snapshot drift for ${scene.name}: actual=${actualSignature.toHexString()}, size=${scene.width}x${scene.height}, " +
                "center=${summary.center}, topLeft=${summary.topLeft}, topRight=${summary.topRight}, " +
                "bottomLeft=${summary.bottomLeft}, bottomRight=${summary.bottomRight}",
        )
    }
}

private fun UiSnapshotScene.snapshotSignature(): ULong {
    val pixels = primitives.rasterize(width, height, background, font)
    var hash = 0xcbf29ce484222325uL
    for (byte in pixels) {
        hash = hash xor (byte.toInt() and 0xFF).toULong()
        hash *= 0x100000001b3uL
    }
    return hash
}

// 2026-08-03: PackedUiFont.advanceFor() now clamps the pen step to the glyph's own quad right
// edge (see PackedUiFont.kt) -- the embedded Roboto data declared several letters' advances up
// to 42% narrower than their own ink, which rendered as adjacent glyphs visibly touching/merging
// at real UI text sizes. Every text-bearing scene's rasterized pixels shifted as a result, so
// every signature below was re-recorded against the fixed, non-overlapping glyph spacing.
// 2026-08-08: re-recorded for two stacked intended changes: (1) text()'s verticallyCentered
// default flipped to true in ac03b490 without a re-record at the time; (2) the AA fringe now
// centers on the true path boundary (interior insets by fringe/2) instead of dilating outward,
// so every filled-path edge crisped by ~0.5px.
// 2026-08-08: re-recorded after em normalisation was corrected. Glyph metrics were divided by
// the line-height cell (19) rather than the render font size (16), so every advance and quad was
// 16/19 = 0.842x too small and all text rendered ~19% narrow. Text is now its true size, and
// slots size to the line box (lineHeightEm) instead of the font size so they can contain it.
// 2026-08-10 (4): pxrange 4 -> 2. A distance field cannot encode a feature thinner than its
// own spread, and at 4 (with an em spanning 32 texels) 'i'/'l' stems were narrower than the
// range and rendered eroded next to rounder glyphs.
// 2026-08-10 (3): re-recorded after the atlas became MTSDF -- msdfgen-generated, 4-channel,
// sampled via median3 instead of coverage alpha. Every glyph's pixels are now resolved from a
// distance field rather than a resampled bitmap, so all of them changed.
// 2026-08-10 (2): re-recorded again after the atlas moved to TTF outline geometry
// (:awake:engine:ui:font-atlas-generator) and BasicText switched to snapping a line's pen
// origin once with exact per-glyph sizes. Every glyph's position and size changed.
// 2026-08-10: re-recorded after BasicText switched from rounding each glyph's origin and size
// independently to rounding its right/bottom edges from one unrounded origin -- the "wavy text"
// fix. Every text-bearing scene moved by up to a pixel per glyph.
private val expectedReviewSnapshotSignatures = mapOf(
    "toggle-unchecked" to 0xb316d0e6dcb666c7uL,
    "toggle-checked" to 0x5185876d2237a1e7uL,
    "button-filled" to 0xfe51d9503086a307uL,
    "button-outline" to 0xb537e459ca8ae28fuL,
    "button-ghost" to 0xd088286e8e753bd7uL,
    "theme-dark" to 0xd088286e8e753bd7uL,
    "theme-light" to 0x56db939aa658573buL,
    "panel-with-children" to 0x94f77e15d1e4ec86uL,
    "shadcn-field-error" to 0x43269f220a0ede6fuL,
)

// 2026-08-03: quads/rounded-quads/borders (surface fills, buttons, dialogs, separators) now
// pixel-snap their emitted position/size the same way BasicText.kt's glyph emission already
// did (see ShapePainter.kt/BorderPrimitives.kt/Separator.kt) -- previously only glyphs snapped
// to whole device pixels, so a bordered/panel-shaped widget at a sub-pixel layout position
// rendered a visibly softer/antialiased edge than the crisp text sitting right next to it.
// Every scene with a border/panel/button shifted its rasterized pixels by sub-pixel rounding as
// a result; re-recorded against the now-pixel-snapped primitives.
// 2026-08-08: re-recorded again after the shadcn token/radius value pass (base radius 6->10dp,
// additive radius scale, button/input rounded-md, accent/muted/sidebar-primary colors) -- every
// tutorial scene renders through ShadcnTheme, so all of them shifted.
// 2026-08-08: re-recorded once more after the glyph-advance coordinate-space fix -- advances
// were inflated by the atlas cell padding by a per-glyph-varying 6-29%, so every glyph in every
// text-bearing scene moved.
// 2026-08-08: ui-component-state-matrix only -- the checkbox corner moved off radii.md (8dp on a
// 16dp box, i.e. a circle) to shadcn's literal rounded-[4px]. It is the one scene with a checkbox.
// 2026-08-08: ui-component-state-matrix again -- the slider knob shrank 20dp -> 16dp with a 1dp
// border, shadcn v4's size-4 thumb. It is also the one scene with a slider.
private val expectedTutorialSnapshotSignatures = mapOf(
    "ui-button-variants" to 0x4a4f7b60a2c968eauL,
    "ui-shaped-panel" to 0xfff36e9ef3ce0671uL,
    "ui-panel-controls" to 0xe8c0139e5954717duL,
    "ui-alert-dialog" to 0x305dca0cd2c98dcfuL,
    "ui-component-state-matrix" to 0xe7e1a14f6542f14auL,
    "ui-rounded-clip-vector" to 0xedcf61ceec2b3df8uL,
    "ui-awake-shadcn-showcase" to 0x1c1d06abbc778966uL,
)

private fun ULong.toHexString(): String {
    val digits = CharArray(16)
    var value = this
    for (index in 15 downTo 0) {
        val nibble = (value and 0xFu).toInt()
        digits[index] = "0123456789abcdef"[nibble]
        value = value shr 4
    }
    return digits.concatToString()
}
