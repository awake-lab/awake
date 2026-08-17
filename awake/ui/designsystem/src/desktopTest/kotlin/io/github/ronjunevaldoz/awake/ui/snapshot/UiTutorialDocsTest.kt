// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.snapshot

import io.github.ronjunevaldoz.awake.testing.ui.saveUiTutorialSnapshot
import kotlin.test.Test

/**
 * Curated tutorial snapshots for the developer docs pipeline. The PNGs come from the same
 * fixture catalog that powers the cross-platform signature test, so docs and regression
 * coverage stay in lockstep.
 */
class UiTutorialDocsTest {

    @Test
    fun writeTutorialSnapshots() {
        tutorialSnapshotScenes().forEach { scene ->
            saveUiTutorialSnapshot(
                name = scene.name,
                title = requireNotNull(scene.title) { "Tutorial snapshot ${scene.name} is missing a title" },
                summary = requireNotNull(scene.summary) { "Tutorial snapshot ${scene.name} is missing a summary" },
                primitives = scene.primitives,
                width = scene.width,
                height = scene.height,
                background = scene.background,
                font = scene.font,
            )
        }
    }
}
