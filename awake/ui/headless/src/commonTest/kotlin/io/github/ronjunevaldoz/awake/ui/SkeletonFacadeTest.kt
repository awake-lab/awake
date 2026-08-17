// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.testing.ui.renderUiComponent
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.skeleton
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.api.dp
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Facade-level smoke coverage for [io.github.ronjunevaldoz.awake.ui.headless.skeleton] driven
 * only through the public `UiScope` API. No existing test file owned `skeleton` at any level in
 * `commonTest` (only a low-level pixel probe, `SkeletonShimmerPixelTest`, in `desktopTest`).
 */
class SkeletonFacadeTest {

    @Test
    fun rendersASemanticNodeAtItsRequestedBounds() {
        val frame = renderUiComponent(width = 200f, height = 100f) {
            skeleton(id = "skeleton.smoke", modifier = Modifier.width(120f.dp).height(16f.dp))
        }

        val bounds = frame.bounds("skeleton.smoke")
        assertEquals(120f, bounds.width)
        assertEquals(16f, bounds.height)
    }
}
