// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.app

import io.github.ronjunevaldoz.awake.engine.application.gameShaderSet
import io.github.ronjunevaldoz.awake.webgpu.application.WebGpuGameApplication

private val Scene3DPlaygroundShaders = gameShaderSet("triangle")

// position(3) + color/normal(3) + uv(2) -- the "triangle" base shader's fixed vertex layout,
// required by WebGpuGameApplication's 3D pipeline setup at startup.
private val scene3DPlaygroundVertexStride: Int = 8 * Float.SIZE_BYTES

fun createScene3DPlaygroundWebGpuApplication(): WebGpuGameApplication {
    return WebGpuGameApplication(
        shaderSet = Scene3DPlaygroundShaders,
        vertexStride = scene3DPlaygroundVertexStride,
        game = scene3DPlayground()
    )
}
