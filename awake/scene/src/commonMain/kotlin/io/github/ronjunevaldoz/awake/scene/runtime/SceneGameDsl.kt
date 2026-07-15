// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.engine.application.GameDsl
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.systems.FreeFlyCameraSystem
import io.github.ronjunevaldoz.awake.scene.systems.OrbitCameraSystem
import kotlin.reflect.KClass

typealias SceneRenderableFactory = SceneGameRuntime.(SceneRenderableRequest) -> MeshRenderer
typealias SceneSystemFactory = SceneGameRuntime.() -> System
typealias SceneUpdateBlock = SceneGameRuntime.(delta: Float) -> Unit
typealias SceneOverlayBlock = SceneGameRuntime.(viewportWidth: Float, viewportHeight: Float) -> Unit
typealias SceneReadyBlock = suspend SceneGameRuntime.() -> Unit
typealias SceneDisposeBlock = SceneGameRuntime.() -> Unit

fun GameDsl.ecs(block: SceneGameDsl.() -> Unit) {
    install(sceneGame(block))
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
    private var updateBlock: SceneUpdateBlock = { delta -> runAllSystems(delta) }
    private var overlayBlock: SceneOverlayBlock = { _, _ -> }
    private var onReadyBlock: SceneReadyBlock = {}
    private var onDisposeBlock: SceneDisposeBlock = {}
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
        onReadyBlock = block
    }

    fun onDispose(block: SceneDisposeBlock) {
        onDisposeBlock = block
    }

    fun <T : Any> service(type: KClass<T>, factory: SceneGameRuntime.() -> T) {
        serviceRegistrations += SceneServiceRegistration(type, factory)
    }

    inline fun <reified T : Any> service(noinline factory: SceneGameRuntime.() -> T) {
        service(T::class, factory)
    }

    internal fun build(): SceneGameSpec = SceneGameSpec(
        sceneDocument = sceneDocumentDsl.build(),
        renderableFactory = renderableFactory,
        assetLibraryFactory = assetLibraryFactory,
        systems = systemsDsl.build(),
        updateBlock = updateBlock,
        overlayBlock = overlayBlock,
        onReadyBlock = onReadyBlock,
        onDisposeBlock = onDisposeBlock,
        serviceRegistrations = serviceRegistrations.toList()
    )
}

fun SceneGameDsl.cameraEntity(
    name: String,
    transform: SceneTransformDsl.() -> Unit = {},
    camera: SceneCameraDsl.() -> Unit = {}
) {
    entity(name) {
        transform(transform)
        camera(camera)
    }
}

fun SceneGameDsl.meshEntity(
    name: String,
    mesh: String,
    material: String,
    transform: SceneTransformDsl.() -> Unit = {}
) {
    entity(name) {
        transform(transform)
        meshRenderer(mesh = mesh, material = material)
    }
}

fun SceneGameDsl.orbitCameraSystem(
    name: String = "orbit",
    target: String,
    camera: String,
    initialDistance: Float = 5f,
    autoRotateSpeed: Float = 0f,
    configure: OrbitCameraSystem.() -> Unit = {}
): SceneSystemHandle<OrbitCameraSystem> {
    return system(name) {
        OrbitCameraSystem(
            target = requireTransform(target),
            camera = requireCamera(camera),
            initialDistance = initialDistance,
            autoRotateSpeed = autoRotateSpeed
        ).also(configure)
    }
}

fun SceneGameDsl.freeFlyCameraSystem(
    name: String = "freeFly",
    camera: String,
    configure: FreeFlyCameraSystem.() -> Unit = {}
): SceneSystemHandle<FreeFlyCameraSystem> {
    return system(name) {
        FreeFlyCameraSystem(requireCamera(camera)).also(configure)
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
