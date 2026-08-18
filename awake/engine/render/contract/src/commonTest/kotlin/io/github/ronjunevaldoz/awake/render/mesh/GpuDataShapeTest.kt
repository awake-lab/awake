// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.mesh

import kotlin.test.Test
import kotlin.test.assertEquals

class GpuDataShapeTest {
    @Test
    fun vec3VertexByteSizeIsUnpadded() {
        assertEquals(12, GpuDataShape.Vec3.vertexByteSize)
    }

    @Test
    fun mat4VertexByteSizeIsSixtyFour() {
        assertEquals(64, GpuDataShape.Mat4.vertexByteSize)
    }
}
