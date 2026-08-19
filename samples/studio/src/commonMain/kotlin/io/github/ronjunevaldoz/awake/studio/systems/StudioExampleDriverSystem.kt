// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.systems

import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.ecs.ensure
import io.github.ronjunevaldoz.awake.scene.controls.components.ActiveCamera
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraComponent
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.rendering.components.Camera
import io.github.ronjunevaldoz.awake.scene.runtime.SceneAppLifecycleRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.SceneLoader
import io.github.ronjunevaldoz.awake.studio.examples.ExampleLoader
import io.github.ronjunevaldoz.awake.studio.examples.StudioExamples
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sqrt
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera

/** Below this the authored eye sits on the target and describes no direction to orbit from. */
private const val MIN_AUTHORED_DISTANCE = 1e-4f

/** Standing eye height above the aim point, so first person starts outside whatever it was
 * looking at rather than inside it. */
private const val FIRST_PERSON_EYE_HEIGHT = 1.8f

internal class StudioExampleDriverSystem(
    private val runtime: SceneAppLifecycleRuntime,
    private val store: StudioStore,
    private val loader: ExampleLoader,
    private val writeScene: (fileName: String, json: String) -> String,
    private val editorCamera: StudioEditorCamera,
) : System {
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

                StudioContract.Effect.AlignViewToCamera -> alignEditorCameraToAuthored(world)
            }
        }
        // Drivers are gameplay: an animation that keeps running in Edit would fight every
        // inspector edit, and its result would be saved as if it had been authored.
        if (store.state.value.mode == StudioContract.Mode.Play) {
            val activeId = store.state.value.examples.activeExampleId
            StudioExamples.first { it.id == activeId }.driver?.invoke(runtime, delta)
        }
        syncPrimaryFlag(world)
        syncCameraComponent(world)
    }

    /**
     * Which camera the viewport actually renders through: the editor camera in Edit mode (a
     * Scene-view, independent of whatever the loaded example authors), the example's own camera
     * in Play mode (a Game view -- showing exactly what the scene defines, untouched by orbit
     * input). Runs every frame, self-healing across `LoadExample`: a freshly loaded example's
     * camera defaults `isPrimary = true` (its scene document authors it that way), and this loop
     * corrects it the very next frame -- no special-case `LoadExample` hook needed.
     *
     * `ActiveCamera`/`CameraComponent` stay exclusively on the editor entity regardless of mode
     * (see [syncCameraComponent]) -- only [Camera.isPrimary] flips, so Play mode's viewport is
     * never hijacked by orbit math applied to the example's own camera.
     */
    private fun syncPrimaryFlag(world: World) {
        val editorEntity = editorCamera.entity ?: return
        val editorIsActive = store.state.value.mode == StudioContract.Mode.Edit
        world.family<Camera>().forEach { entity, camera ->
            camera.isPrimary = if (entity == editorEntity) editorIsActive else !editorIsActive
        }
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
     * The editor camera entity persists across every `LoadExample` (unlike a scene's own
     * authored camera, which is recreated on every load) -- so `ActiveCamera`/`CameraComponent`
     * are ensured against this FIXED entity, not scanned for by `isPrimary` (which now flips
     * between the editor and the example's own camera by [StudioContract.Mode], see
     * [syncPrimaryFlag]). A fresh example's authored pose is adopted by watching the AUTHORED
     * camera entity's own identity change, and re-seeding the editor camera's orbit from it --
     * this is what makes switching examples reframe the Scene view sensibly instead of leaving
     * it staring at wherever the previous example's geometry was.
     */
    private fun syncCameraComponent(world: World) {
        val entity = editorCamera.entity ?: return
        val renderingCamera = world.get<Camera>(entity) ?: return
        world.ensure(entity) { ActiveCamera() }
        val camera = world.ensure(entity) { CameraComponent() }
        val target = world.ensure(entity, ::Transform)
        camera.targetEntity = entity

        val authoredEntity = findAuthoredCameraEntity(world, entity)
        val authoredCameraChanged =
            authoredEntity != null && authoredEntity != editorCamera.authoredCameraEntity
        if (authoredCameraChanged) {
            alignEditorPose(camera, target, world, authoredEntity)
            editorCamera.authoredCameraEntity = authoredEntity
        }

        val stateBeforeSync = store.state.value.camera
        val modeChangedByInput =
            camera.mode != syncedComponentMode && stateBeforeSync.mode == syncedStoreMode
        if (modeChangedByInput) store.dispatch(StudioContract.Intent.SetCameraMode(camera.mode))

        val state = store.state.value.camera
        applyModeOffset(camera, state.mode)
        // Store changes originate in studio's header/menu. Input changes originate in the
        // engine's CameraInputSystem. Keep both paths live without reapplying stale yaw, pitch,
        // or distance over CameraSystem's per-mode gesture state every frame.
        if (authoredCameraChanged || state.mode != syncedStoreMode) {
            camera.mode = state.mode
        }
        syncedStoreMode = state.mode
        syncedComponentMode = camera.mode

        // Projection stays here rather than moving to CameraComponent: it belongs to the
        // rendering Camera, and CameraSystem only owns pose. Dropping it was a regression when
        // CameraPresetMath went away, since that had set both. Always the EDITOR camera's own
        // projection now -- Play mode shows whatever the example itself authored, untouched.
        renderingCamera.camera.projection = when (state.projection) {
            StudioContract.Projection.Perspective -> CoreCamera.Projection.Perspective
            StudioContract.Projection.Orthographic -> CoreCamera.Projection.Orthographic
        }
    }

    /**
     * One-shot: snaps the editor camera's orbit pose to match the scene's own authored camera
     * right now, same math [syncCameraComponent] already runs when a fresh example loads --
     * just triggered on demand ([StudioContract.Intent.AlignViewToCamera]) instead of only on an
     * authored-camera identity change. No-ops silently if the editor entity's own
     * `CameraComponent`/`Transform` don't exist yet (can't happen once the app is running
     * interactively -- `ready()`'s own initial sync pass already creates them) or if the scene
     * authors no camera at all.
     */
    private fun alignEditorCameraToAuthored(world: World) {
        val entity = editorCamera.entity ?: return
        val camera = world.get<CameraComponent>(entity) ?: return
        val target = world.get<Transform>(entity) ?: return
        val authoredEntity = findAuthoredCameraEntity(world, entity) ?: return
        alignEditorPose(camera, target, world, authoredEntity)
    }

    private fun alignEditorPose(
        camera: CameraComponent,
        target: Transform,
        world: World,
        authoredEntity: Entity
    ) {
        world.get<Camera>(authoredEntity)?.camera?.let { authored ->
            target.position.set(authored.center)
            seedFromAuthoredPose(camera, authored)
        }
    }

    /** The scene's own authored camera -- any `Camera` entity that isn't the editor camera.
     * Every example authors exactly one camera today, so "first non-editor entity" is
     * unambiguous in practice; multiple authored cameras per example is out of scope. */
    private fun findAuthoredCameraEntity(world: World, editorEntity: Entity): Entity? {
        var found: Entity? = null
        world.family<Camera>()
            .forEach { entity, _ -> if (found == null && entity != editorEntity) found = entity }
        return found
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
}
