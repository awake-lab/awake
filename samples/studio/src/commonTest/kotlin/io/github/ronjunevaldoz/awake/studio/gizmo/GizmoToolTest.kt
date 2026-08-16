// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.gizmo

import io.github.ronjunevaldoz.awake.core.math.Aabb
import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.ClipSpace
import io.github.ronjunevaldoz.awake.core.math.Vec2
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.ecs.ensure
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private const val WIDTH = 800f
private const val HEIGHT = 600f

/** The tool is what makes the rail real: the same drag has to do three different things. */
class GizmoToolTest {

    private val camera = Camera(
        eye = Vec3(0f, 0f, 6f),
        center = Vec3(0f, 0f, 0f),
        fovYRadians = 1f,
        near = 0.1f,
        far = 100f,
    )
    private val projection = ViewportProjection(
        camera = camera,
        viewProjection = camera.viewProjectionMatrix(WIDTH / HEIGHT, ClipSpace.WebGpu),
        width = WIDTH,
        height = HEIGHT,
    )

    private fun world(): Pair<World, Transform> {
        val world = World()
        val entity = world.create()
        val transform = world.ensure(entity, ::Transform)
        return world to transform
    }

    /** Drag the X handle rightwards and see what each tool made of it. */
    private fun dragX(tool: GizmoTool): Transform {
        val (world, transform) = world()
        val gizmo = StudioGizmo()
        val entityId = 0
        val handleMidpoint = assertNotNull(projection.toScreen(Vec3(0.75f, 0f, 0f)))
        val bounds = { _: Int -> Aabb(Vec3(-0.5f, -0.5f, -0.5f), Vec3(0.5f, 0.5f, 0.5f)) }

        gizmo.update(world, GizmoFrame(projection, entityId, GizmoPointer(handleMidpoint, true), tool), bounds)
        gizmo.update(
            world,
            GizmoFrame(
                projection,
                entityId,
                GizmoPointer(Vec2(handleMidpoint.x + 60f, handleMidpoint.y), true),
                tool,
            ),
            bounds,
        )
        return transform
    }

    @Test
    fun moveTranslatesAlongTheAxis() {
        val transform = dragX(GizmoTool.Move)
        assertTrue(transform.position.x > 0f, "Move must translate, was ${transform.position.x}")
        assertEquals(0f, transform.rotation.y)
        assertEquals(1f, transform.scale.x)
    }

    @Test
    fun rotateTurnsAboutTheAxisWithoutMoving() {
        val transform = dragX(GizmoTool.Rotate)
        assertTrue(transform.rotation.x != 0f, "Rotate must turn about X, was ${transform.rotation.x}")
        assertEquals(0f, transform.position.x, "Rotate must not translate")
    }

    @Test
    fun scaleGrowsTheAxisWithoutMoving() {
        val transform = dragX(GizmoTool.Scale)
        assertTrue(transform.scale.x > 1f, "Scale must grow X, was ${transform.scale.x}")
        assertEquals(0f, transform.position.x, "Scale must not translate")
    }

    /** Select is how you get at an object a handle would otherwise cover, so it must not drag. */
    @Test
    fun selectDrawsNoHandlesAndNeverDrags() {
        val (world, transform) = world()
        val gizmo = StudioGizmo()
        assertTrue(gizmo.handleLines(world, 0, GizmoTool.Select).isEmpty(), "Select draws no handles")

        val transformAfter = dragX(GizmoTool.Select)
        assertEquals(0f, transformAfter.position.x)
        assertEquals(1f, transformAfter.scale.x)
        assertEquals(0f, transform.rotation.x)
    }

    @Test
    fun theOtherToolsEachDrawOneHandlePerAxis() {
        val (world, _) = world()
        val gizmo = StudioGizmo()
        listOf(GizmoTool.Move, GizmoTool.Rotate, GizmoTool.Scale).forEach { tool ->
            assertEquals(3, gizmo.handleLines(world, 0, tool).size, "$tool must draw three handles")
        }
    }
}
