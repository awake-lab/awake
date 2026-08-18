// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.renderer

import io.github.ronjunevaldoz.awake.render.mesh.GpuDataShape
import kotlin.test.Test
import kotlin.test.assertEquals

class UniformLayoutTest {
    @Test
    fun vec3UniformFloatsIsPadded() {
        assertEquals(4, GpuDataShape.Vec3.uniformFloats)
    }

    @Test
    fun mat4UniformFloatsIsSixteen() {
        assertEquals(16, GpuDataShape.Mat4.uniformFloats)
    }

    @Test
    fun vec4UniformFloatsIsFour() {
        assertEquals(4, GpuDataShape.Vec4.uniformFloats)
    }

    @Test
    fun layoutTotalSumsEachFieldsUniformFloats() {
        val layout = UniformLayout(
            UniformField("a", GpuDataShape.Mat4),
            UniformField("b", GpuDataShape.Vec3),
            UniformField("c", GpuDataShape.Float),
        )

        assertEquals(21, layout.total)
    }

    @Test
    fun offsetOfAccumulatesPrecedingFields() {
        val second = UniformField("b", GpuDataShape.Vec3)
        val layout = UniformLayout(UniformField("a", GpuDataShape.Mat4), second)

        assertEquals(16, layout.offsetOf(second))
    }
}
