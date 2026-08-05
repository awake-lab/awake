// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene

import io.github.ronjunevaldoz.awake.core.math.OrbitCameraController
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.controls.OrbitCameraDemoRig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** [OrbitCameraDemoRig] just bundles [io.github.ronjunevaldoz.awake.scene.controls
 * .PrimaryOrbitCamera] with a [Transform]-only placement entity -- these tests check the
 * combined lifecycle (both entities appear together, both disappear together, spawn is
 * idempotent), not orbit math itself (already covered by [OrbitCameraController]'s own tests). */
class OrbitCameraDemoRigTest {
    private val origin = Vec3(0f, 0f, 0f)

    @Test
    fun spawnCreatesOnePlacementEntityAndOnePrimaryCameraEntity() {
        val world = World()
        val rig = OrbitCameraDemoRig(OrbitCameraController())

        rig.spawn(world, Vec3(1f, 2f, 3f))

        val cameraEntities = world.query<Camera>()
        assertEquals(1, world.query<Transform>().size)
        assertEquals(1, cameraEntities.size)
        assertTrue(world.get(cameraEntities.single(), Camera::class)!!.isPrimary)
    }

    @Test
    fun spawnIsIdempotent() {
        val world = World()
        val rig = OrbitCameraDemoRig(OrbitCameraController())

        rig.spawn(world, origin)
        rig.spawn(world, origin)

        assertEquals(1, world.query<Transform>().size)
        assertEquals(1, world.query<Camera>().size)
    }

    @Test
    fun destroyRemovesBothEntities() {
        val world = World()
        val rig = OrbitCameraDemoRig(OrbitCameraController())
        rig.spawn(world, origin)

        rig.destroy(world)

        assertTrue(world.query<Transform>().isEmpty())
        assertTrue(world.query<Camera>().isEmpty())
    }

    @Test
    fun refreshBeforeSpawnDoesNotThrow() {
        val world = World()
        val rig = OrbitCameraDemoRig(OrbitCameraController())

        rig.refresh(world, origin)

        assertTrue(world.query<Camera>().isEmpty())
    }
}
