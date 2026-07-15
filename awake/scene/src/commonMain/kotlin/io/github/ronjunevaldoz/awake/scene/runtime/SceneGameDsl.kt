// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.core.application.FixedTimestepLoop
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.engine.application.Game
import io.github.ronjunevaldoz.awake.engine.application.GameDsl
import io.github.ronjunevaldoz.awake.engine.application.GameInstaller
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.components.Name
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.systems.RenderSystem
import io.github.ronjunevaldoz.awake.scene.systems.TransformSystem
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.font.BitmapFont
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

class SceneGameRuntime internal constructor(
    private val spec: SceneGameSpec
) : Game {
    private val fixedTimestepLoop = FixedTimestepLoop()
    private val transformSystem = TransformSystem()
    private val registeredSystems = linkedMapOf<SceneSystemHandle<out System>, System>()
    private val systemNames = linkedMapOf<String, SceneSystemHandle<out System>>()
    private val namedEntities = linkedMapOf<String, Entity>()

    val uiContext = UiContext()
    val font = BitmapFont()
    val sceneDocument: SceneDocument
        get() = spec.sceneDocument
    val sceneName: String
        get() = sceneDocument.name ?: "scene"

    lateinit var renderer: Renderer
        private set
    lateinit var world: World
        private set
    lateinit var scene: SceneInstance
        private set

    private var assetLibrary: SceneAssetLibrary? = null
    private lateinit var renderSystem: RenderSystem

    override suspend fun ready(renderer: Renderer) {
        this.renderer = renderer
        world = World()
        scene = spec.sceneDocument.instantiate(
            flipYForClipSpace = renderer.flipYForClipSpace,
            world = world
        )
        assetLibrary = spec.assetLibraryFactory?.invoke()
        scene.attachRenderableComponents { request -> spec.renderableFactory(this, request) }
        renderSystem = RenderSystem(renderer)
        rebuildNameIndex()
        spec.systems.forEach { registration ->
            val system = registration.factory(this)
            registeredSystems[registration.handle] = system
            systemNames[registration.handle.name] = registration.handle
        }
        transformSystem.update(world, 0f)
        spec.onReadyBlock(this)
    }

    override fun render(delta: Float, viewportWidth: Float, viewportHeight: Float) {
        fixedTimestepLoop.advance(
            frameDelta = delta,
            fixedUpdate = { step -> spec.updateBlock(this, step) },
            render = {
                transformSystem.update(world, delta)
                renderSystem.update(world, delta)
            }
        )
        uiContext.beginFrame(viewportWidth, viewportHeight)
        spec.overlayBlock(this, viewportWidth, viewportHeight)
        renderer.drawUi(uiContext.endFrame(), font)
    }

    override fun dispose() {
        spec.onDisposeBlock(this)
        assetLibrary?.dispose()
        assetLibrary = null
        registeredSystems.clear()
        systemNames.clear()
        namedEntities.clear()
    }

    internal fun requireAssetLibrary(): SceneAssetLibrary = checkNotNull(assetLibrary) {
        "No scene asset library is registered for '${sceneName}'."
    }

    fun requireMesh(name: String): Mesh = requireAssetLibrary().requireMesh(this, name)

    fun requireMaterial(name: String): Material = requireAssetLibrary().requireMaterial(this, name)

    suspend fun readback(
        camera: CoreCamera,
        width: Int,
        height: Int
    ): TextureAsset {
        val target = renderer.createRenderTarget(width, height)
        return try {
            renderer.renderToTexture(target, camera, collectDrawCalls())
            renderer.readPixels(target)
        } finally {
            target.destroy()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : System> system(handle: SceneSystemHandle<T>): T {
        return checkNotNull(registeredSystems[handle]) {
            "No ECS system named '${handle.name}' is registered."
        } as T
    }

    fun system(name: String): System {
        val handle = checkNotNull(systemNames[name]) {
            "No ECS system named '$name' is registered."
        }
        return system(handle)
    }

    fun <T : System> update(handle: SceneSystemHandle<T>, delta: Float) {
        system(handle).update(world, delta)
    }

    fun runAllSystems(delta: Float) {
        registeredSystems.values.forEach { system ->
            system.update(world, delta)
        }
    }

    fun findEntity(name: String): Entity? = namedEntities[name]

    fun requireEntity(name: String): Entity {
        return checkNotNull(findEntity(name)) {
            "Scene is missing an entity named '$name'."
        }
    }

    fun findTransform(name: String): Transform? = findEntity(name)?.let { entity ->
        world.get(entity, Transform::class)
    }

    fun requireTransform(name: String): Transform {
        return checkNotNull(findTransform(name)) {
            "Entity '$name' has no Transform."
        }
    }

    fun findCamera(name: String): Camera? = findEntity(name)?.let { entity ->
        world.get(entity, Camera::class)
    }

    fun requireCamera(name: String): Camera {
        return checkNotNull(findCamera(name)) {
            "Entity '$name' has no Camera component."
        }
    }

    fun collectDrawCalls(): List<DrawCall> {
        val family = world.family<Transform, MeshRenderer>()
        val transforms = family.componentsA()
        val renderers = family.componentsB()
        return buildList(family.size) {
            var index = 0
            while (index < family.size) {
                add(
                    DrawCall(
                        mesh = renderers[index].mesh,
                        material = renderers[index].material,
                        model = transforms[index].worldMatrix
                    )
                )
                index += 1
            }
        }
    }

    private fun rebuildNameIndex() {
        namedEntities.clear()
        world.queryEach<Name> { entity, component ->
            namedEntities[component.value] = entity
        }
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
