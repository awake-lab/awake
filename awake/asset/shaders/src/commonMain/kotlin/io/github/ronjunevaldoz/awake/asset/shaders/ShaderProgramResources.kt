// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.asset.shaders

data class ShaderStageResource(
    val resourcePath: String,
    val entryPoint: String,
)

data class ShaderProgramResources(
    val vertex: ShaderStageResource,
    val fragment: ShaderStageResource,
) {
    val vertexResourcePath: String
        get() = vertex.resourcePath

    val fragmentResourcePath: String
        get() = fragment.resourcePath

    val vertexEntryPoint: String
        get() = vertex.entryPoint

    val fragmentEntryPoint: String
        get() = fragment.entryPoint
}

data class ShaderSet(
    val vulkan: ShaderProgramResources,
    val webGpu: ShaderProgramResources,
)

fun shaderSet(
    vulkan: ShaderProgramResources,
    webGpu: ShaderProgramResources,
): ShaderSet = ShaderSet(
    vulkan = vulkan,
    webGpu = webGpu,
)

fun shaderSet(
    name: String,
    vulkanDirectory: String = "assets/shader/vulkan",
    webGpuDirectory: String = "assets/shader/webgpu",
): ShaderSet = ShaderSet(
    vulkan = ShaderProgramResources(
        vertex = ShaderStageResource(
            resourcePath = "$vulkanDirectory/$name.vert.spv",
            entryPoint = "vertexMain",
        ),
        fragment = ShaderStageResource(
            resourcePath = "$vulkanDirectory/$name.frag.spv",
            entryPoint = "fragmentMain",
        ),
    ),
    webGpu = ShaderProgramResources(
        vertex = ShaderStageResource(
            resourcePath = "$webGpuDirectory/$name.wgsl",
            entryPoint = "vertexMain",
        ),
        fragment = ShaderStageResource(
            resourcePath = "$webGpuDirectory/$name.wgsl",
            entryPoint = "fragmentMain",
        ),
    ),
)
