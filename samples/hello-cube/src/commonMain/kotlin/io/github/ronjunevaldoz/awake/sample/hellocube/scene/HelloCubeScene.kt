// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.scene

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.mesh.gltf.GltfParser
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.ecs.toggle
import io.github.ronjunevaldoz.awake.physics.BoxShape
import io.github.ronjunevaldoz.awake.physics.MotionType
import io.github.ronjunevaldoz.awake.physics.PhysicsWorld
import io.github.ronjunevaldoz.awake.render.mesh.MeshGeometry
import io.github.ronjunevaldoz.awake.scene.components.PhysicsBody
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameSpec
import io.github.ronjunevaldoz.awake.scene.runtime.entities.cameraEntity
import io.github.ronjunevaldoz.awake.scene.runtime.entities.meshEntity
import io.github.ronjunevaldoz.awake.scene.runtime.systems.freeFlyCameraSystem
import io.github.ronjunevaldoz.awake.scene.runtime.systems.playerControlSystem
import io.github.ronjunevaldoz.awake.scene.runtime.sceneGame
import io.github.ronjunevaldoz.awake.scene.systems.PhysicsSystem
import io.github.ronjunevaldoz.awake.sample.hellocube.app.createPhysicsWorld
import io.github.ronjunevaldoz.awake.sample.hellocube.debug.updateHelloCubeHud
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeCameraMode
import io.github.ronjunevaldoz.awake.sample.hellocube.state.HelloCubeRuntimeState

/** Static ground collider: a thin box, top face at world y=0 -- matches [sampleGridGeometry]'s
 * own local y=0 plane so the visual grid sits right at the surface the falling box lands on. */
private val groundHalfExtents = Vec3(5f, 0.05f, 5f)
private val fallingBoxHalfExtents = Vec3(0.5f, 0.5f, 0.5f)

/**
 * Wraps [createPhysicsWorld] + [PhysicsSystem] behind one `System` so the scene-dsl `system {}`
 * factory (called lazily during [io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime.ready],
 * never at scene-spec construction time) is the only place the native [PhysicsWorld] gets
 * constructed -- building it eagerly in [helloCubeSceneSpec] itself would allocate a real
 * jolt-jni world (native temp allocator + worker-thread job system) for every test that calls
 * `helloCubeGame()`/`helloCubeGameSpec()` without ever calling `ready()`.
 */
private class HelloCubePhysicsSystem : System {
    val physicsWorld: PhysicsWorld? = createPhysicsWorld()
    private val delegate = physicsWorld?.let(::PhysicsSystem)

    override fun update(world: World, delta: Float) {
        delegate?.update(world, delta)
    }
}

internal fun helloCubeSceneSpec(state: HelloCubeRuntimeState): SceneGameSpec {
    return sceneGame {
        name("hello-cube")
        cameraEntity(
            "camera",
            transform = { position(6f, 5f, 9f) },
            camera = {
                // Static, framed to see the whole scene at once: the cube at the origin, the
                // 10x10 grid ground plane, and the falling box's full drop arc from (3, 6, 3)
                // down to its resting height near the ground -- the old orbit camera auto-
                // rotated by default (autoRotateSpeed), which made it hard to actually watch
                // the physics demo land.
                eye(6f, 5f, 9f)
                center(1f, 1f, 1f)
                up(0f, 1f, 0f)
                perspective(fovYDegrees = 45f, near = 0.1f, far = 100f)
                primary(true)
            }
        )
        meshEntity(
            name = "cube",
            mesh = "cube",
            material = "default",
            transform = {
                position(0f, 0f, 0f)
                rotation(0f, 0f, 0f)
                scale(1f, 1f, 1f)
            }
        )
        // Phase 8 physics demo (see docs/MVP_PLAN.md): a checkerboard ground plane -- both
        // the visual reference grid and (when a real PhysicsWorld is available, see
        // HelloCubePhysicsSystem) the static collider the falling box lands on -- plus a
        // dynamic box dropped from above it.
        meshEntity(
            name = "ground",
            mesh = "grid",
            material = "default",
            transform = { position(0f, 0f, 0f) }
        )
        meshEntity(
            name = "fallingBox",
            mesh = "cube",
            material = "default",
            transform = { position(3f, 6f, 3f) }
        )
        // Track 2 (glTF end-to-end proof, see docs/MVP_PLAN.md): a mesh sourced from a real
        // GltfParser.parseScene() -> LoadedScene traversal, not a hand-rolled MeshGeometry
        // like every other entity in this scene -- proves the parser's interleaved
        // vertices/indices feed straight into renderer.createMesh() on both backends.
        meshEntity(
            name = "gltfQuad",
            mesh = "gltfQuad",
            material = "default",
            transform = { position(-3f, 0f, -3f) }
        )
        // Real third-party fixture (Khronos glTF-Sample-Assets' Box.glb, see
        // GltfBoxAsset.kt) alongside the synthetic gltfQuad above: proves GltfParser handles
        // an actual exporter's output (explicit node `matrix`, NORMAL attribute, no COLOR_0),
        // not just this codebase's own hand-built GLBs. Position/scale come from the parsed
        // primitive's own localTransform (see toPositionScale()), offset so it doesn't
        // overlap gltfQuad or the origin-anchored cube/grid/falling-box demo.
        run {
            val primitive = GltfParser.parseScene(gltfBoxGlb()).meshes.single().primitives.single()
            val (position, scale) = primitive.localTransform.toPositionScale()
            meshEntity(
                name = "gltfBox",
                mesh = "gltfBox",
                material = "default",
                transform = {
                    position(position.x - 6f, position.y + 0.5f, position.z - 3f)
                    scale(scale.x, scale.y, scale.z)
                }
            )
        }
        assets {
            mesh("cube") {
                renderer.createMesh(sampleCubeGeometry)
            }
            mesh("grid") {
                renderer.createMesh(sampleGridGeometry())
            }
            mesh("gltfQuad") {
                val primitive = GltfParser.parseScene(gltfSampleQuadGlb())
                    .meshes.single().primitives.single()
                renderer.createMesh(MeshGeometry(primitive.vertices, primitive.indices))
            }
            mesh("gltfBox") {
                val primitive = GltfParser.parseScene(gltfBoxGlb()).meshes.single().primitives.single()
                renderer.createMesh(MeshGeometry(primitive.vertices, primitive.indices))
            }
            material("default") {
                renderer.createMaterial()
            }
        }
        val physicsSystem = system("physics") { HelloCubePhysicsSystem() }
        onReady {
            if (system(physicsSystem).physicsWorld != null) {
                world.add(
                    requireEntity("ground"),
                    PhysicsBody(shape = BoxShape(groundHalfExtents), motionType = MotionType.STATIC)
                )
                world.add(
                    requireEntity("fallingBox"),
                    PhysicsBody(shape = BoxShape(fallingBoxHalfExtents), motionType = MotionType.DYNAMIC)
                )
            }
        }
        onDispose { system(physicsSystem).physicsWorld?.destroy() }
        playerControlSystem()
        freeFlyCameraSystem(camera = "camera")

        update { delta, _ ->
            // STATIC mode is the plain resting state: no controller component attached, camera
            // stays exactly where the scene placed it. Only FREE_FLY attaches a controller.
            val cameraEntity = requireEntity("camera")
            val isFreeFly = state.mode == HelloCubeCameraMode.FREE_FLY

            world.toggle<io.github.ronjunevaldoz.awake.scene.components.FreeFlyControl>(cameraEntity, isFreeFly) {
                io.github.ronjunevaldoz.awake.scene.components.FreeFlyControl()
            }

            updateHelloCubeHud(state, delta)
        }
    }
}
