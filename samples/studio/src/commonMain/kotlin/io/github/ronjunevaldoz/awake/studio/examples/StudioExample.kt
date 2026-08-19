// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.examples

import io.github.ronjunevaldoz.awake.scene.runtime.Scene
import io.github.ronjunevaldoz.awake.scene.runtime.SceneAppLifecycleRuntime

internal data class StudioExample(
    val id: String,
    val title: String,
    val scenePath: String,
    // Only the skinned-mesh example supplies one -- everything else is pure data.
    val driver: (SceneAppLifecycleRuntime.(delta: Float) -> Unit)? = null,
    // Post-instantiate wiring for state a scene document can't author -- skinned-mesh's
    // SkinnedPose needs a joint palette sized/valued from the parsed skin at load time, not a
    // static JSON value; instanced-cubes' InstancedMeshRenderer isn't an authorable
    // SceneComponent at all yet. ExampleLoader stays generic; this is where the small number of
    // real exceptions live. Takes the full [SceneGameRuntime] (not just `World`) since building
    // a mesh/material needs [SceneGameRuntime.requireAssetLibrary]/`.renderer`.
    val onActivated: ((instance: Scene, runtime: SceneAppLifecycleRuntime) -> Unit)? = null,
)

internal val StudioExamples: List<StudioExample> = listOf(
    StudioExample(
        id = "rotating-cube",
        title = "Rotating cube",
        scenePath = "assets/examples/rotating-cube.scene.json",
    ),
    // Camera only, no mesh/light -- fps-comparison baseline against a real entity-carrying
    // scene, not a scene the engine can actually render nothing in (a scene needs a primary
    // camera to be valid at all).
    StudioExample(
        id = "empty",
        title = "Empty",
        scenePath = "assets/examples/empty.scene.json",
    ),
    StudioExample(
        id = "gltf-viewer",
        title = "glTF viewer",
        scenePath = "assets/examples/gltf-viewer.scene.json",
        onActivated = { instance, runtime -> GltfViewerAssets.attach(instance, runtime) },
    ),
    StudioExample(
        id = "skinned-mesh",
        title = "Skinned mesh",
        scenePath = "assets/examples/skinned-mesh.scene.json",
        driver = { delta -> SkinnedExampleDriver.advance(this, delta) },
        onActivated = { instance, runtime -> SkinnedExampleDriver.attachPose(instance, runtime) },
    ),
    StudioExample(
        id = "instanced-cubes",
        title = "Instanced cubes",
        scenePath = "assets/examples/instanced-cubes.scene.json",
        onActivated = { instance, runtime ->
            InstancedCubesExampleDriver.attach(
                instance,
                runtime
            )
        },
    ),
    StudioExample(
        id = "instanced-skinned",
        title = "Instanced skinned",
        scenePath = "assets/examples/instanced-skinned.scene.json",
        driver = { delta -> InstancedSkinnedExampleDriver.advance(this, delta) },
        onActivated = { instance, runtime ->
            InstancedSkinnedExampleDriver.attach(
                instance,
                runtime
            )
        },
    ),
    StudioExample(
        id = "particles",
        title = "Particles",
        scenePath = "assets/examples/particles.scene.json",
        driver = { delta -> ParticleEmitterExampleDriver.advance(delta) },
        onActivated = { instance, runtime ->
            ParticleEmitterExampleDriver.attach(
                instance,
                runtime
            )
        },
    ),
)
