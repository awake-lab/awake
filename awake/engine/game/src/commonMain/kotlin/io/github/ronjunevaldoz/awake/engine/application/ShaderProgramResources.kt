// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.application

data class ShaderProgramResources(
    val vertexResourcePath: String,
    val fragmentResourcePath: String
)

data class GameShaderSet(
    val vulkan: ShaderProgramResources,
    val webGpu: ShaderProgramResources
)

fun gameShaderSet(
    vulkan: ShaderProgramResources,
    webGpu: ShaderProgramResources
): GameShaderSet = GameShaderSet(
    vulkan = vulkan,
    webGpu = webGpu
)

fun gameShaderSet(
    name: String,
    vulkanDirectory: String = "assets/shader/vulkan",
    webGpuDirectory: String = "assets/shader/webgpu"
): GameShaderSet {
    return GameShaderSet(
        vulkan = ShaderProgramResources(
            vertexResourcePath = "$vulkanDirectory/$name.vert.spv",
            fragmentResourcePath = "$vulkanDirectory/$name.frag.spv"
        ),
        webGpu = ShaderProgramResources(
            vertexResourcePath = "$webGpuDirectory/$name.wgsl",
            fragmentResourcePath = "$webGpuDirectory/$name.wgsl"
        )
    )
}
