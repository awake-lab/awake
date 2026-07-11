// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.systems.CameraFollowSystem
import kotlin.test.Test
import kotlin.test.assertEquals

class CameraFollowSystemTest {
    @Test
    fun cameraTracksPlayerPositionWithFixedOffset() {
        val world = World()
        val playerTransform = Transform(position = Vec3(2f, 0f, 4f))
        val coreCamera = CoreCamera(
            eye = Vec3(0f, 0f, 0f),
            center = Vec3(0f, 0f, 0f),
            fovYRadians = 1f,
            near = 0.1f,
            far = 100f
        )
        val cameraComponent = Camera(camera = coreCamera)
        val system = CameraFollowSystem(
            playerTransform,
            cameraComponent,
            offset = Vec3(0f, 3f, 6f)
        )

        system.update(world, 0f)

        assertEquals(2f, coreCamera.eye.x)
        assertEquals(3f, coreCamera.eye.y)
        assertEquals(10f, coreCamera.eye.z)
        assertEquals(2f, coreCamera.center.x)
        assertEquals(0f, coreCamera.center.y)
        assertEquals(4f, coreCamera.center.z)

        // Camera keeps tracking after the player moves.
        playerTransform.position.x = 5f
        system.update(world, 0f)
        assertEquals(5f, coreCamera.eye.x)
        assertEquals(5f, coreCamera.center.x)
    }
}
