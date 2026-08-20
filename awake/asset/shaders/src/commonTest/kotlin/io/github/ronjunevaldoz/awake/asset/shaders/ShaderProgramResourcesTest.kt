package io.github.ronjunevaldoz.awake.asset.shaders

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ShaderProgramResourcesTest {

    @Test
    fun conventionBasedShaderSetMapsTriangleAcrossBackends() {
        val shaders = shaderSet("triangle")

        val vulkanVertex = assertIs<ShaderSource.ResourcePath>(shaders.vulkan[ShaderStage.VERTEX])
        val vulkanFragment = assertIs<ShaderSource.ResourcePath>(shaders.vulkan[ShaderStage.FRAGMENT])
        assertEquals("assets/shader/vulkan/triangle.vert.spv", vulkanVertex.path)
        assertEquals("assets/shader/vulkan/triangle.frag.spv", vulkanFragment.path)
        assertEquals("vertexMain", vulkanVertex.entryPoint)
        assertEquals("fragmentMain", vulkanFragment.entryPoint)

        val webGpuVertex = assertIs<ShaderSource.ResourcePath>(shaders.webGpu[ShaderStage.VERTEX])
        val webGpuFragment = assertIs<ShaderSource.ResourcePath>(shaders.webGpu[ShaderStage.FRAGMENT])
        assertEquals("assets/shader/webgpu/triangle.wgsl", webGpuVertex.path)
        assertEquals("assets/shader/webgpu/triangle.wgsl", webGpuFragment.path)
        assertEquals("vertexMain", webGpuVertex.entryPoint)
        assertEquals("fragmentMain", webGpuFragment.entryPoint)
    }

    @Test
    fun explicitShaderSetKeepsPerBackendEscapeHatch() {
        val shaders = shaderSet(
            vulkan = ShaderStages.graphics(
                vertex = ShaderSource.ResourcePath("assets/shader/vulkan/custom.vert.spv", entryPoint = "appVertex"),
                fragment = ShaderSource.ResourcePath("assets/shader/vulkan/custom.frag.spv", entryPoint = "appFragment"),
            ),
            webGpu = ShaderStages.graphics(
                vertex = ShaderSource.ResourcePath("assets/shader/webgpu/custom.wgsl", entryPoint = "vsMain"),
                fragment = ShaderSource.ResourcePath("assets/shader/webgpu/custom.wgsl", entryPoint = "fsMain"),
            ),
        )

        val vulkanVertex = assertIs<ShaderSource.ResourcePath>(shaders.vulkan[ShaderStage.VERTEX])
        val vulkanFragment = assertIs<ShaderSource.ResourcePath>(shaders.vulkan[ShaderStage.FRAGMENT])
        assertEquals("assets/shader/vulkan/custom.vert.spv", vulkanVertex.path)
        assertEquals("assets/shader/vulkan/custom.frag.spv", vulkanFragment.path)
        assertEquals("appVertex", vulkanVertex.entryPoint)
        assertEquals("appFragment", vulkanFragment.entryPoint)

        val webGpuVertex = assertIs<ShaderSource.ResourcePath>(shaders.webGpu[ShaderStage.VERTEX])
        val webGpuFragment = assertIs<ShaderSource.ResourcePath>(shaders.webGpu[ShaderStage.FRAGMENT])
        assertEquals("assets/shader/webgpu/custom.wgsl", webGpuVertex.path)
        assertEquals("vsMain", webGpuVertex.entryPoint)
        assertEquals("fsMain", webGpuFragment.entryPoint)
    }

    @Test
    fun resourcePathEntryPointExtensionReadsRegardlessOfVariant() {
        val resourcePath = ShaderSource.ResourcePath("x.wgsl", entryPoint = "vsMain")
        val precompiled = ShaderSource.PrecompiledBinary(byteArrayOf(1, 2, 3), entryPoint = "csMain")
        val inline = ShaderSource.InlineText("fn main() {}")

        assertEquals("vsMain", resourcePath.entryPoint)
        assertEquals("csMain", precompiled.entryPoint)
        assertEquals("main", inline.entryPoint)
    }
}
