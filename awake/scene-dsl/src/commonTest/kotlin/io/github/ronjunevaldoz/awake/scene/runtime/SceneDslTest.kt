// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.CameraComponent
import io.github.ronjunevaldoz.awake.scene.components.Name
import io.github.ronjunevaldoz.awake.scene.components.Transform
import io.github.ronjunevaldoz.awake.scene.runtime.dsl.Modifier
import io.github.ronjunevaldoz.awake.scene.runtime.dsl.camera
import io.github.ronjunevaldoz.awake.scene.runtime.dsl.scene
import io.github.ronjunevaldoz.awake.scene.runtime.dsl.transform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SceneDslTest {

    @Test
    fun worldSceneBuildsEntitiesWithNamesAndModifiers() {
        val world = World()

        world.scene {
            entity("camera", Modifier().camera())
            entity("cube", Modifier().transform(y = 2f)) {
                entity("child", Modifier().transform(x = 1f))
            }
        }

        val cameraEntity = world.query(Name::class).first { world.get<Name>(it)?.value == "camera" }
        assertNotNull(world.get<CameraComponent>(cameraEntity))

        val cubeEntity = world.query(Name::class).first { world.get<Name>(it)?.value == "cube" }
        val cubeTransform = world.get<Transform>(cubeEntity)
        assertNotNull(cubeTransform)
        assertEquals(2f, cubeTransform.position.y)

        val childEntity = world.query(Name::class).first { world.get<Name>(it)?.value == "child" }
        val childTransform = world.get<Transform>(childEntity)
        assertNotNull(childTransform)
        assertEquals(1f, childTransform.position.x)
        assertEquals(cubeEntity, childTransform.parent)
    }
}
