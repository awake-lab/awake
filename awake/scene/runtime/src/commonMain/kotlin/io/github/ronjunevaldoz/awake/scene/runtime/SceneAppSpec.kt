// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.engine.app.core.AppModule
import io.github.ronjunevaldoz.awake.engine.app.dsl.AppSpecBuilder
import kotlin.reflect.KClass

class SceneAppSpec(
    val sceneName: String?,
    val systems: List<SceneSystemRegistration>,
    // Delayed setup mechanism for entities
    val scenePopulationBlock: SceneAppLifecycleRuntime.() -> Unit,
    val renderableFactory: SceneRenderableFactory,
    internal val assetLibraryFactory: (() -> SceneAssetLibrary)?,
    val updateBlock: SceneUpdateBlock,
    val overlayBlock: SceneOverlayBlock,
    val onReadyBlock: SceneReadyBlock,
    val onDisposeBlock: SceneDisposeBlock,
    internal val serviceRegistrations: List<SceneServiceRegistration<*>>,
    // Mandatory, not user-configurable data (every scene needs transform resolution + a draw
    // pass) -- pluggable so a game can swap the render backend, but defaults to the standard
    // pair so `authoring` never has to import RenderSystem just to get one running. See
    // defaultInfrastructureSystems() in SceneGameRuntime.kt.
    val infrastructureSystemsFactory: SceneAppLifecycleRuntime.() -> List<System> =
        SceneAppLifecycleRuntime::defaultInfrastructureSystems,
) : AppModule {
    override fun install(into: AppSpecBuilder) {
        installInto(into)
    }

    fun installInto(into: AppSpecBuilder): SceneAppLifecycleRuntime {
        val runtime = SceneAppLifecycleRuntime(this)
        runtime.initialize(into.serviceLookup())
        into.service(SceneAppLifecycleRuntime::class, runtime)
        serviceRegistrations.forEach { registration ->
            registration.install(into, runtime)
        }
        into.ready { renderer -> runtime.ready(renderer) }
        into.render { delta, viewportWidth, viewportHeight ->
            runtime.update(
                delta,
                viewportWidth,
                viewportHeight,
            )
        }
        into.resize { width, height -> runtime.resize(width, height) }
        into.pause { runtime.pause() }
        into.resume { runtime.resume() }
        into.dispose { runtime.dispose() }
        return runtime
    }
}

class SceneServiceRegistration<T : Any>(
    val type: KClass<T>,
    val factory: SceneAppLifecycleRuntime.() -> T,
) {
    fun install(into: AppSpecBuilder, runtime: SceneAppLifecycleRuntime) {
        into.service(type, runtime.factory())
    }
}
