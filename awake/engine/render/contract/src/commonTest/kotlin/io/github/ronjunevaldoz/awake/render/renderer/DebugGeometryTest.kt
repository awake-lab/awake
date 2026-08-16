// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.renderer

import io.github.ronjunevaldoz.awake.core.math.Aabb
import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Frustum
import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.core.math.Vec3
import kotlin.test.Test
import kotlin.test.assertEquals

private val RED = floatArrayOf(1f, 0f, 0f, 1f)

class DebugGeometryTest {
    private fun identityViewCamera() = Camera(
        eye = Vec3(0f, 0f, 0f),
        center = Vec3(0f, 0f, -1f),
        up = Vec3(0f, 1f, 0f),
        fovYRadians = (kotlin.math.PI / 2.0).toFloat(),
        near = 1f,
        far = 10f,
    )

    @Test
    fun frustumDebugLinesHasOneLinePerFrustumEdge() {
        val camera = identityViewCamera()

        val lines = frustumDebugLines(camera, aspect = 1f, RED)

        assertEquals(Frustum.EDGES.size, lines.size)
        val corners = Frustum.corners(camera, aspect = 1f)
        val (a, b) = Frustum.EDGES.first()
        assertEquals(LineSegment(corners[a], corners[b], RED), lines.first())
    }

    @Test
    fun boundsDebugLinesHasOneLinePerBoxEdgeInWorldSpace() {
        val bounds = Aabb(Vec3(-1f, -1f, -1f), Vec3(1f, 1f, 1f))
        val worldMatrix = Mat4().translate(5f, 0f, 0f)

        val lines = boundsDebugLines(bounds, worldMatrix, RED)

        assertEquals(Aabb.EDGES.size, lines.size)
        // Every line endpoint is the translated box -- x in [4, 6], not the original [-1, 1].
        for (line in lines) {
            assertEquals(true, line.start.x in 4f..6f)
            assertEquals(true, line.end.x in 4f..6f)
        }
    }
}
