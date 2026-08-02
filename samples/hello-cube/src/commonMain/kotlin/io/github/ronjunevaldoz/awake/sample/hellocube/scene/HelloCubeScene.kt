// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.scene

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.ecs.toggle
import io.github.ronjunevaldoz.awake.physics.BoxShape
import io.github.ronjunevaldoz.awake.physics.MotionType
import io.github.ronjunevaldoz.awake.physics.PhysicsWorld
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
        assets {
            mesh("cube") {
                renderer.createMesh(sampleCubeGeometry)
            }
            mesh("grid") {
                renderer.createMesh(sampleGridGeometry())
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
