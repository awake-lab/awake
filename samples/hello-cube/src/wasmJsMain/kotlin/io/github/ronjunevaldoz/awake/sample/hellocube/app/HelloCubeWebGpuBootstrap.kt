// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.app

import io.github.ronjunevaldoz.awake.engine.application.gameShaderSet
import io.github.ronjunevaldoz.awake.sample.hellocube.scene.sampleVertexStride
import io.github.ronjunevaldoz.awake.webgpu.application.WebGpuGameApplication

private val HelloCubeShaders = gameShaderSet("triangle")

internal fun createHelloCubeWebGpuApplication(): WebGpuGameApplication {
    return WebGpuGameApplication(
        shaderSet = HelloCubeShaders,
        vertexStride = sampleVertexStride,
        game = helloCubeGame()
    )
}
