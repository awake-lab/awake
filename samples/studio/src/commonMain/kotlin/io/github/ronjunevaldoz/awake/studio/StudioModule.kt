// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio

import io.github.ronjunevaldoz.awake.core.input.Input
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.engine.game.GameModule
import io.github.ronjunevaldoz.awake.engine.game.GameWindowBackend
import io.github.ronjunevaldoz.awake.engine.gameauthoring.gameModule
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.asset.shaders.LitShadowUniformLayout
import io.github.ronjunevaldoz.awake.scene.authoring.scene
import io.github.ronjunevaldoz.awake.scene.controls.components.ActiveCamera
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraComponent
import io.github.ronjunevaldoz.awake.scene.controls.systems.CameraInputSystem
import io.github.ronjunevaldoz.awake.scene.controls.systems.CameraSystem
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.core.systems.SpinSystem
import io.github.ronjunevaldoz.awake.scene.rendering.components.Camera
import io.github.ronjunevaldoz.awake.scene.rendering.components.WorldDebugSettings
import io.github.ronjunevaldoz.awake.scene.rendering.systems.ParticleSystem
import io.github.ronjunevaldoz.awake.scene.runtime.defaultInfrastructureSystems
import io.github.ronjunevaldoz.awake.studio.app.platformBackendPreference
import io.github.ronjunevaldoz.awake.studio.app.writeSceneDocument
import io.github.ronjunevaldoz.awake.studio.examples.ExampleLoader
import io.github.ronjunevaldoz.awake.studio.examples.GltfViewerAssets
import io.github.ronjunevaldoz.awake.studio.examples.InstancedSkinnedExampleDriver
import io.github.ronjunevaldoz.awake.studio.examples.ParticleEmitterExampleDriver
import io.github.ronjunevaldoz.awake.studio.examples.SkinnedExampleDriver
import io.github.ronjunevaldoz.awake.studio.examples.StudioExamples
import io.github.ronjunevaldoz.awake.studio.examples.StudioMeshBounds
import io.github.ronjunevaldoz.awake.studio.examples.studioCubeGeometry
import io.github.ronjunevaldoz.awake.studio.examples.studioGroundGeometry
import io.github.ronjunevaldoz.awake.studio.gizmo.StudioGizmo
import io.github.ronjunevaldoz.awake.studio.gizmo.StudioViewportRect
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.studio.state.StudioStore
import io.github.ronjunevaldoz.awake.studio.systems.PlayModeSystem
import io.github.ronjunevaldoz.awake.studio.systems.SpinClockSystem
import io.github.ronjunevaldoz.awake.studio.systems.StudioEditorCamera
import io.github.ronjunevaldoz.awake.studio.systems.StudioExampleDriverSystem
import io.github.ronjunevaldoz.awake.studio.systems.StudioGizmoSystem
import io.github.ronjunevaldoz.awake.studio.systems.gizmoCapturedOwnership
import io.github.ronjunevaldoz.awake.studio.ui.StudioCameraPreview
import io.github.ronjunevaldoz.awake.studio.ui.StudioOrientationGizmo
import io.github.ronjunevaldoz.awake.studio.ui.drawStudioShell
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera

/** `lit_shadow.wgsl`'s Uniforms size -- taken from the shared layout rather than re-summed
 * here, so adding a field to that shader can't leave this call site silently short. */
internal val LIT_SHADOW_UNIFORM_FLOAT_COUNT = LitShadowUniformLayout.total

// Matches the lens every example scene authors (see rotating-cube.scene.json) -- only matters
// for the one frame between onReady creating the editor camera and the first LoadExample's
// seedFromAuthoredPose overwriting its pose/angles from whatever the loaded example actually
// authors.
private const val DEFAULT_EDITOR_CAMERA_FOV_RADIANS = 0.7853982f // 45 degrees
private const val DEFAULT_EDITOR_CAMERA_NEAR = 0.1f
private const val DEFAULT_EDITOR_CAMERA_FAR = 100f

/** An editor's own free-look zoom range is a different concern than a gameplay orbit rig's --
 * see the call site's own comment for why this is wider than [CameraComponent]'s engine default. */
private const val EDITOR_CAMERA_MAX_DISTANCE = 100f

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
    // Owned here, like the gizmo: it holds GPU resources, and the shell's draw functions run
    // every frame and can own nothing that needs destroying.
    val cameraPreview = StudioCameraPreview()
    val orientationGizmo = StudioOrientationGizmo()
    val editorCamera = StudioEditorCamera()
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
                mesh("particle-quad") { ParticleEmitterExampleDriver.createMesh(this) }
                material("particle") { ParticleEmitterExampleDriver.createMaterial(this) }
                material("particle-flicker") { ParticleEmitterExampleDriver.createFlickerMaterial(this) }
                material("particle-levelup") { ParticleEmitterExampleDriver.createLevelupMaterial(this) }
            }
            // The input system writes engine CameraMode hotkeys before the studio bridge syncs
            // them into UI state; CameraSystem then applies the pose after that bridge has
            // handled a header/menu selection for this frame.
            //
            // Direct construction, not the `cameraInputSystem()`/`cameraSystem()` DSL helpers --
            // both need `isCaptured` to also cover a gizmo handle drag, not just real UI widgets,
            // otherwise dragging a handle ALSO orbits the camera underneath it (the same raw
            // pointer drag read twice, with no mutual exclusion). `gizmoCapturedOwnership` is the
            // one place that OR's the two together; everything else about these systems is
            // unchanged from what the DSL helpers themselves construct.
            frameSystem("cameraInput") {
                CameraInputSystem(
                    inputProvider = { requireService(Input::class).currentSnapshot },
                    uiResultProvider = { uiContext.finishFrame().ownership.gizmoCapturedOwnership(gizmo) },
                )
            }
            // Registered here rather than left out: the rotating-cube scene authors a
            // SpinControl that nothing was ticking, so studio's "rotating cube" never rotated.
            // Two systems because SpinControl is a "control carries state, system only composes"
            // component (see SpinSystem): something has to advance the angle, and the engine
            // leaves that to whoever owns the spin rate. In an editor, that owner is play mode.
            frameSystem("spin-clock") { PlayModeSystem(SpinClockSystem(), store) }
            frameSystem("spin") { PlayModeSystem(SpinSystem(), store) }
            // Unconditional (not PlayModeSystem-gated like spin above) -- particles are a
            // passive visual demo, meant to keep animating while orbiting/inspecting in Edit
            // mode too, same reasoning the camera preview/orientation gizmo already animate
            // regardless of mode.
            frameSystem("particles") { ParticleSystem() }
            frameSystem("example-driver") {
                StudioExampleDriverSystem(this, store, loader, writeScene, editorCamera)
            }
            frameSystem("camera") {
                CameraSystem(
                    inputProvider = { requireService(Input::class).currentSnapshot },
                    uiResultProvider = { uiContext.finishFrame().ownership.gizmoCapturedOwnership(gizmo) },
                )
            }
            // Runs after the default infrastructure systems (Transform/Render/Debug), not as a
            // frameSystem -- Renderer.drawDebugLines replaces the whole line buffer each call
            // rather than appending, so whichever system calls it last wins. DebugVisualization
            // System is one of the default infra systems; drawing the gizmo before it (the old
            // frameSystem ordering) meant its handle lines got wiped out any frame a debug
            // toggle (frustum/bounds/occlusion/light/shadow) was on.
            infrastructureSystems {
                defaultInfrastructureSystems() +
                    StudioGizmoSystem(this, store, gizmo, viewportRect, loader::boundsOf)
            }
            onReady {
                GltfViewerAssets.preload()
                SkinnedExampleDriver.preload()
                InstancedSkinnedExampleDriver.preload()
                loader.preload()
                // One entity, created once (not per-example -- ExampleLoader.teardown only
                // destroys instantiated scene roots), so the frustum/bounds toggle survives
                // switching examples instead of resetting to off each time.
                world.create().also { world.add(it, WorldDebugSettings()) }
                // Same "created once, survives every LoadExample" shape -- the editor's own
                // Scene-view camera, distinct from whatever camera a loaded example authors (see
                // StudioEditorCamera's own doc comment). No Name: not a selectable/deletable
                // scene object, same reasoning WorldDebugSettings' entity is invisible in the
                // hierarchy.
                editorCamera.entity = world.create().also { entity ->
                    world.add(
                        entity,
                        Camera(
                            CoreCamera(
                                eye = Vec3(6f, 4f, 8f),
                                center = Vec3.ZERO,
                                fovYRadians = DEFAULT_EDITOR_CAMERA_FOV_RADIANS,
                                near = DEFAULT_EDITOR_CAMERA_NEAR,
                                far = DEFAULT_EDITOR_CAMERA_FAR,
                            ),
                            isPrimary = true,
                        ),
                    )
                    world.add(entity, Transform())
                    world.add(entity, CameraComponent().also {
                        it.targetEntity = entity
                        // An editor's own free-look zoom range is a different concern than a
                        // gameplay orbit rig's -- the engine default (CameraComponent
                        // .DEFAULT_MAX_DISTANCE, 20f) is a character-camera range, not enough to
                        // pull back and see a whole scene laid out. seedFromAuthoredPose only
                        // WIDENS this if a scene's authored eye sits farther out, it never
                        // shrinks it -- so this floor is a permanent editor-camera property, not
                        // overwritten per scene load.
                        it.maxDistance = EDITOR_CAMERA_MAX_DISTANCE
                    })
                    world.add(entity, ActiveCamera())
                }
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
                    cameraPreview,
                    orientationGizmo,
                )
            }
            onDispose {
                cameraPreview.dispose()
                orientationGizmo.dispose()
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
