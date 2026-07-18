// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import io.github.ronjunevaldoz.awake.core.utils.summarizePixels
import io.github.ronjunevaldoz.awake.testing.ui.inspectUiFrame
import io.github.ronjunevaldoz.awake.testing.ui.rasterize
import io.github.ronjunevaldoz.awake.ui.UiSlot
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
    expected: Map<String, ULong>
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
            frame = UiSlot(0f, 0f, scene.width.toFloat(), scene.height.toFloat()),
            font = scene.font
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
                "bottomLeft=${summary.bottomLeft}, bottomRight=${summary.bottomRight}"
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

private val expectedReviewSnapshotSignatures = mapOf(
    "toggle-unchecked" to 0x6854fe453d58a031uL,
    "toggle-checked" to 0x29a81be38aaad5abuL,
    "button-filled" to 0x0d0e346a89b128ebuL,
    "button-outline" to 0x9e39c0f79a46e67buL,
    "button-ghost" to 0x0d0e346a89b128ebuL,
    "theme-dark" to 0x0d0e346a89b128ebuL,
    "theme-light" to 0xb1b12af74057ca57uL,
    "panel-with-children" to 0x99acad4eb17c5899uL
)

private val expectedTutorialSnapshotSignatures = mapOf(
    "ui-button-variants" to 0xb8e6c0d1f0b1ebbfuL,
    "ui-shaped-panel" to 0x1d60a4bcd7d84cb5uL,
    "ui-panel-controls" to 0x2f285e4695975243uL,
    "ui-rounded-clip-vector" to 0x3ee797a9ba8c18abuL,
    "ui-awake-shadcn-showcase" to 0x1826286485e7aabeuL
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
