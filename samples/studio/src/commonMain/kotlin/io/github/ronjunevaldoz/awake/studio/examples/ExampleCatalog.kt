// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.examples

import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.SceneInstance

internal data class StudioExample(
    val id: String,
    val title: String,
    val scenePath: String,
    // Only the skinned-mesh example supplies one -- everything else is pure data.
    val driver: (SceneGameRuntime.(delta: Float) -> Unit)? = null,
    // Post-instantiate wiring for state a scene document can't author -- skinned-mesh's
    // SkinnedPose needs a joint palette sized/valued from the parsed skin at load time, not a
    // static JSON value. ExampleLoader stays generic; this is where the one real exception lives.
    val onActivated: ((instance: SceneInstance, world: World) -> Unit)? = null,
)

internal val StudioExamples: List<StudioExample> = listOf(
    StudioExample(
        id = "rotating-cube",
        title = "Rotating cube",
        scenePath = "assets/examples/rotating-cube.scene.json",
    ),
    StudioExample(
        id = "gltf-viewer",
        title = "glTF viewer",
        scenePath = "assets/examples/gltf-viewer.scene.json",
    ),
    StudioExample(
        id = "skinned-mesh",
        title = "Skinned mesh",
        scenePath = "assets/examples/skinned-mesh.scene.json",
        driver = { delta -> SkinnedExampleDriver.advance(this, delta) },
        onActivated = { instance, world -> SkinnedExampleDriver.attachPose(instance, world) },
    ),
)
