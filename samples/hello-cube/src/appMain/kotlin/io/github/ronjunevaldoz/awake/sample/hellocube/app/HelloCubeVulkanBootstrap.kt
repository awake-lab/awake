// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.app

import io.github.ronjunevaldoz.awake.engine.application.AwakeGame
import io.github.ronjunevaldoz.awake.sample.hellocube.scene.sampleVertexStride
import io.github.ronjunevaldoz.awake.vulkan.application.VulkanGameApplication

private const val HELLO_CUBE_VULKAN_VERTEX_SHADER = "assets/shader/vulkan/triangle.vert.spv"
private const val HELLO_CUBE_VULKAN_FRAGMENT_SHADER = "assets/shader/vulkan/triangle.frag.spv"

internal fun createHelloCubeVulkanApplication(
    game: AwakeGame = helloCubeGame()
): VulkanGameApplication {
    return VulkanGameApplication(
        vertexShaderResourcePath = HELLO_CUBE_VULKAN_VERTEX_SHADER,
        fragmentShaderResourcePath = HELLO_CUBE_VULKAN_FRAGMENT_SHADER,
        vertexStride = sampleVertexStride,
        game = game
    )
}
