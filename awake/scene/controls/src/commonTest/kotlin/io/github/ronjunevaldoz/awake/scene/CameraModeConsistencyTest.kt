// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene

import io.github.ronjunevaldoz.awake.core.input.InputSnapshot
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.ActiveCamera
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.CameraComponent
import io.github.ronjunevaldoz.awake.scene.components.CameraMode
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.systems.CameraSystem
import io.github.ronjunevaldoz.awake.ui.context.UiInputOwnership
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera

/**
 * First-person and third-person used to derive their aim from opposite conventions -- one
 * treated (yaw, pitch) as a view direction, the other as the eye's position on a sphere around
 * the target, which are negatives of each other. Switching modes therefore mirrored the
 * controls both horizontally and vertically. These assert the two now agree.
 */
class CameraModeConsistencyTest {
    @Test
    fun firstAndThirdPersonAgreeOnWhereYawPointsTheView() {
        val firstPerson = viewDirection(CameraMode.FirstPerson, yaw = 0.6f, pitch = 0f)
        val thirdPerson = viewDirection(CameraMode.ThirdPerson, yaw = 0.6f, pitch = 0f)

        assertEquals(firstPerson.x, thirdPerson.x, ABSOLUTE_TOLERANCE)
        assertEquals(firstPerson.z, thirdPerson.z, ABSOLUTE_TOLERANCE)
    }

    @Test
    fun firstAndThirdPersonAgreeOnWherePitchPointsTheView() {
        val firstPerson = viewDirection(CameraMode.FirstPerson, yaw = 0f, pitch = 0.5f)
        val thirdPerson = viewDirection(CameraMode.ThirdPerson, yaw = 0f, pitch = 0.5f)

        assertTrue(firstPerson.y > 0f, "positive pitch should look up, got ${firstPerson.y}")
        assertEquals(firstPerson.y, thirdPerson.y, ABSOLUTE_TOLERANCE)
    }

    @Test
    fun thirdPersonPutsTheEyeBehindTheTargetItIsLookingAt() {
        val world = World()
        val (cameraEntity, _) = spawn(world, CameraMode.ThirdPerson, yaw = 0f, pitch = 0f)
        settle(world)

        val core = world.get(cameraEntity, Camera::class)!!.camera
        // Looking down -Z means the eye sits on the +Z side of what it is aimed at.
        assertTrue(core.eye.z > core.center.z, "eye ${core.eye.z} should be behind center ${core.center.z}")
    }

    @Test
    fun cinematicIgnoresDragSoItsHiddenAnglesCannotDrift() {
        val world = World()
        val (_, config) = spawn(world, CameraMode.Cinematic, yaw = 0f, pitch = 0f)
        config.needsReset = false

        // A held drag across several frames must leave the unused angles exactly alone.
        val system = CameraSystem({ DRAGGING }, { UiInputOwnership() })
        repeat(3) { system.update(world, 0.016f) }

        assertEquals(0f, config.yaw, ABSOLUTE_TOLERANCE)
        assertEquals(0f, config.pitch, ABSOLUTE_TOLERANCE)
    }

    /** Unit-ish view direction (`center - eye`) once the mode's pose has settled. */
    private fun viewDirection(mode: CameraMode, yaw: Float, pitch: Float): Vec3 {
        val world = World()
        val (cameraEntity, _) = spawn(world, mode, yaw, pitch)
        settle(world)

        val core = world.get(cameraEntity, Camera::class)!!.camera
        return (core.center - core.eye).normalize()
    }

    /** Third-person eases its eye in, so run enough long frames for the lerp to converge. */
    private fun settle(world: World) {
        val system = CameraSystem({ IDLE }, { UiInputOwnership() })
        repeat(8) { system.update(world, 1f) }
    }

    private fun spawn(
        world: World,
        mode: CameraMode,
        yaw: Float,
        pitch: Float
    ): Pair<io.github.ronjunevaldoz.awake.ecs.Entity, CameraComponent> {
        val target = world.create()
        world.add(target, Transform())

        val cameraEntity = world.create()
        world.add(
            cameraEntity,
            Camera(
                CoreCamera(
                    eye = Vec3(0f, 0f, 5f),
                    center = Vec3(0f, 0f, 0f),
                    fovYRadians = (45.0 * PI / 180.0).toFloat(),
                    near = 0.1f,
                    far = 100f
                )
            )
        )
        val config = CameraComponent().apply {
            this.mode = mode
            this.targetEntity = target
            this.needsReset = false
            this.yaw = yaw
            this.pitch = pitch
        }
        world.add(cameraEntity, config)
        world.add(cameraEntity, ActiveCamera())
        return cameraEntity to config
    }

    private companion object {
        const val ABSOLUTE_TOLERANCE = 0.001f

        val IDLE = InputSnapshot(
            pointerX = 0f,
            pointerY = 0f,
            pointerDown = false,
            scrollDeltaX = 0f,
            scrollDeltaY = 0f,
            keysDown = emptySet(),
            typedText = "",
            editActions = emptyList()
        )

        val DRAGGING = IDLE.copy(pointerX = 40f, pointerY = 25f, pointerDown = true)
    }
}
