// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio

import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.ecs.ensure
import io.github.ronjunevaldoz.awake.engine.game.GameModule
import io.github.ronjunevaldoz.awake.engine.gameauthoring.gameModule
import io.github.ronjunevaldoz.awake.scene.authoring.infrastructure.cameraInputSystem
import io.github.ronjunevaldoz.awake.scene.authoring.infrastructure.cameraSystem
import io.github.ronjunevaldoz.awake.scene.authoring.scene
import io.github.ronjunevaldoz.awake.scene.controls.components.ActiveCamera
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraComponent
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.rendering.components.Camera
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.studio.examples.ExampleLoader
import io.github.ronjunevaldoz.awake.studio.examples.GltfViewerAssets
import io.github.ronjunevaldoz.awake.studio.examples.SkinnedExampleDriver
import io.github.ronjunevaldoz.awake.studio.examples.StudioExamples
import io.github.ronjunevaldoz.awake.studio.examples.studioCubeGeometry
import io.github.ronjunevaldoz.awake.studio.examples.studioGroundGeometry
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import io.github.ronjunevaldoz.awake.studio.ui.drawStudioShell
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera

/** mvp(16) + lightDirection(4) + lightColor(4) + lightMvp(16) + model(16) + cameraPosition(4)
 * + material(4). Must match `lit_shadow.wgsl`'s Uniforms field order. */
internal const val LIT_SHADOW_UNIFORM_FLOAT_COUNT = 64

internal fun studioModule(store: StudioStore = StudioStore()): GameModule {
    val loader = ExampleLoader()
    return gameModule {
        scene("studio") {
            assets {
                mesh("cube") { renderer.createMesh(studioCubeGeometry) }
                mesh("ground") { renderer.createMesh(studioGroundGeometry) }
                material("lit-shadow") { renderer.createMaterial(uniformFloatCount = LIT_SHADOW_UNIFORM_FLOAT_COUNT) }
                mesh("duck") { GltfViewerAssets.createMesh(this) }
                material("duck-material") { GltfViewerAssets.createMaterial(this) }
                mesh("skinned-mesh") { SkinnedExampleDriver.createMesh(this) }
                material("skinned-material") { SkinnedExampleDriver.createMaterial(this) }
            }
            // The input system writes engine CameraMode hotkeys before the studio bridge syncs
            // them into UI state; CameraSystem then applies the pose after that bridge has
            // handled a header/menu selection for this frame.
            cameraInputSystem()
            frameSystem("example-driver") {
                StudioExampleDriverSystem(this, store, loader)
            }
            cameraSystem()
            onReady {
                GltfViewerAssets.preload()
                SkinnedExampleDriver.preload()
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
                    viewportWidth,
                    viewportHeight,
                )
            }
        }
    }
}

private class StudioExampleDriverSystem(
    private val runtime: SceneGameRuntime,
    private val store: StudioStore,
    private val loader: ExampleLoader,
) : System {
    private var syncedCameraEntity: Entity? = null
    private var syncedStoreMode: CameraMode? = null
    private var syncedComponentMode: CameraMode? = null

    override fun update(world: World, delta: Float) {
        store.drainEffects().forEach { effect ->
            when (effect) {
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
        val activeId = store.state.value.examples.activeExampleId
        StudioExamples.first { it.id == activeId }.driver?.invoke(runtime, delta)
        syncCameraComponent(world)
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
        val stateBeforeSync = store.state.value.camera
        val sameCamera = syncedCameraEntity == entity
        val modeChangedByInput = sameCamera &&
            camera.mode != syncedComponentMode &&
            stateBeforeSync.mode == syncedStoreMode
        if (modeChangedByInput) store.dispatch(StudioContract.Intent.SetCameraMode(camera.mode))

        val state = store.state.value.camera
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
