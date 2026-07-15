// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.startergame.app

import io.github.ronjunevaldoz.awake.engine.application.gameShaderSet
import io.github.ronjunevaldoz.awake.sample.startergame.scene.starterVertexStride
import io.github.ronjunevaldoz.awake.webgpu.application.WebGpuGameApplication

private val StarterGameShaders = gameShaderSet("triangle")

internal fun createStarterGameWebGpuApplication(): WebGpuGameApplication {
    return WebGpuGameApplication(
        shaderSet = StarterGameShaders,
        vertexStride = starterVertexStride,
        game = starterGame()
    )
}
