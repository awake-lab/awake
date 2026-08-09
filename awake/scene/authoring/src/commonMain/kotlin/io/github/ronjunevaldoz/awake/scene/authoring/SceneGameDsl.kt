// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.authoring

import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.engine.gameauthoring.GameSpecDsl
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.AwakeSceneDsl
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.EntityModifier
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.Modifier
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.SceneBuilder
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.scene
import io.github.ronjunevaldoz.awake.scene.runtime.SceneAssetLibrary
import io.github.ronjunevaldoz.awake.scene.runtime.SceneDisposeBlock
import io.github.ronjunevaldoz.awake.scene.runtime.SceneDocument
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameSpec
import io.github.ronjunevaldoz.awake.scene.runtime.SceneOverlayBlock
import io.github.ronjunevaldoz.awake.scene.runtime.SceneReadyBlock
import io.github.ronjunevaldoz.awake.scene.runtime.SceneRenderableFactory
import io.github.ronjunevaldoz.awake.scene.runtime.SceneServiceRegistration
import io.github.ronjunevaldoz.awake.scene.runtime.SceneSystemHandle
import io.github.ronjunevaldoz.awake.scene.runtime.SceneSystemPhase
import io.github.ronjunevaldoz.awake.scene.runtime.SceneSystemRegistration
import io.github.ronjunevaldoz.awake.scene.runtime.SceneUpdateBlock
import io.github.ronjunevaldoz.awake.scene.runtime.attachRenderableComponents
import io.github.ronjunevaldoz.awake.scene.runtime.defaultInfrastructureSystems
import io.github.ronjunevaldoz.awake.scene.runtime.instantiate
import kotlin.reflect.KClass

fun GameSpecDsl.ecs(block: SceneGameDsl.() -> Unit) {
    install(sceneGame(block))
}

fun GameSpecDsl.ecs(spec: SceneGameSpec) {
    install(spec)
}

fun GameSpecDsl.scene(
    name: String? = null,
    block: SceneGameDsl.() -> Unit,
) {
    install(
        sceneGame {
            if (name != null) {
                this.name(name)
            }
            block()
        },
    )
}

fun GameSpecDsl.scene(spec: SceneGameSpec) {
    install(spec)
}

fun sceneGame(block: SceneGameDsl.() -> Unit): SceneGameSpec = SceneGameDsl().apply(block).build()

/**
 * Marked with [AwakeSceneDsl] so the enclosing `GameSpecDsl` receiver is hidden inside this
 * block. Without it, a `scene(name) { ... }` call here silently resolves to the outer
 * `GameSpecDsl.scene` extension and installs a whole second scene module -- two `World`s, two
 * render callbacks, and a `requireService<SceneGameRuntime>()` that returns the wrong one.
 */
@AwakeSceneDsl
class SceneGameDsl internal constructor() {
    private var sceneName: String? = null
    private var scenePopulationBlock: SceneGameRuntime.() -> Unit = {}
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
    private var infrastructureSystemsFactory: SceneGameRuntime.() -> List<System> =
        SceneGameRuntime::defaultInfrastructureSystems

    fun name(value: String?) {
        this.sceneName = value
    }

    /**
     * Captures the declarative entity layout block without running it yet.
     * It delays execution until the actual runtime engine assigns a World.
     */
    fun scene(name: String? = null, block: SceneBuilder.() -> Unit) {
        if (name != null) {
            this.sceneName = name
        }
        this.scenePopulationBlock = {
            world.scene(block)
        }
    }

    /**
     * Integrates an existing [io.github.ronjunevaldoz.awake.scene.runtime.SceneDocument] into the population block.
     */
    fun scene(document: SceneDocument) {
        this.sceneName = document.name
        this.scenePopulationBlock = {
            val scene = document.instantiate(world = world)
            scene.attachRenderableComponents { request -> spec.renderableFactory(this, request) }
        }
    }

    /**
     * Shortcut to spawn a root-level entity cleanly without nesting.
     */
    fun entity(
        name: String? = null,
        modifier: EntityModifier = Modifier(),
        block: SceneBuilder.() -> Unit = {},
    ) {
        // Since we want to preserve the population block, we append to it.
        val previous = scenePopulationBlock
        scenePopulationBlock = {
            previous()
            SceneBuilder(world).entity(name, modifier, block)
        }
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
        phase: SceneSystemPhase,
        factory: SceneGameRuntime.() -> T,
    ): SceneSystemHandle<T> = systemsDsl.system(name, phase, factory)

    fun <T : System> fixedSystem(
        name: String,
        factory: SceneGameRuntime.() -> T,
    ): SceneSystemHandle<T> = systemsDsl.fixedSystem(name, factory)

    fun <T : System> frameSystem(
        name: String,
        factory: SceneGameRuntime.() -> T,
    ): SceneSystemHandle<T> = systemsDsl.frameSystem(name, factory)

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

    /**
     * Overrides the mandatory transform-resolution + draw-pass systems (default:
     * [defaultInfrastructureSystems]) -- e.g. to swap in a custom render backend. This DSL
     * never imports a concrete render system itself; the override lambda is free to import
     * whatever it needs from the caller's own module.
     */
    fun infrastructureSystems(factory: SceneGameRuntime.() -> List<System>) {
        infrastructureSystemsFactory = factory
    }

    internal fun build(): SceneGameSpec = SceneGameSpec(
        sceneName = sceneName,
        systems = systemsDsl.build(),
        scenePopulationBlock = scenePopulationBlock,
        renderableFactory = renderableFactory,
        assetLibraryFactory = assetLibraryFactory,
        updateBlock = updateBlock,
        overlayBlock = overlayBlock,
        onReadyBlock = { onReadyBlocks.forEach { it(this) } },
        onDisposeBlock = { onDisposeBlocks.forEach { it(this) } },
        serviceRegistrations = serviceRegistrations.toList(),
        infrastructureSystemsFactory = infrastructureSystemsFactory,
    )
}

class SceneSystemsDsl internal constructor() {
    private val registrations = mutableListOf<SceneSystemRegistration>()

    fun <T : System> system(
        name: String,
        phase: SceneSystemPhase,
        factory: SceneGameRuntime.() -> T,
    ): SceneSystemHandle<T> {
        val handle = SceneSystemHandle<T>(name)
        registrations += SceneSystemRegistration(
            handle = handle,
            phase = phase,
            factory = { factory() },
        )
        return handle
    }

    fun <T : System> fixedSystem(
        name: String,
        factory: SceneGameRuntime.() -> T,
    ): SceneSystemHandle<T> = system(name, SceneSystemPhase.Fixed, factory)

    fun <T : System> frameSystem(
        name: String,
        factory: SceneGameRuntime.() -> T,
    ): SceneSystemHandle<T> = system(name, SceneSystemPhase.Frame, factory)

    internal fun build(): List<SceneSystemRegistration> = registrations.toList()
}
