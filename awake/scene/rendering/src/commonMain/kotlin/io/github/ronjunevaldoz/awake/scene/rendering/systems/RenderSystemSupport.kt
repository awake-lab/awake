// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.rendering.systems

import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.rendering.components.Camera

/** Shared between [RenderSystem] and [DebugVisualizationSystem] -- both need "the" camera a
 * frame draws through. */
internal fun primaryCamera(world: World): Camera? {
    val family = world.family<Camera>()
    val cameras = family.components()
    var index = 0
    val count = family.size
    while (index < count) {
        val camera = cameras[index]
        if (camera.isPrimary) {
            return camera
        }
        index += 1
    }
    return null
}

/** ponytail: neither [RenderSystem] nor [DebugVisualizationSystem] knows the renderer's actual
 * viewport aspect ratio (`System.update(world, delta)` takes no viewport size, and `Renderer`
 * doesn't expose one) -- widening the frustum's horizontal extent past any real device's
 * aspect ratio (ultra-wide monitors included) keeps this a false-negative-only approximation:
 * it may under-cull on a narrow screen, never cull something actually visible. Upgrade path:
 * expose the renderer's real aspect ratio and pass it through here once that's a free contract
 * change to make. */
internal const val CONSERVATIVE_ASPECT = 3f
