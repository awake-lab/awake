// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

import kotlin.test.Test
import kotlin.test.assertEquals

class ShaderProgramResourcesTest {

    @Test
    fun conventionBasedGameShaderSetMapsTriangleAcrossBackends() {
        val shaders = gameShaderSet("triangle")

        assertEquals("assets/shader/vulkan/triangle.vert.spv", shaders.vulkan.vertexResourcePath)
        assertEquals("assets/shader/vulkan/triangle.frag.spv", shaders.vulkan.fragmentResourcePath)
        assertEquals("assets/shader/webgpu/triangle.wgsl", shaders.webGpu.vertexResourcePath)
        assertEquals("assets/shader/webgpu/triangle.wgsl", shaders.webGpu.fragmentResourcePath)
    }

    @Test
    fun explicitGameShaderSetKeepsPerBackendEscapeHatch() {
        val shaders = gameShaderSet(
            vulkan = ShaderProgramResources(
                vertexResourcePath = "assets/shader/vulkan/custom.vert.spv",
                fragmentResourcePath = "assets/shader/vulkan/custom.frag.spv"
            ),
            webGpu = ShaderProgramResources(
                vertexResourcePath = "assets/shader/webgpu/custom.wgsl",
                fragmentResourcePath = "assets/shader/webgpu/custom.wgsl"
            )
        )

        assertEquals("assets/shader/vulkan/custom.vert.spv", shaders.vulkan.vertexResourcePath)
        assertEquals("assets/shader/vulkan/custom.frag.spv", shaders.vulkan.fragmentResourcePath)
        assertEquals("assets/shader/webgpu/custom.wgsl", shaders.webGpu.vertexResourcePath)
    }
}
