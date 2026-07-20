// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.core.application.FixedTimestepLoop
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.SystemFrequency
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.engine.application.Game
import io.github.ronjunevaldoz.awake.engine.application.GameServiceLookup
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.components.Name
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts


/**
 * Orchestrates a single 3D scene session.
 */
class SceneGameRuntime internal constructor(
    private val spec: SceneGameSpec
) : Game {
    private val fixedTimestepLoop = FixedTimestepLoop()
    
    /** All systems indexed by their handle. */
    private val registeredSystems = linkedMapOf<SceneSystemHandle<out System>, System>()
    
    /** Gameplay systems (simulation rate). */
    private val simulationSystems = mutableListOf<System>()
    
    /** Engine systems (frame rate). */
    private val infrastructureSystems = mutableListOf<System>()
    
    lateinit var world: World
        private set
    lateinit var renderer: Renderer
        private set
        
    private var assetLibrary: SceneAssetLibrary? = null

    val uiContext = UiContext()
    val font: UiFont = UiFonts.default()
    val sceneDocument: SceneDocument
        get() = spec.sceneDocument
    val sceneName: String
        get() = sceneDocument.name ?: "scene"

    private lateinit var services: GameServiceLookup

    /** Called at install time (see [SceneGameSpec.installInto]). */
    fun initialize(services: GameServiceLookup) {
        this.services = services
        this.world = World()
        this.assetLibrary = spec.assetLibraryFactory?.invoke()
    }

    override suspend fun ready(renderer: Renderer) {
        this.renderer = renderer
        
        // 1. Instantiate systems (after renderer is available)
        spec.systems.forEach { registration ->
            val system = registration.factory(this)
            registeredSystems[registration.handle] = system
            
            // Sort into execution buckets
            when (system.frequency) {
                SystemFrequency.Simulation -> simulationSystems.add(system)
                SystemFrequency.Infrastructure -> infrastructureSystems.add(system)
            }
        }

        // 2. Instantiate document entities
        val scene = spec.sceneDocument.instantiate(flipYForClipSpace = renderer.flipYForClipSpace, world = world)
        scene.attachRenderableComponents { request -> spec.renderableFactory(this, request) }
        
        // 3. Initial sync pass for all frame-rate systems
        infrastructureSystems.forEach { it.update(world, 0f) }
        
        spec.onReadyBlock(this)
    }

    override fun render(delta: Float, viewportWidth: Float, viewportHeight: Float) {
        val input = services.requireService(Input::class)
        val snapshot = input.currentSnapshot

        // 1. UI Pass
        uiContext.beginFrame(
            screenWidth = viewportWidth,
            screenHeight = viewportHeight,
            inputState = snapshot.toUiInputState(),
            deltaSeconds = delta
        )
        spec.overlayBlock(this, viewportWidth, viewportHeight)
        val uiPrimitives = uiContext.endFrame()
        val uiResult = uiContext.inputResult()

        // 2. Simulation & Infrastructure Pump
        fixedTimestepLoop.advance(
            frameDelta = delta,
            fixedUpdate = { step -> 
                simulationSystems.forEach { it.update(world, step) }
                spec.updateBlock(this, step, snapshot) 
            },
            render = {
                infrastructureSystems.forEach { it.update(world, delta) }
            }
        )
        
        // 3. Final Compositing
        renderer.drawUi(uiPrimitives, font)
        
        // Sync UI focus state back to session input
        input.textInputFocused = uiResult.isTextInputFocused
    }

    override fun resize(width: Float, height: Float) {}

    override fun dispose() {
        spec.onDisposeBlock(this)
        assetLibrary?.dispose()
        assetLibrary = null
        registeredSystems.clear()
        simulationSystems.clear()
        infrastructureSystems.clear()
    }

    fun requireAssetLibrary(): SceneAssetLibrary = checkNotNull(assetLibrary) {
        "No scene asset library is registered for '$sceneName'."
    }

    fun requireMesh(name: String): Mesh = requireAssetLibrary().requireMesh(this, name)

    fun requireMaterial(name: String): Material = requireAssetLibrary().requireMaterial(this, name)

    suspend fun readback(camera: CoreCamera, width: Int, height: Int): TextureAsset {
        val target = renderer.createRenderTarget(width, height)
        return try {
            renderer.renderToTexture(target, camera, collectDrawCalls())
            renderer.readPixels(target)
        } finally {
            target.destroy()
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

    fun <T : Any> service(type: kotlin.reflect.KClass<T>): T? = services.service(type)

    fun <T : Any> requireService(type: kotlin.reflect.KClass<T>): T = services.requireService(type)

    @Suppress("UNCHECKED_CAST")
    fun <T : System> system(handle: SceneSystemHandle<T>): T =
        registeredSystems[handle] as? T ?: error("System ${handle.name} not found")

    fun system(name: String): System =
        registeredSystems.entries.firstOrNull { it.key.name == name }?.value
        ?: error("System $name not found")

    fun <T : System> update(handle: SceneSystemHandle<T>, delta: Float) {
        val system = system(handle)
        system.update(world, delta)
    }

    fun findEntity(name: String): Entity? {
        var result: Entity? = null
        world.queryEach(Name::class) { entity, n ->
            if (n.value == name) result = entity
        }
        return result
    }

    fun requireEntity(name: String): Entity = findEntity(name) ?: error("Entity with name '$name' not found")

    fun findTransform(name: String): Transform? = findEntity(name)?.let { world.get(it, Transform::class) }

    fun requireTransform(name: String): Transform = world.get(requireEntity(name), Transform::class)
        ?: error("Entity '$name' has no Transform component")

    fun findCamera(name: String): Camera? = findEntity(name)?.let { world.get(it, Camera::class) }

    fun requireCamera(name: String): Camera = world.get(requireEntity(name), Camera::class)
        ?: error("Entity '$name' has no Camera component")

    val inputState: UiInputState get() = uiContext.inputState
}
