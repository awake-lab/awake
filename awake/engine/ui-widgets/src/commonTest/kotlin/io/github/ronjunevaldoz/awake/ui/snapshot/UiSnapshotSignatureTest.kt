// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import io.github.ronjunevaldoz.awake.core.utils.summarizePixels
import io.github.ronjunevaldoz.awake.testing.ui.rasterize
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
    "toggle-unchecked" to 0xc56a5405fac411b5uL,
    "toggle-checked" to 0x9488adb21fd13351uL,
    "button-filled" to 0x74bd46fa0a1ade9fuL,
    "button-outline" to 0x85eca633295c43a4uL,
    "button-ghost" to 0x74bd46fa0a1ade9fuL,
    "theme-dark" to 0x74bd46fa0a1ade9fuL,
    "theme-light" to 0xd9088febcb8d6643uL,
    "panel-with-children" to 0xc83494baea523568uL
)

private val expectedTutorialSnapshotSignatures = mapOf(
    "ui-button-variants" to 0x58fdfbde6d8a9dc3uL,
    "ui-shaped-panel" to 0x80fec6bc3c1ad8f7uL,
    "ui-panel-controls" to 0x2d56bee17ec2b17fuL,
    "ui-rounded-clip-vector" to 0x06cee56edf05a759uL,
    "ui-awake-shadcn-showcase" to 0xd1904aa80bce8613uL
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
