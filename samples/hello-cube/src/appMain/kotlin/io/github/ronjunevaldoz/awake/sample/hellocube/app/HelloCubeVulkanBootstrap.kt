// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.app

import io.github.ronjunevaldoz.awake.engine.application.AwakeGame
import io.github.ronjunevaldoz.awake.engine.application.gameShaderSet
import io.github.ronjunevaldoz.awake.sample.hellocube.scene.sampleVertexStride
import io.github.ronjunevaldoz.awake.vulkan.application.VulkanGameApplication

private val HelloCubeShaders = gameShaderSet("triangle")

internal fun createHelloCubeVulkanApplication(
    game: AwakeGame = helloCubeGame()
): VulkanGameApplication {
    return VulkanGameApplication(
        shaderSet = HelloCubeShaders,
        vertexStride = sampleVertexStride,
        game = game
    )
}
