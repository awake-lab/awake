// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.engine.application.GameDsl
import io.github.ronjunevaldoz.awake.engine.application.GameInstaller
import kotlin.reflect.KClass

class SceneGameSpec internal constructor(
    val sceneDocument: SceneDocument,
    val renderableFactory: SceneRenderableFactory,
    internal val assetLibraryFactory: (() -> SceneAssetLibrary)?,
    internal val systems: List<SceneSystemRegistration>,
    val updateBlock: SceneUpdateBlock,
    val overlayBlock: SceneOverlayBlock,
    val onReadyBlock: SceneReadyBlock,
    val onDisposeBlock: SceneDisposeBlock,
    internal val serviceRegistrations: List<SceneServiceRegistration<*>>
) : GameInstaller {
    override fun install(into: GameDsl) {
        installInto(into)
    }

    fun installInto(into: GameDsl): SceneGameRuntime {
        val runtime = SceneGameRuntime(this)
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

internal class SceneServiceRegistration<T : Any>(
    val type: KClass<T>,
    val factory: SceneGameRuntime.() -> T
) {
    fun install(into: GameDsl, runtime: SceneGameRuntime) {
        into.service(type, runtime.factory())
    }
}
