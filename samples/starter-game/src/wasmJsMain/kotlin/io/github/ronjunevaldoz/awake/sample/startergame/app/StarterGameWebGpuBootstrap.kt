// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.app

import io.github.ronjunevaldoz.awake.sample.startergame.scene.starterVertexStride
import io.github.ronjunevaldoz.awake.webgpu.application.WebGpuGameApplication

private const val STARTER_GAME_WEBGPU_SHADER = "assets/shader/webgpu/triangle.wgsl"

internal fun createStarterGameWebGpuApplication(): WebGpuGameApplication {
    return WebGpuGameApplication(
        vertexShaderResourcePath = STARTER_GAME_WEBGPU_SHADER,
        fragmentShaderResourcePath = STARTER_GAME_WEBGPU_SHADER,
        vertexStride = starterVertexStride,
        game = starterGame()
    )
}
