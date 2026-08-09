// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.game

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

data class GameShaderSet(
    val vulkan: ShaderProgramResources,
    val webGpu: ShaderProgramResources,
)

fun gameShaderSet(
    vulkan: ShaderProgramResources,
    webGpu: ShaderProgramResources,
): GameShaderSet = GameShaderSet(
    vulkan = vulkan,
    webGpu = webGpu,
)

fun gameShaderSet(
    name: String,
    vulkanDirectory: String = "assets/shader/vulkan",
    webGpuDirectory: String = "assets/shader/webgpu",
): GameShaderSet = GameShaderSet(
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
