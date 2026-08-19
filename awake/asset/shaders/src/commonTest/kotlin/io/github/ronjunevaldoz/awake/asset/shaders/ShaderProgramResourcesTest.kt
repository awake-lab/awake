package io.github.ronjunevaldoz.awake.asset.shaders

import kotlin.test.Test
import kotlin.test.assertEquals

class ShaderProgramResourcesTest {

    @Test
    fun conventionBasedGameShaderSetMapsTriangleAcrossBackends() {
        val shaders = shaderSet("triangle")

        assertEquals("assets/shader/vulkan/triangle.vert.spv", shaders.vulkan.vertexResourcePath)
        assertEquals("assets/shader/vulkan/triangle.frag.spv", shaders.vulkan.fragmentResourcePath)
        assertEquals("vertexMain", shaders.vulkan.vertexEntryPoint)
        assertEquals("fragmentMain", shaders.vulkan.fragmentEntryPoint)
        assertEquals("assets/shader/webgpu/triangle.wgsl", shaders.webGpu.vertexResourcePath)
        assertEquals("assets/shader/webgpu/triangle.wgsl", shaders.webGpu.fragmentResourcePath)
        assertEquals("vertexMain", shaders.webGpu.vertexEntryPoint)
        assertEquals("fragmentMain", shaders.webGpu.fragmentEntryPoint)
    }

    @Test
    fun explicitGameShaderSetKeepsPerBackendEscapeHatch() {
        val shaders = shaderSet(
            vulkan = ShaderProgramResources(
                vertex = ShaderStageResource(
                    resourcePath = "assets/shader/vulkan/custom.vert.spv",
                    entryPoint = "appVertex",
                ),
                fragment = ShaderStageResource(
                    resourcePath = "assets/shader/vulkan/custom.frag.spv",
                    entryPoint = "appFragment",
                ),
            ),
            webGpu = ShaderProgramResources(
                vertex = ShaderStageResource(
                    resourcePath = "assets/shader/webgpu/custom.wgsl",
                    entryPoint = "vsMain",
                ),
                fragment = ShaderStageResource(
                    resourcePath = "assets/shader/webgpu/custom.wgsl",
                    entryPoint = "fsMain",
                ),
            ),
        )

        assertEquals("assets/shader/vulkan/custom.vert.spv", shaders.vulkan.vertexResourcePath)
        assertEquals("assets/shader/vulkan/custom.frag.spv", shaders.vulkan.fragmentResourcePath)
        assertEquals("appVertex", shaders.vulkan.vertexEntryPoint)
        assertEquals("appFragment", shaders.vulkan.fragmentEntryPoint)
        assertEquals("assets/shader/webgpu/custom.wgsl", shaders.webGpu.vertexResourcePath)
        assertEquals("vsMain", shaders.webGpu.vertexEntryPoint)
        assertEquals("fsMain", shaders.webGpu.fragmentEntryPoint)
    }
}