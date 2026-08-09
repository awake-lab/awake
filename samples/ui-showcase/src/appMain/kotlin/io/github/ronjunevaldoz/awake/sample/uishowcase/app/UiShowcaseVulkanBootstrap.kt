// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.app

import io.github.ronjunevaldoz.awake.engine.game.AwakeGame
import io.github.ronjunevaldoz.awake.engine.game.gameShaderSet
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.vulkan.application.VulkanGameApplication

private val UiShowcaseShaders = gameShaderSet("triangle")

fun createUiShowcaseVulkanApplication(
    game: AwakeGame = uiShowcase(),
): VulkanGameApplication = VulkanGameApplication(
    shaderSet = UiShowcaseShaders,
    vertexFormat = VertexFormat.PositionColorUv,
    game = game,
)
