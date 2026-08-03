// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.app

import io.github.ronjunevaldoz.awake.engine.application.AwakeGame
import io.github.ronjunevaldoz.awake.engine.application.gameShaderSet
import io.github.ronjunevaldoz.awake.vulkan.application.VulkanGameApplication

private val Scene3DPlaygroundShaders = gameShaderSet("triangle")

// position(3) + color/normal(3) + uv(2) -- the "triangle" base shader's fixed vertex layout,
// required by VulkanGameApplication's 3D pipeline setup at startup.
private val scene3DPlaygroundVertexStride: Int = 8 * Float.SIZE_BYTES

fun createScene3DPlaygroundVulkanApplication(
    game: AwakeGame = scene3DPlayground()
): VulkanGameApplication {
    return VulkanGameApplication(
        shaderSet = Scene3DPlaygroundShaders,
        vertexStride = scene3DPlaygroundVertexStride,
        game = game
    )
}
