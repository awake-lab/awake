// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.engine.application.GameDsl
import io.github.ronjunevaldoz.awake.engine.application.GameModuleDsl
import io.github.ronjunevaldoz.awake.scene.runtime.systems.installInfrastructureSystems
import kotlin.reflect.KClass

fun GameDsl.ecs(block: SceneGameDsl.() -> Unit) {
    install(sceneGame(block))
}

fun GameDsl.ecs(spec: SceneGameSpec) {
    install(spec)
}

fun GameModuleDsl.ecs(block: SceneGameDsl.() -> Unit) {
    install(sceneGame(block))
}

fun GameModuleDsl.ecs(spec: SceneGameSpec) {
    install(spec)
}

fun GameDsl.scene(
    name: String? = null,
    block: SceneGameDsl.() -> Unit
) {
    install(
        sceneGame {
            if (name != null) {
                this.name(name)
            }
            block()
        }
    )
}

fun GameDsl.scene(spec: SceneGameSpec) {
    install(spec)
}

fun GameModuleDsl.scene(
    name: String? = null,
    block: SceneGameDsl.() -> Unit
) {
    install(
        sceneGame {
            if (name != null) {
                this.name(name)
            }
            block()
        }
    )
}

fun GameModuleDsl.scene(spec: SceneGameSpec) {
    install(spec)
}

fun sceneGame(block: SceneGameDsl.() -> Unit): SceneGameSpec {
    return SceneGameDsl().apply(block).build()
}

class SceneGameDsl internal constructor() {
    private var sceneDocumentDsl: SceneDocumentDsl = SceneDocumentDsl(null)
    private var renderableFactory: SceneRenderableFactory = {
        error("ecs { assets { ... } } or ecs { renderables { ... } } must resolve scene mesh/material requests.")
    }
    private var assetLibraryFactory: (() -> SceneAssetLibrary)? = null
    private val systemsDsl = SceneSystemsDsl()
    private var updateBlock: SceneUpdateBlock = { _, _ -> }
    private var overlayBlock: SceneOverlayBlock = { _, _ -> }
    private val onReadyBlocks = mutableListOf<SceneReadyBlock>()
    private val onDisposeBlocks = mutableListOf<SceneDisposeBlock>()
    private val serviceRegistrations = mutableListOf<SceneServiceRegistration<*>>()

    fun scene(
        name: String? = null,
        block: SceneDocumentDsl.() -> Unit
    ) {
        sceneDocumentDsl = SceneDocumentDsl(name).apply(block)
    }

    fun name(value: String?) {
        sceneDocumentDsl.name(value)
    }

    fun entity(
        name: String? = null,
        block: SceneNodeDsl.() -> Unit
    ) {
        sceneDocumentDsl.entity(name, block)
    }

    fun assets(block: SceneAssetsDsl.() -> Unit) {
        val dsl = SceneAssetsDsl().apply(block)
        assetLibraryFactory = dsl::buildLibrary
        renderableFactory = { request ->
            requireAssetLibrary().resolve(this, request)
        }
    }

    fun renderables(factory: SceneRenderableFactory) {
        renderableFactory = factory
    }

    fun <T : System> system(
        name: String,
        factory: SceneGameRuntime.() -> T
    ): SceneSystemHandle<T> {
        return systemsDsl.system(name, factory)
    }

    fun systems(block: SceneSystemsDsl.() -> Unit) {
        systemsDsl.apply(block)
    }

    fun update(block: SceneUpdateBlock) {
        updateBlock = block
    }

    fun overlay(block: SceneOverlayBlock) {
        overlayBlock = block
    }

    fun onReady(block: SceneReadyBlock) {
        onReadyBlocks += block
    }

    fun onDispose(block: SceneDisposeBlock) {
        onDisposeBlocks += block
    }

    fun <T : Any> service(type: KClass<T>, factory: SceneGameRuntime.() -> T) {
        serviceRegistrations += SceneServiceRegistration(type, factory)
    }

    inline fun <reified T : Any> service(noinline factory: SceneGameRuntime.() -> T) {
        service(T::class, factory)
    }

    internal fun build(): SceneGameSpec {
        // Core infrastructure: must run every frame (Infrastructure frequency).
        // Added at the end of build so they always execute LAST in the pipeline.
        installInfrastructureSystems()

        return SceneGameSpec(
            sceneDocument = sceneDocumentDsl.build(),
            renderableFactory = renderableFactory,
            assetLibraryFactory = assetLibraryFactory,
            systems = systemsDsl.build(),
            updateBlock = updateBlock,
            overlayBlock = overlayBlock,
            onReadyBlock = { onReadyBlocks.forEach { it(this) } },
            onDisposeBlock = { onDisposeBlocks.forEach { it(this) } },
            serviceRegistrations = serviceRegistrations.toList()
        )
    }
}

class SceneSystemsDsl internal constructor() {
    private val registrations = mutableListOf<SceneSystemRegistration>()

    fun <T : System> system(
        name: String,
        factory: SceneGameRuntime.() -> T
    ): SceneSystemHandle<T> {
        val handle = SceneSystemHandle<T>(name)
        registrations += SceneSystemRegistration(
            handle = handle,
            factory = { factory() }
        )
        return handle
    }

    internal fun build(): List<SceneSystemRegistration> = registrations.toList()
}
