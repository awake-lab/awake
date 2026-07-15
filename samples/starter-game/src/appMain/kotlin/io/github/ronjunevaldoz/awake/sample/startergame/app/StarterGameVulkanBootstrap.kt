// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.app

import io.github.ronjunevaldoz.awake.engine.application.AwakeGame
import io.github.ronjunevaldoz.awake.engine.application.gameShaderSet
import io.github.ronjunevaldoz.awake.sample.startergame.scene.starterVertexStride
import io.github.ronjunevaldoz.awake.vulkan.application.VulkanGameApplication

private val StarterGameShaders = gameShaderSet("triangle")

fun createStarterGameVulkanApplication(
    game: AwakeGame = starterGame()
): VulkanGameApplication {
    return VulkanGameApplication(
        shaderSet = StarterGameShaders,
        vertexStride = starterVertexStride,
        game = game
    )
}
