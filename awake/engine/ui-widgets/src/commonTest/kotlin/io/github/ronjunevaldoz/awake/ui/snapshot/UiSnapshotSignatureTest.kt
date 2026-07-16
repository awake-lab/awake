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
    "toggle-unchecked" to 0x12bffc42ff46a2d0uL,
    "toggle-checked" to 0x90ad956321fcd9c8uL,
    "button-filled" to 0x94191a15e06741a0uL,
    "button-outline" to 0x1558a22d610468e3uL,
    "button-ghost" to 0x94191a15e06741a0uL,
    "theme-dark" to 0x94191a15e06741a0uL,
    "theme-light" to 0x20b36f7ff1a5fb28uL,
    "panel-with-children" to 0x54d643c886f7cfc4uL
)

private val expectedTutorialSnapshotSignatures = mapOf(
    "ui-button-variants" to 0x998833c448c1d9d4uL,
    "ui-shaped-panel" to 0xff89a9a38d3e026auL,
    "ui-panel-controls" to 0x5f45da8eb2e13d55uL,
    "ui-rounded-clip-vector" to 0x4d0ab04bd0cf10e3uL,
    "ui-awake-shadcn-showcase" to 0x344c986a573b2073uL
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
