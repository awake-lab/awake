// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.mesh

import kotlin.test.Test
import kotlin.test.assertEquals

class VertexFormatTest {

    @Test
    fun positionColorUvMatchesTheLayoutEveryBackendPreviouslyHardcoded() {
        val format = VertexFormat.PositionColorUv

        assertEquals(32, format.strideBytes)
        assertEquals(listOf(0, 12, 24), format.entries.map { it.offsetBytes })
        assertEquals(
            listOf(VertexSemantic.Position, VertexSemantic.Color, VertexSemantic.Uv),
            format.entries.map { it.attribute.semantic },
        )
    }

    @Test
    fun offsetsAccumulateInDeclarationOrder() {
        val format = VertexFormat(
            listOf(
                VertexAttribute(VertexSemantic.Position, GpuDataShape.Vec3, location = 0),
                VertexAttribute(VertexSemantic.Normal, GpuDataShape.Vec3, location = 1),
                VertexAttribute(VertexSemantic.JointIndices, GpuDataShape.UInt4, location = 2),
                VertexAttribute(VertexSemantic.JointWeights, GpuDataShape.Vec4, location = 3),
            ),
        )

        assertEquals(listOf(0, 12, 24, 40), format.entries.map { it.offsetBytes })
        assertEquals(56, format.strideBytes)
    }
}
