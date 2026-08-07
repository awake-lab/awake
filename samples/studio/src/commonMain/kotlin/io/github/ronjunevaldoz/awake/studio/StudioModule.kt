// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio

import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.engine.application.GameModule
import io.github.ronjunevaldoz.awake.engine.application.gameModule
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.scene
import io.github.ronjunevaldoz.awake.studio.examples.ExampleLoader
import io.github.ronjunevaldoz.awake.studio.examples.GltfViewerAssets
import io.github.ronjunevaldoz.awake.studio.examples.SkinnedExampleDriver
import io.github.ronjunevaldoz.awake.studio.examples.StudioExamples
import io.github.ronjunevaldoz.awake.studio.examples.studioCubeGeometry
import io.github.ronjunevaldoz.awake.studio.examples.studioGroundGeometry
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import io.github.ronjunevaldoz.awake.studio.ui.drawStudioShell

/** mvp(16) + lightDirection(4) + lightColor(4) + lightMvp(16) + model(16) + cameraPosition(4)
 * + material(4). Must match `lit_shadow.wgsl`'s Uniforms field order. */
internal const val LIT_SHADOW_UNIFORM_FLOAT_COUNT = 64

internal fun studioModule(): GameModule {
    val store = StudioStore()
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
            overlay { viewportWidth, viewportHeight -> drawStudioShell(store, viewportWidth, viewportHeight) }
        }
    }
}

private class StudioExampleDriverSystem(
    private val runtime: SceneGameRuntime,
    private val store: StudioStore,
    private val loader: ExampleLoader,
) : System {
    override fun update(world: World, delta: Float) {
        store.drainEffects().forEach { effect ->
            when (effect) {
                is StudioContract.Effect.LoadExample -> loader.activate(effect.exampleId, runtime)
            }
        }
        val activeId = store.state.value.examples.activeExampleId
        StudioExamples.first { it.id == activeId }.driver?.invoke(runtime, delta)
    }
}
