// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.app

import io.github.ronjunevaldoz.awake.engine.application.AwakeGame
import io.github.ronjunevaldoz.awake.engine.application.gameShaderSet
import io.github.ronjunevaldoz.awake.sample.uishowcase.scene.uiShowcaseVertexStride
import io.github.ronjunevaldoz.awake.vulkan.application.VulkanGameApplication

private val UiShowcaseShaders = gameShaderSet("triangle")

fun createUiShowcaseVulkanApplication(
    game: AwakeGame = uiShowcase()
): VulkanGameApplication {
    return VulkanGameApplication(
        shaderSet = UiShowcaseShaders,
        vertexStride = uiShowcaseVertexStride,
        game = game
    )
}
