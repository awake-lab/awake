// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.app

import io.github.ronjunevaldoz.awake.engine.application.AwakeGame
import io.github.ronjunevaldoz.awake.sample.startergame.scene.starterVertexStride
import io.github.ronjunevaldoz.awake.vulkan.application.VulkanGameApplication

private const val STARTER_GAME_VULKAN_VERTEX_SHADER = "assets/shader/vulkan/triangle.vert.spv"
private const val STARTER_GAME_VULKAN_FRAGMENT_SHADER = "assets/shader/vulkan/triangle.frag.spv"

fun createStarterGameVulkanApplication(
    game: AwakeGame = starterGame()
): VulkanGameApplication {
    return VulkanGameApplication(
        vertexShaderResourcePath = STARTER_GAME_VULKAN_VERTEX_SHADER,
        fragmentShaderResourcePath = STARTER_GAME_VULKAN_FRAGMENT_SHADER,
        vertexStride = starterVertexStride,
        game = game
    )
}
