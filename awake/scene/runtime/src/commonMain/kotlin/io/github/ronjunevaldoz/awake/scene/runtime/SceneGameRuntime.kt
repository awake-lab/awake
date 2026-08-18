// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.core.application.FixedTimestepLoop
import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.engine.game.Game
import io.github.ronjunevaldoz.awake.engine.game.GameServiceLookup
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.render.texture.TextureAsset
import io.github.ronjunevaldoz.awake.scene.core.components.Name
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.core.systems.TransformSystem
import io.github.ronjunevaldoz.awake.scene.rendering.components.Camera
import io.github.ronjunevaldoz.awake.scene.rendering.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.rendering.systems.DebugVisualizationSystem
import io.github.ronjunevaldoz.awake.scene.rendering.systems.RenderSystem
import io.github.ronjunevaldoz.awake.ui.UiInputState
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.context.UiCursor
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.toUiInputState
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.ui.context.UiFrameInput

/**
 * Orchestrates a single 3D scene session.
 */
class SceneGameRuntime internal constructor(
    val spec: SceneGameSpec,
) : Game {
    private val fixedTimestepLoop = FixedTimestepLoop()

    /** All systems indexed by their handle. */
    private val registeredSystems = linkedMapOf<SceneSystemHandle<out System>, System>()

    /** Systems that run during fixed-timestep simulation steps. */
    private val fixedSystems = mutableListOf<System>()

    /** Systems that run once per rendered frame. */
    private val frameSystems = mutableListOf<System>()

    lateinit var world: World
        private set
    lateinit var renderer: Renderer
        private set

    private var assetLibrary: SceneAssetLibrary? = null

    /** Mandatory, not user-configurable -- see [SceneGameSpec.infrastructureSystemsFactory]. */
    private lateinit var infrastructureSystems: List<System>

    val uiContext = UiContext()
    val font: UiFont = UiFonts.default()

    /** The overlay's cursor request for the current frame -- a desktop host applies it by
     * passing `cursor = { runtime.cursor }` to `runVulkanDesktopGame` (see GameUiRuntime.cursor,
     * the same opt-in shape). Dropped on the floor before this existed: resize handles and text
     * fields requested cursors that no platform call ever consumed. */
    var cursor: UiCursor = UiCursor.Default
        private set

    /** Rolling window backing [averageFrameTimeMs]/[fps] -- same shape as
     * `GameUiRuntime.recordFrameTime`'s own tracker (see `SceneGameFrame.kt`'s `frameStats()`
     * port, which reads these). */
    private val frameTimesMs = ArrayDeque<Float>()

    private fun recordFrameTime(deltaSeconds: Float) {
        frameTimesMs.addLast(deltaSeconds * 1000f)
        while (frameTimesMs.size > FRAME_TIME_HISTORY_SIZE) frameTimesMs.removeFirst()
    }

    val averageFrameTimeMs: Float
        get() = if (frameTimesMs.isEmpty()) 0f else frameTimesMs.sum() / frameTimesMs.size

    val fps: Float
        get() = averageFrameTimeMs.takeIf { it > 0f }?.let { 1000f / it } ?: 0f

    val sceneName: String
        get() = spec.sceneName ?: "scene"

    private lateinit var services: GameServiceLookup

    /** Called at install time (see [SceneGameSpec.installInto]). */
    fun initialize(services: GameServiceLookup) {
        this.services = services
        this.world = World()
        this.assetLibrary = spec.assetLibraryFactory?.invoke()
    }

    override suspend fun ready(renderer: Renderer) {
        this.renderer = renderer
        infrastructureSystems = spec.infrastructureSystemsFactory(this)

        // 1. Instantiate systems (after renderer is available)
        spec.systems.forEach { registration ->
            val system = registration.factory(this)
            registeredSystems[registration.handle] = system

            // Sort into execution buckets owned by the scene runtime, not the system object.
            when (registration.phase) {
                SceneSystemPhase.Fixed -> fixedSystems.add(system)
                SceneSystemPhase.Frame -> frameSystems.add(system)
            }
        }

        // 2. RUN THE SCENE BUILDER BLUEPRINT (Delayed Execution)
        spec.scenePopulationBlock(this)

        // 3. Asset loading BEFORE the first system tick. onReady is where suspend resource
        // loads live, and a system's first update can already need them -- a demo that
        // activates on frame 0 and reads a scene document or a parsed model would otherwise
        // see null. GltfViewerDemo used to paper over this by re-checking every frame.
        spec.onReadyBlock(this)

        // 4. Initial sync pass for all frame-rate systems, infrastructure (transform
        // resolution + the draw pass) always last so it sees this frame's final state.
        frameSystems.forEach { it.update(world, 0f) }
        infrastructureSystems.forEach { it.update(world, 0f) }
    }

    override fun render(delta: Float, viewportWidth: Float, viewportHeight: Float) {
        val input = services.requireService(Input::class)
        val snapshot = input.currentSnapshot
        recordFrameTime(delta)

        // 1. UI Pass
        uiContext.beginFrame(
            UiFrameInput(
                viewportWidth = viewportWidth,
                viewportHeight = viewportHeight,
                input = snapshot.toUiInputState(),
                deltaSeconds = delta,
            ),
        )
        spec.overlayBlock(this, viewportWidth, viewportHeight)
        val uiFrame = uiContext.finishFrame()

        // 2. Stage UI before the infrastructure render pass. Vulkan/WebGPU consume these
        // runs while recording the present-producing renderer.draw() call below; staging
        // after RenderSystem would always draw the previous frame's overlay.
        renderer.drawUi(uiFrame.primitives, font)

        // 3. Simulation & Infrastructure Pump
        fixedTimestepLoop.advance(
            frameDelta = delta,
            fixedUpdate = { step ->
                fixedSystems.forEach { it.update(world, step) }
                spec.updateBlock(this, step, snapshot)
            },
            render = {
                frameSystems.forEach { it.update(world, delta) }
                infrastructureSystems.forEach { it.update(world, delta) }
            },
        )

        // 4. Sync UI focus state back to session input.
        input.textInputFocused = uiFrame.effects.requestKeyboard
        cursor = uiFrame.effects.cursor
    }

    override fun resize(width: Float, height: Float) = Unit

    override fun dispose() {
        spec.onDisposeBlock(this)
        assetLibrary?.dispose()
        assetLibrary = null
        registeredSystems.clear()
        fixedSystems.clear()
        frameSystems.clear()
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
                        model = transforms[index].worldMatrix,
                    ),
                )
                index += 1
            }
        }
    }

    fun <T : Any> service(type: kotlin.reflect.KClass<T>): T? = services.service(type)

    fun <T : Any> requireService(type: kotlin.reflect.KClass<T>): T = services.requireService(type)

    // registeredSystems is keyed by SceneSystemHandle<T>, and only ever populated (in ready())
    // with the System instance that handle's own factory produced, so `as? T` matches or the
    // error() below fires -- it never silently returns a mistyped system.
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

    fun requireEntity(name: String): Entity =
        findEntity(name) ?: error("Entity with name '$name' not found")

    fun findTransform(name: String): Transform? =
        findEntity(name)?.let { world.get(it, Transform::class) }

    fun requireTransform(name: String): Transform = world.get(requireEntity(name), Transform::class)
        ?: error("Entity '$name' has no Transform component")

    fun findCamera(name: String): Camera? = findEntity(name)?.let { world.get(it, Camera::class) }

    fun requireCamera(name: String): Camera = world.get(requireEntity(name), Camera::class)
        ?: error("Entity '$name' has no Camera component")

    val inputState: UiInputState get() = uiContext.inputState

    private companion object {
        const val FRAME_TIME_HISTORY_SIZE = 30
    }
}

/** The standard infrastructure trio every 3D scene needs -- transform resolution, the draw
 * pass, then debug wireframes (frustum/[io.github.ronjunevaldoz.awake.scene.rendering
 * .components.MeshBounds] boxes) drawn over whatever [RenderSystem] just drew. See
 * [SceneGameSpec.infrastructureSystemsFactory]'s doc comment for why this lives here instead
 * of being wired through the authoring DSL. [DebugVisualizationSystem] is a no-op (draws
 * nothing extra) unless a scene adds a
 * [io.github.ronjunevaldoz.awake.scene.rendering.components.WorldDebugSettings] entity and
 * toggles it on -- every existing scene is unaffected by its presence here. */
fun SceneGameRuntime.defaultInfrastructureSystems(): List<System> =
    listOf(TransformSystem(), RenderSystem(renderer), DebugVisualizationSystem(renderer))
