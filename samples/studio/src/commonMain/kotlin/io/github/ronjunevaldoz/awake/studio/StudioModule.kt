// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio

import io.github.ronjunevaldoz.awake.core.math.Aabb
import io.github.ronjunevaldoz.awake.core.math.Vec2
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.ecs.ensure
import io.github.ronjunevaldoz.awake.engine.game.GameModule
import io.github.ronjunevaldoz.awake.engine.game.GameWindowBackend
import io.github.ronjunevaldoz.awake.engine.gameauthoring.gameModule
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.scene.authoring.infrastructure.cameraInputSystem
import io.github.ronjunevaldoz.awake.scene.authoring.infrastructure.cameraSystem
import io.github.ronjunevaldoz.awake.scene.authoring.scene
import io.github.ronjunevaldoz.awake.scene.controls.components.ActiveCamera
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraComponent
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode
import io.github.ronjunevaldoz.awake.scene.core.components.SpinControl
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.core.systems.SpinSystem
import io.github.ronjunevaldoz.awake.scene.rendering.components.Camera
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.SceneLoader
import io.github.ronjunevaldoz.awake.studio.app.platformBackendPreference
import io.github.ronjunevaldoz.awake.studio.app.writeSceneDocument
import io.github.ronjunevaldoz.awake.studio.examples.ExampleLoader
import io.github.ronjunevaldoz.awake.studio.examples.GltfViewerAssets
import io.github.ronjunevaldoz.awake.studio.examples.InstancedSkinnedExampleDriver
import io.github.ronjunevaldoz.awake.studio.examples.SkinnedExampleDriver
import io.github.ronjunevaldoz.awake.studio.examples.StudioExamples
import io.github.ronjunevaldoz.awake.studio.examples.StudioMeshBounds
import io.github.ronjunevaldoz.awake.studio.examples.studioCubeGeometry
import io.github.ronjunevaldoz.awake.studio.examples.studioGroundGeometry
import io.github.ronjunevaldoz.awake.studio.gizmo.GizmoFrame
import io.github.ronjunevaldoz.awake.studio.gizmo.GizmoPick
import io.github.ronjunevaldoz.awake.studio.gizmo.GizmoPointer
import io.github.ronjunevaldoz.awake.studio.gizmo.GizmoTool
import io.github.ronjunevaldoz.awake.studio.gizmo.StudioGizmo
import io.github.ronjunevaldoz.awake.studio.gizmo.StudioViewportRect
import io.github.ronjunevaldoz.awake.studio.gizmo.ViewportProjection
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import io.github.ronjunevaldoz.awake.studio.ui.drawStudioShell
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sqrt
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera

/** mvp(16) + lightDirection(4) + lightColor(4) + lightMvp(16) + model(16) + cameraPosition(4)
 * + material(4). Must match `lit_shadow.wgsl`'s Uniforms field order. */
internal const val LIT_SHADOW_UNIFORM_FLOAT_COUNT = 64

/** [backend] is only a status-bar label. It is the backend this game asks its window for (see
 * `configureStudioWindow`), which is the closest honest answer available: `Renderer` exposes no
 * identity of its own. */
internal fun studioModule(
    store: StudioStore = StudioStore(),
    backend: GameWindowBackend = platformBackendPreference(),
    // Injectable so a test can assert what a save produced without writing to the real home
    // directory. Defaulted, so production wiring stays a one-liner.
    writeScene: (fileName: String, json: String) -> String = ::writeSceneDocument,
): GameModule {
    val loader = ExampleLoader()
    val backendLabel = backend.label()
    val gizmo = StudioGizmo()
    val viewportRect = StudioViewportRect()
    return gameModule {
        scene("studio") {
            assets {
                // Bounds are recorded here because this is the last point where the geometry is
                // still on the CPU -- createMesh uploads it and the vertices are gone.
                mesh("cube") { renderer.createMesh(studioCubeGeometry.alsoRecordBounds("cube")) }
                mesh("ground") { renderer.createMesh(studioGroundGeometry.alsoRecordBounds("ground")) }
                material("lit-shadow") { renderer.createMaterial(uniformFloatCount = LIT_SHADOW_UNIFORM_FLOAT_COUNT) }
                mesh("duck") { GltfViewerAssets.createMesh(this) }
                material("duck-material") { GltfViewerAssets.createMaterial(this) }
                mesh("skinned-mesh") { SkinnedExampleDriver.createMesh(this) }
                material("skinned-material") { SkinnedExampleDriver.createMaterial(this) }
                mesh("instanced-skinned-mesh") { InstancedSkinnedExampleDriver.createMesh(this) }
                material("instanced-skinned-material") { InstancedSkinnedExampleDriver.createMaterial(this) }
            }
            // The input system writes engine CameraMode hotkeys before the studio bridge syncs
            // them into UI state; CameraSystem then applies the pose after that bridge has
            // handled a header/menu selection for this frame.
            cameraInputSystem()
            // Registered here rather than left out: the rotating-cube scene authors a
            // SpinControl that nothing was ticking, so studio's "rotating cube" never rotated.
            // Two systems because SpinControl is a "control carries state, system only composes"
            // component (see SpinSystem): something has to advance the angle, and the engine
            // leaves that to whoever owns the spin rate. In an editor, that owner is play mode.
            frameSystem("spin-clock") { PlayModeSystem(SpinClockSystem(), store) }
            frameSystem("spin") { PlayModeSystem(SpinSystem(), store) }
            frameSystem("example-driver") {
                StudioExampleDriverSystem(this, store, loader, writeScene)
            }
            // After the driver (so a freshly loaded scene is pickable this frame) and before the
            // infrastructure systems, whose RenderSystem consumes the staged handle lines.
            frameSystem("gizmo") { StudioGizmoSystem(this, store, gizmo, viewportRect, loader::boundsOf) }
            cameraSystem()
            onReady {
                GltfViewerAssets.preload()
                SkinnedExampleDriver.preload()
                InstancedSkinnedExampleDriver.preload()
                loader.preload()
                // dispatch, not activate() directly: only Intent.SelectExample queues the
                // LoadExample effect the driver system acts on. Without this, the store's
                // default activeExampleId sits "active" in the rail but nothing ever loads,
                // since dispatch only otherwise fires from a click.
                store.dispatch(StudioContract.Intent.SelectExample(StudioExamples.first().id))
            }
            overlay { viewportWidth, viewportHeight ->
                drawStudioShell(
                    store,
                    backendLabel,
                    viewportWidth,
                    viewportHeight,
                    viewportRect,
                )
            }
        }
    }
}

/** Records [meshId]'s local bounds for picking, and returns the geometry unchanged. */
private fun MeshGeometry.alsoRecordBounds(meshId: String): MeshGeometry = also {
    StudioMeshBounds.register(meshId, vertices, format.strideBytes / Float.SIZE_BYTES)
}

// Spelled out rather than derived from `name`: enum-case text ("WEBGPU") is not how any of these
// backends is written.
private fun GameWindowBackend.label(): String = when (this) {
    GameWindowBackend.DEFAULT -> "Default"
    GameWindowBackend.VULKAN -> "Vulkan"
    GameWindowBackend.WEBGPU -> "WebGPU"
    GameWindowBackend.OPENGL -> "OpenGL"
}

/** Advances every [SpinControl] by its own rate, which is the half [SpinSystem] deliberately
 * does not do. Runs before it, so the composed transform is this frame's angle. */
private class SpinClockSystem : System {
    override fun update(world: World, delta: Float) {
        world.queryEach(SpinControl::class) { _, spin -> spin.radians += spin.speed * delta }
    }
}

/** Ticks [delegate] only in [StudioContract.Mode.Play]. A wrapper rather than a mode check
 * inside each system: the systems are engine-owned, and an editor's mode is not their concern. */
private class PlayModeSystem(private val delegate: System, private val store: StudioStore) : System {
    override fun update(world: World, delta: Float) {
        if (store.state.value.mode == StudioContract.Mode.Play) delegate.update(world, delta)
    }
}

/**
 * Drives the translate gizmo once per frame.
 *
 * A system rather than something hooked into the overlay: the UI layout callback it used to run
 * from fires several times per frame (the resizable group measures before it places), which
 * advanced a drag's state machine multiple times per frame and against intermediate rects.
 *
 * The viewport rect comes from [StudioViewportRect], written by the shell during layout -- the
 * same rect the scene is rendered into, so a pick can never disagree with what the user sees.
 */
/** Below this the authored eye sits on the target and describes no direction to orbit from. */
private const val MIN_AUTHORED_DISTANCE = 1e-4f

/** Standing eye height above the aim point, so first person starts outside whatever it was
 * looking at rather than inside it. */
private const val FIRST_PERSON_EYE_HEIGHT = 1.8f

private class StudioGizmoSystem(
    private val runtime: SceneGameRuntime,
    private val store: StudioStore,
    private val gizmo: StudioGizmo,
    private val viewportRect: StudioViewportRect,
    private val boundsOf: (Int) -> Aabb?,
) : System {
    override fun update(world: World, delta: Float) {
        val viewport = viewportRect.bounds ?: return
        val camera = primaryCamera(world) ?: return
        val projection = ViewportProjection(
            camera = camera.camera,
            viewProjection = camera.camera.viewProjectionMatrix(
                viewport.width / viewport.height,
                runtime.renderer.clipSpace,
            ),
            width = viewport.width,
            height = viewport.height,
        )

        val input = runtime.uiContext.inputState
        val insideViewport = input.pointerX >= viewport.x &&
            input.pointerX <= viewport.x + viewport.width &&
            input.pointerY >= viewport.y &&
            input.pointerY <= viewport.y + viewport.height
        // null rather than a clamped edge position: a drag must not keep running against the
        // panel border after the cursor leaves it.
        val local = if (insideViewport) Vec2(input.pointerX - viewport.x, input.pointerY - viewport.y) else null

        val selected = store.state.value.inspector.selectedEntityId
        val tool = store.state.value.tools.active.toGizmoTool()
        // Edit mode only: dragging while gameplay systems own the transform would fight them, and
        // the result is discarded on stop anyway.
        if (store.state.value.mode == StudioContract.Mode.Edit) {
            val pick = gizmo.update(
                world,
                GizmoFrame(projection, selected, GizmoPointer(local, input.pointerDown), tool),
                boundsOf,
            )
            // Only a frame that actually resolved a press changes the selection. Dispatching on
            // every frame the button is held cleared it one frame after making it.
            if (pick is GizmoPick.Selected && pick.entityId != selected) {
                store.dispatch(StudioContract.Intent.SelectEntity(pick.entityId))
            }
        }
        runtime.renderer.drawDebugLines(gizmo.handleLines(world, selected, tool))
    }

    /** Mapped rather than shared: the gizmo does not depend on studio's store, and a UI enum
     * gaining a case that has no drag behaviour should fail to compile here. */
    private fun StudioContract.Tool.toGizmoTool(): GizmoTool = when (this) {
        StudioContract.Tool.Select -> GizmoTool.Select
        StudioContract.Tool.Move -> GizmoTool.Move
        StudioContract.Tool.Rotate -> GizmoTool.Rotate
        StudioContract.Tool.Scale -> GizmoTool.Scale
    }

    private fun primaryCamera(world: World): Camera? {
        var found: Camera? = null
        world.family<Camera>().forEach { _, camera -> if (found == null && camera.isPrimary) found = camera }
        return found
    }
}

private class StudioExampleDriverSystem(
    private val runtime: SceneGameRuntime,
    private val store: StudioStore,
    private val loader: ExampleLoader,
    private val writeScene: (fileName: String, json: String) -> String,
) : System {
    private var syncedCameraEntity: Entity? = null
    private var syncedStoreMode: CameraMode? = null
    private var syncedComponentMode: CameraMode? = null

    override fun update(world: World, delta: Float) {
        store.drainEffects().forEach { effect ->
            when (effect) {
                StudioContract.Effect.SaveScene -> saveActiveScene(world)

                is StudioContract.Effect.LoadExample -> {
                    loader.activate(effect.exampleId, runtime)
                    store.dispatch(
                        StudioContract.Intent.AppendConsole(
                            level = StudioContract.ConsoleLevel.Info,
                            message = "Loaded ${effect.exampleId}",
                        ),
                    )
                }
            }
        }
        // Drivers are gameplay: an animation that keeps running in Edit would fight every
        // inspector edit, and its result would be saved as if it had been authored.
        if (store.state.value.mode == StudioContract.Mode.Play) {
            val activeId = store.state.value.examples.activeExampleId
            StudioExamples.first { it.id == activeId }.driver?.invoke(runtime, delta)
        }
        syncCameraComponent(world)
    }

    /**
     * Exports the LIVE world, so inspector edits are what land on disk -- not the document that
     * was loaded. Failures are reported to the studio console rather than thrown: a save that
     * cannot resolve an asset ID must not take the frame loop down with it.
     */
    private fun saveActiveScene(world: World) {
        val exampleId = store.state.value.examples.activeExampleId
        val message = runCatching {
            val document = loader.exportActive(world, name = exampleId)
            val target = writeScene("$exampleId.scene.json", SceneLoader.encode(document))
            StudioContract.ConsoleLevel.Info to "Saved to $target"
        }.getOrElse { failure ->
            StudioContract.ConsoleLevel.Error to "Save failed: ${failure.message}"
        }
        store.dispatch(StudioContract.Intent.AppendConsole(message.first, message.second))
    }

    /**
     * Pushes the store's camera state onto the ECS [CameraComponent] that `CameraSystem` reads.
     *
     * Studio used to run its own `CameraPresetMath` here, a second camera implementation beside
     * the engine's. That left Cinematic and TopDown unreachable and drag responding only in
     * Orbit, because `CameraInputSystem`'s per-mode usesYaw/usesPitch/usesZoom handling was never
     * in the loop. The store stays the source of truth for what the UI selected; the systems own
     * how that mode behaves.
     *
     * The camera entity is recreated on every `LoadExample`, so the components are ensured each
     * frame rather than attached once at setup.
     */
    private fun syncCameraComponent(world: World) {
        val entity = primaryCameraEntity(world) ?: return
        val renderingCamera = world.get<Camera>(entity) ?: return
        world.ensure(entity) { ActiveCamera() }
        val needsTargetInitialization = !world.has(entity, CameraComponent::class)
        val camera = world.ensure(entity) { CameraComponent() }
        // Scene documents author a rendering Camera but no Transform. CameraSystem requires a
        // Transform target for every mode, so the camera entity itself owns a stable target
        // initialized from the document's original aim point.
        val target = world.ensure(entity, ::Transform)
        if (needsTargetInitialization) target.position.set(renderingCamera.camera.center)
        camera.targetEntity = entity
        if (needsTargetInitialization) {
            seedFromAuthoredPose(camera, renderingCamera.camera)
        }
        val stateBeforeSync = store.state.value.camera
        val sameCamera = syncedCameraEntity == entity
        val modeChangedByInput = sameCamera &&
            camera.mode != syncedComponentMode &&
            stateBeforeSync.mode == syncedStoreMode
        if (modeChangedByInput) store.dispatch(StudioContract.Intent.SetCameraMode(camera.mode))

        val state = store.state.value.camera
        applyModeOffset(camera, state.mode)
        // Store changes originate in studio's header/menu. Input changes originate in the
        // engine's CameraInputSystem. Keep both paths live without reapplying stale yaw, pitch,
        // or distance over CameraSystem's per-mode gesture state every frame.
        if (needsTargetInitialization || !sameCamera || state.mode != syncedStoreMode) {
            camera.mode = state.mode
        }
        syncedCameraEntity = entity
        syncedStoreMode = state.mode
        syncedComponentMode = camera.mode

        // Projection stays here rather than moving to CameraComponent: it belongs to the
        // rendering Camera, and CameraSystem only owns pose. Dropping it was a regression when
        // CameraPresetMath went away, since that had set both.
        renderingCamera.camera.projection = when (state.projection) {
            StudioContract.Projection.Perspective -> CoreCamera.Projection.Perspective
            StudioContract.Projection.Orthographic -> CoreCamera.Projection.Orthographic
        }
    }

    /**
     * The eye offset each mode actually wants.
     *
     * `CameraComponent` defaults it to a 1.8 eye height, which is a character controller's, and
     * the two modes read it for opposite purposes. The orbit modes aim at `target + offset`, so a
     * non-zero offset put the orbit centre 1.8 units ABOVE the model and framed every scene low.
     * FirstPerson instead PLACES the eye there -- so zeroing it for everything, which is what
     * fixed the orbit framing, dropped the first-person eye exactly onto the orbit target and put
     * the viewer inside the cube it was aiming at.
     */
    private fun applyModeOffset(camera: CameraComponent, mode: CameraMode) {
        if (mode == CameraMode.FirstPerson) {
            camera.offsetPosition.set(0f, FIRST_PERSON_EYE_HEIGHT, 0f)
        } else {
            camera.offsetPosition.set(0f, 0f, 0f)
        }
    }

    /**
     * Adopts the scene document's own camera as the starting orbit.
     *
     * `CameraSystem` recomputes the eye from yaw/pitch/distance every frame, so an authored eye
     * is overwritten on the first frame and the scene is framed by whatever the mode's defaults
     * happen to be -- rotating-cube authors an eye 10 units back and got 4.6, well inside its own
     * ground plane. Converting that eye into the angles the system actually reads makes the first
     * frame match the document, and leaves the camera fully input-driven from there.
     *
     * Clears `needsReset` because setting the mode above raised it, and the reset it asks for is
     * exactly the mode-default pose being replaced here.
     */
    private fun seedFromAuthoredPose(camera: CameraComponent, authored: CoreCamera) {
        val offsetX = authored.eye.x - authored.center.x
        val offsetY = authored.eye.y - authored.center.y
        val offsetZ = authored.eye.z - authored.center.z
        val distance = sqrt(offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ)
        if (distance < MIN_AUTHORED_DISTANCE) return
        // CameraSystem places the eye at `center - forward * distance`, so the authored offset is
        // that forward negated -- see its own forwardFrom for the basis these angles feed.
        camera.pitch = asin((-offsetY / distance).coerceIn(-1f, 1f))
        camera.yaw = atan2(-offsetX, offsetZ)
        camera.maxDistance = maxOf(camera.maxDistance, distance)
        camera.distance = distance
        camera.needsReset = false
    }

    /** Mirrors `RenderSystem.primaryCamera`, but returns the ENTITY: the camera components live
     * on it, and the entity is recreated on every `LoadExample`, so this looks it up fresh each
     * frame rather than caching one. */
    private fun primaryCameraEntity(world: World): Entity? {
        var found: Entity? = null
        world.family<Camera>().forEach { entity, camera ->
            if (found == null && camera.isPrimary) found = entity
        }
        return found
    }
}
