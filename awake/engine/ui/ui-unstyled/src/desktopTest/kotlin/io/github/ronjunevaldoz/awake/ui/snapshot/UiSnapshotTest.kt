// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import kotlin.test.Test

/**
 * Writes review PNGs for humans while [UiSnapshotSignatureTest] owns the real pass/fail
 * regression check across targets.
 */
class UiSnapshotTest {

    @Test
    fun writeReviewSnapshots() {
        reviewSnapshotScenes().forEach { scene ->
            saveUiSnapshot(
                name = scene.name,
                primitives = scene.primitives,
                width = scene.width,
                height = scene.height,
                background = scene.background,
                font = scene.font
            )
        }
    }
}
