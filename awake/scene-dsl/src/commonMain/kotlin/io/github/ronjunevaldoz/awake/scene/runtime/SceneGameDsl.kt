// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.engine.application.GameDsl
import io.github.ronjunevaldoz.awake.engine.application.GameModuleDsl
import io.github.ronjunevaldoz.awake.scene.systems.FreeFlyCameraSystem
import io.github.ronjunevaldoz.awake.scene.systems.OrbitCameraSystem
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
        system("transform") { io.github.ronjunevaldoz.awake.scene.systems.TransformSystem() }
        system("render") { io.github.ronjunevaldoz.awake.scene.systems.RenderSystem(renderer) }

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
    initialPitch: Float = 0.4f,
    autoRotateSpeed: Float = 0f,
    configure: OrbitCameraSystem.() -> Unit = {}
): SceneSystemHandle<OrbitCameraSystem> {
    onReady {
        val cameraEntity = requireEntity(camera)
        world.add(cameraEntity, io.github.ronjunevaldoz.awake.scene.components.OrbitControl().apply {
            this.target = requireTransform(target)
            this.distance = initialDistance
            this.pitch = initialPitch
        })
    }
    return system(name) {
        OrbitCameraSystem(
            autoRotateSpeed = autoRotateSpeed
        ).also(configure)
    }
}

fun SceneGameDsl.freeFlyCameraSystem(
    name: String = "freeFly",
    camera: String,
    configure: FreeFlyCameraSystem.() -> Unit = {}
): SceneSystemHandle<FreeFlyCameraSystem> {
    onReady {
        val cameraEntity = requireEntity(camera)
        world.add(cameraEntity, io.github.ronjunevaldoz.awake.scene.components.FreeFlyControl())
    }
    return system(name) {
        FreeFlyCameraSystem().also(configure)
    }
}

fun SceneGameDsl.playerControlSystem(
    name: String = "playerControl",
    rotateSpeed: Float = 0.01f,
    zoomSpeed: Float = 4f,
    pinchZoomSpeed: Float = 0.5f
): SceneSystemHandle<io.github.ronjunevaldoz.awake.scene.systems.PlayerControlSystem> {
    return system(name) {
        io.github.ronjunevaldoz.awake.scene.systems.PlayerControlSystem(
            rotateSpeed = rotateSpeed,
            zoomSpeed = zoomSpeed,
            pinchZoomSpeed = pinchZoomSpeed,
            inputProvider = { requireService(io.github.ronjunevaldoz.awake.core.input.Input::class).currentSnapshot },
            uiResultProvider = { uiContext.inputResult() }
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
