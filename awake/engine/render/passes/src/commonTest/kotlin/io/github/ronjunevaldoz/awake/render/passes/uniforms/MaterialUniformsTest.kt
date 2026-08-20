// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.passes.uniforms

import io.github.ronjunevaldoz.awake.core.math.Mat4
import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.render.renderer.DrawCall
import kotlin.test.Test
import kotlin.test.assertEquals

class MaterialUniformsTest {

    private class FakeMesh : Mesh {
        override val format: VertexFormat = VertexFormat.PositionColorUv
        override fun bind(commandBuffer: Long) = Unit
        override fun draw(commandBuffer: Long) = Unit
        override fun destroy() = Unit
    }

    private class FakeMaterial : Material {
        override fun updateUniformBuffer(mvp: FloatArray) = Unit
        override fun bind(commandBuffer: Long, pipelineLayout: Long) = Unit
        override fun destroy() = Unit
    }

    @Test
    fun testPbrMaterialFloatsDefault() {
        val drawCall = DrawCall(
            mesh = FakeMesh(),
            material = FakeMaterial(),
            model = Mat4(),
            extraUniformFloats = floatArrayOf(),
        )
        val floats = pbrMaterialFloats(drawCall)
        assertEquals(4, floats.size)
        assertEquals(0f, floats[0])
        assertEquals(0.5f, floats[1])
    }

    @Test
    fun testPbrTexturedMaterialFloatsSupplied() {
        val custom = FloatArray(12) { it.toFloat() }
        val drawCall = DrawCall(
            mesh = FakeMesh(),
            material = FakeMaterial(),
            model = Mat4(),
            extraUniformFloats = custom,
        )
        val floats = pbrTexturedMaterialFloats(drawCall)
        assertEquals(12, floats.size)
        assertEquals(0f, floats[0])
        assertEquals(11f, floats[11])
    }

    @Test
    fun testFogUniformFloats() {
        val fog = fogUniformFloats(floatArrayOf(0.5f, 0.6f, 0.7f), 0.05f)
        assertEquals(4, fog.size)
        assertEquals(0.5f, fog[0])
        assertEquals(0.6f, fog[1])
        assertEquals(0.7f, fog[2])
        assertEquals(0.05f, fog[3])
    }

    @Test
    fun testMaterialUniformLayouts() {
        assertEquals(4, PBR_MATERIAL_FLOATS)
        assertEquals(12, PBR_TEXTURED_MATERIAL_FLOATS)

        // Lit: MVP(16) + lightDir(4) + lightColor(4) + pbr(4) = 28 floats
        assertEquals(28, MaterialUniformLayouts.Lit.total)

        // PbrTextured: MVP(16) + lightDir(4) + lightColor(4) + model(16) + camPos(4) + pbr(4) + baseColor(4) + emissive(4) + fog(4) = 60 floats
        assertEquals(60, MaterialUniformLayouts.PbrTextured.total)
    }
}
