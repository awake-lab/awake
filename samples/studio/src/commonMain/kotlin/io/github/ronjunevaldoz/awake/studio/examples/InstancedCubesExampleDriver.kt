// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.examples

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.scene.rendering.components.InstancedMeshRenderer
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.Scene

private const val GRID_SIDE = 10
private const val SPACING = 1.5f
private const val CUBE_Y = 0.5f

/** [InstancedMeshRenderer] isn't an authorable [io.github.ronjunevaldoz.awake.scene.runtime
 * .SceneComponent] yet -- the instanced-cubes example's scene document authors an empty,
 * named placeholder node instead (`instanced-cubes`, no components), same "author a named
 * node, attach the state a scene document can't express in `onActivated`" shape
 * [SkinnedExampleDriver.attachPose] already uses for its joint palette. Reuses the existing
 * `"cube"`/`"lit-shadow"` named assets (same mesh/material [samples.studio.StudioModule]
 * already registers for the rotating-cube example) -- one GPU draw call for the whole grid. */
internal object InstancedCubesExampleDriver {
    fun attach(instance: Scene, runtime: SceneGameRuntime) {
        val node = instance.roots.find { it.name == "instanced-cubes" } ?: return
        val mesh = runtime.requireMesh("cube")
        val material = runtime.requireMaterial("lit-shadow")
        runtime.world.add(node.entity, InstancedMeshRenderer(mesh, material, gridTransforms()))
    }

    private fun gridTransforms(): List<Mat4> {
        val offset = (GRID_SIDE - 1) * SPACING / 2f
        val transforms = mutableListOf<Mat4>()
        for (x in 0 until GRID_SIDE) {
            for (z in 0 until GRID_SIDE) {
                transforms += Mat4().translate(x * SPACING - offset, CUBE_Y, z * SPACING - offset)
            }
        }
        return transforms
    }
}
