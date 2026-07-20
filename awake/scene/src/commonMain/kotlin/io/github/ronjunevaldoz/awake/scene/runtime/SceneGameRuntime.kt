// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.core.application.FixedTimestepLoop
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.System
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
import io.github.ronjunevaldoz.awake.scene.systems.RenderSystem
import io.github.ronjunevaldoz.awake.scene.systems.TransformSystem
import io.github.ronjunevaldoz.awake.ui.UiContext
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.UiTextEditAction
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts

private fun io.github.ronjunevaldoz.awake.core.input.InputSnapshot.toUiInputState(): UiInputState = UiInputState(
    pointerX = pointerX,
    pointerY = pointerY,
    pointerDown = pointerDown,
    scrollDeltaY = scrollDeltaY,
    typedText = typedText,
    editActions = editActions.map { it.toUiAction() }
)

private fun io.github.ronjunevaldoz.awake.core.input.TextEditAction.toUiAction(): UiTextEditAction = when (this) {
    io.github.ronjunevaldoz.awake.core.input.TextEditAction.Backspace -> UiTextEditAction.Backspace
    io.github.ronjunevaldoz.awake.core.input.TextEditAction.Delete -> UiTextEditAction.Delete
    io.github.ronjunevaldoz.awake.core.input.TextEditAction.Enter -> UiTextEditAction.Enter
    io.github.ronjunevaldoz.awake.core.input.TextEditAction.ArrowLeft -> UiTextEditAction.ArrowLeft
    io.github.ronjunevaldoz.awake.core.input.TextEditAction.ArrowRight -> UiTextEditAction.ArrowRight
    io.github.ronjunevaldoz.awake.core.input.TextEditAction.Home -> UiTextEditAction.Home
    io.github.ronjunevaldoz.awake.core.input.TextEditAction.End -> UiTextEditAction.End
}

/**
 * Orchestrates a single 3D scene session.
 *
 * A pure ECS coordinator: manages [World] and steps two sets of systems:
 * 1. Simulation Systems: Run at a fixed timestep (gameplay/physics).
 * 2. Infrastructure Systems: Run at frame rate (transform/rendering).
 */
class SceneGameRuntime internal constructor(
    private val spec: SceneGameSpec
) : Game {
    private val fixedTimestepLoop = FixedTimestepLoop()
    
    /** All systems indexed by their handle for direct lookup/update. */
    private val allSystems = linkedMapOf<SceneSystemHandle<out System>, System>()
    
    /** Gameplay systems that should run during the fixed-timestep simulation. */
    private val simulationSystems = mutableListOf<System>()
    
    /** Engine systems that must run every render frame (transforms, rendering). */
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

        // 1. Install user-defined gameplay systems
        spec.systems.forEach { registration ->
            val system = registration.factory(this)
            allSystems[registration.handle] = system
            simulationSystems.add(system)
        }
    }

    override suspend fun ready(renderer: Renderer) {
        this.renderer = renderer
        
        // 2. Install mandatory infrastructure systems (run at frame rate)
        val transform = TransformSystem()
        val render = RenderSystem(renderer)
        
        infrastructureSystems.add(transform)
        infrastructureSystems.add(render)
        
        // Add to the general map for lookup support
        allSystems[SceneSystemHandle("transform")] = transform
        allSystems[SceneSystemHandle("render")] = render
        
        // 3. Instantiate document entities
        val scene = spec.sceneDocument.instantiate(flipYForClipSpace = renderer.flipYForClipSpace, world = world)
        scene.attachRenderableComponents { request -> spec.renderableFactory(this, request) }
        
        // 4. Initial sync pass
        transform.update(world, 0f)
        
        spec.onReadyBlock(this)
    }

    override fun render(delta: Float, viewportWidth: Float, viewportHeight: Float) {
        val input = services.requireService(Input::class)
        val snapshot = input.currentSnapshot

        // 1. UI Pass: Highest priority for input interception.
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
                // Run all simulation/gameplay systems
                simulationSystems.forEach { it.update(world, step) }
                // User: Run high-level additive logic from the DSL
                spec.updateBlock(this, step, snapshot) 
            },
            render = {
                // Run all engine infrastructure systems at frame rate
                infrastructureSystems.forEach { it.update(world, delta) }
            }
        )
        
        // 3. Final Compositing
        renderer.drawUi(uiPrimitives, font)
        
        // Sync focused state back to hardware for next frame's OS poll
        input.textInputFocused = uiResult.isTextInputFocused
    }

    override fun resize(width: Float, height: Float) {}

    override fun dispose() {
        spec.onDisposeBlock(this)
        assetLibrary?.dispose()
        assetLibrary = null
        allSystems.clear()
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
        allSystems[handle] as? T ?: error("System ${handle.name} not found")

    fun system(name: String): System =
        allSystems.entries.firstOrNull { it.key.name == name }?.value
        ?: error("System $name not found")

    fun <T : System> update(handle: SceneSystemHandle<T>, delta: Float) {
        system(handle).update(world, delta)
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
