// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.authoring.sugar

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.scene
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraComponent
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode
import io.github.ronjunevaldoz.awake.scene.core.components.Name
import io.github.ronjunevaldoz.awake.scene.rendering.components.Light
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SceneConvenienceSugarTest {

    @Test
    fun defaultOrbitCameraAndSingleDirectionalLightSpawnExpectedComponents() {
        val world = World()

        world.scene {
            val camera = defaultOrbitCamera()
            val light = lighting.singleDirectionalLight(
                direction = Vec3(0f, 1f, 0f),
                color = Vec3(0.9f, 0.9f, 0.9f),
                intensity = 1.5f,
            )

            assertNotNull(camera)
            assertNotNull(light)
        }

        val cameraEntity = world.query(Name::class).first { world.get<Name>(it)?.value == "camera" }
        val cameraComp = world.get<CameraComponent>(cameraEntity)
        assertNotNull(cameraComp)
        assertEquals(CameraMode.ThirdPerson, cameraComp.mode)

        val lightEntity = world.query(Name::class).first { world.get<Name>(it)?.value == "light" }
        val lightComp = world.get<Light>(lightEntity)
        assertNotNull(lightComp)
        assertEquals(Light.Type.Directional, lightComp.type)
        assertEquals(1.5f, lightComp.intensity)
        assertEquals(0f, lightComp.direction.x)
        assertEquals(1f, lightComp.direction.y)
    }
}
