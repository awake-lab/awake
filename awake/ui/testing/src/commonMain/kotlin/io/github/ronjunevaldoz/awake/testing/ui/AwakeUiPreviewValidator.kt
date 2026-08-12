// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.testing.ui

import io.github.ronjunevaldoz.awake.testing.comparePixels

/**
 * High-level UI preview verification. Validates semantics, layout, and pixels against a golden baseline.
 */
fun verifyAwakeUiPreview(
    scene: AwakeUiPreviewScene,
    config: AwakeUiPreviewValidationConfig = AwakeUiPreviewValidationConfig(),
    record: Boolean = false,
) {
    // 1. Semantic/Layout validation
    val report = validateAwakeUiPreview(scene, config)
    report.requireClean()

    // 2. Visual/Pixel validation
    val rasterized = scene.primitives.rasterize(
        width = scene.metadata.rasterWidth,
        height = scene.metadata.rasterHeight,
        background = scene.background,
        font = scene.font,
    )

    if (record) {
        saveAwakeUiSnapshot(scene.metadata.id, rasterized, scene.metadata.rasterWidth, scene.metadata.rasterHeight)
        return
    }

    val baseline = loadAwakeUiSnapshot(scene.metadata.id)
    if (baseline == null) {
        // Fallback: if no baseline exists, save this one as the initial baseline
        saveAwakeUiSnapshot(scene.metadata.id, rasterized, scene.metadata.rasterWidth, scene.metadata.rasterHeight)
        return
    }

    val diff = comparePixels(rasterized, baseline)
    if (!diff.matches) {
        val diffImage = generatePixelDiffImage(rasterized, baseline)
        saveAwakeUiDiff(scene.metadata.id, diffImage, scene.metadata.rasterWidth, scene.metadata.rasterHeight)
        throw AssertionError(
            "[${scene.metadata.id}] Visual regression detected! " +
                "Pixels mismatched: ${diff.diffPixelCount}. Max channel diff: ${diff.maxChannelDiff}. " +
                "Check build/ui-previews/${scene.metadata.id}_diff.png",
        )
    }
}

fun generatePixelDiffImage(actual: ByteArray, expected: ByteArray): ByteArray {
    val result = ByteArray(actual.size)
    var i = 0
    while (i < actual.size) {
        val r1 = actual[i].toInt() and 0xFF
        val g1 = actual[i + 1].toInt() and 0xFF
        val b1 = actual[i + 2].toInt() and 0xFF
        val a1 = actual[i + 3].toInt() and 0xFF

        val r2 = expected[i].toInt() and 0xFF
        val g2 = expected[i + 1].toInt() and 0xFF
        val b2 = expected[i + 2].toInt() and 0xFF
        val a2 = expected[i + 3].toInt() and 0xFF

        if (r1 != r2 || g1 != g2 || b1 != b2 || a1 != a2) {
            // Highlight diff in bright red
            result[i] = 255.toByte()
            result[i + 1] = 0.toByte()
            result[i + 2] = 0.toByte()
            result[i + 3] = 255.toByte()
        } else {
            // Dimmed actual for context
            result[i] = (r1 / 2).toByte()
            result[i + 1] = (g1 / 2).toByte()
            result[i + 2] = (b1 / 2).toByte()
            result[i + 3] = a1.toByte()
        }
        i += 4
    }
    return result
}
