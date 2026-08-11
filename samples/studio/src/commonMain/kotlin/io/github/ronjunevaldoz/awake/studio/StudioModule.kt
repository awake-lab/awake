// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.engine.game.GameModule
import io.github.ronjunevaldoz.awake.engine.gameauthoring.gameModule
import io.github.ronjunevaldoz.awake.scene.authoring.scene
import io.github.ronjunevaldoz.awake.scene.rendering.components.Camera
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.studio.examples.ExampleLoader
import io.github.ronjunevaldoz.awake.studio.examples.GltfViewerAssets
import io.github.ronjunevaldoz.awake.studio.examples.SkinnedExampleDriver
import io.github.ronjunevaldoz.awake.studio.examples.StudioExamples
import io.github.ronjunevaldoz.awake.studio.examples.studioCubeGeometry
import io.github.ronjunevaldoz.awake.studio.examples.studioGroundGeometry
import io.github.ronjunevaldoz.awake.studio.state.CameraPresetMath
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import io.github.ronjunevaldoz.awake.studio.ui.drawStudioShell

/** mvp(16) + lightDirection(4) + lightColor(4) + lightMvp(16) + model(16) + cameraPosition(4)
 * + material(4). Must match `lit_shadow.wgsl`'s Uniforms field order. */
internal const val LIT_SHADOW_UNIFORM_FLOAT_COUNT = 64

internal fun studioModule(store: StudioStore = StudioStore()): GameModule {
    val loader = ExampleLoader()
    return gameModule {
        scene("studio") {
            assets {
                mesh("cube") { renderer.createMesh(studioCubeGeometry) }
                mesh("ground") { renderer.createMesh(studioGroundGeometry) }
                material("lit-shadow") { renderer.createMaterial(uniformFloatCount = LIT_SHADOW_UNIFORM_FLOAT_COUNT) }
                mesh("duck") { GltfViewerAssets.createMesh(this) }
                material("duck-material") { GltfViewerAssets.createMaterial(this) }
                mesh("skinned-mesh") { SkinnedExampleDriver.createMesh(this) }
                material("skinned-material") { SkinnedExampleDriver.createMaterial(this) }
            }
            frameSystem("example-driver") {
                StudioExampleDriverSystem(this, store, loader)
            }
            onReady {
                GltfViewerAssets.preload()
                SkinnedExampleDriver.preload()
                loader.preload()
                // dispatch, not activate() directly: only Intent.SelectExample queues the
                // LoadExample effect the driver system acts on. Without this, the store's
                // default activeExampleId sits "active" in the rail but nothing ever loads,
                // since dispatch only otherwise fires from a click.
                store.dispatch(StudioContract.Intent.SelectExample(StudioExamples.first().id))
            }
            overlay { viewportWidth, viewportHeight ->
                drawStudioShell(
                    store,
                    viewportWidth,
                    viewportHeight,
                )
            }
        }
    }
}

private class StudioExampleDriverSystem(
    private val runtime: SceneGameRuntime,
    private val store: StudioStore,
    private val loader: ExampleLoader,
) : System {
    // Reused every frame -- CameraPresetMath.applyPreset never allocates (core-math skill Rule 2).
    private val cameraForward = Vec3()

    override fun update(world: World, delta: Float) {
        store.drainEffects().forEach { effect ->
            when (effect) {
                is StudioContract.Effect.LoadExample -> {
                    loader.activate(effect.exampleId, runtime)
                    store.dispatch(
                        StudioContract.Intent.AppendConsole(
                            level = StudioContract.ConsoleLevel.Info,
                            message = "Loaded ${effect.exampleId}",
                        ),
                    )
                }
            }
        }
        val activeId = store.state.value.examples.activeExampleId
        StudioExamples.first { it.id == activeId }.driver?.invoke(runtime, delta)
        applyCameraPreset(world)
    }

    private fun applyCameraPreset(world: World) {
        val core = primaryCamera(world)?.camera ?: return
        CameraPresetMath.applyPreset(
            state = store.state.value.camera,
            target = core.center,
            camera = core,
            forwardScratch = cameraForward,
        )
    }

    /** Mirrors `RenderSystem.primaryCamera` -- the camera entity is recreated on every
     * `LoadExample`, so this looks it up fresh each frame rather than caching an `Entity`. */
    private fun primaryCamera(world: World): Camera? {
        val family = world.family<Camera>()
        val cameras = family.components()
        var index = 0
        while (index < family.size) {
            val camera = cameras[index]
            if (camera.isPrimary) return camera
            index += 1
        }
        return null
    }
}
