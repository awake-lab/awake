// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.engine.application.GameModule
import io.github.ronjunevaldoz.awake.engine.application.GameSpecBuilder
import kotlin.reflect.KClass

class SceneGameSpec(
    val sceneName: String?,
    val systems: List<SceneSystemRegistration>,
    // Delayed setup mechanism for entities
    val scenePopulationBlock: SceneGameRuntime.() -> Unit,
    val renderableFactory: SceneRenderableFactory,
    internal val assetLibraryFactory: (() -> SceneAssetLibrary)?,
    val updateBlock: SceneUpdateBlock,
    val overlayBlock: SceneOverlayBlock,
    val onReadyBlock: SceneReadyBlock,
    val onDisposeBlock: SceneDisposeBlock,
    internal val serviceRegistrations: List<SceneServiceRegistration<*>>
) : GameModule {
    override fun install(into: GameSpecBuilder) {
        installInto(into)
    }

    fun installInto(into: GameSpecBuilder): SceneGameRuntime {
        val runtime = SceneGameRuntime(this)
        runtime.initialize(into.serviceLookup())
        into.service(SceneGameRuntime::class, runtime)
        serviceRegistrations.forEach { registration ->
            registration.install(into, runtime)
        }
        into.ready { renderer -> runtime.ready(renderer) }
        into.render { delta, viewportWidth, viewportHeight -> runtime.render(delta, viewportWidth, viewportHeight) }
        into.resize { width, height -> runtime.resize(width, height) }
        into.pause { runtime.pause() }
        into.resume { runtime.resume() }
        into.dispose { runtime.dispose() }
        return runtime
    }
}

class SceneServiceRegistration<T : Any>(
    val type: KClass<T>,
    val factory: SceneGameRuntime.() -> T
) {
    fun install(into: GameSpecBuilder, runtime: SceneGameRuntime) {
        into.service(type, runtime.factory())
    }
}
