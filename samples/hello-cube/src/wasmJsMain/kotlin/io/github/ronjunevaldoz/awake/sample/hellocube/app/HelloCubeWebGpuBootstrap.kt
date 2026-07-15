// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.app

import io.github.ronjunevaldoz.awake.sample.hellocube.scene.sampleVertexStride
import io.github.ronjunevaldoz.awake.webgpu.application.WebGpuGameApplication

private const val HELLO_CUBE_WEBGPU_SHADER = "assets/shader/webgpu/triangle.wgsl"

internal fun createHelloCubeWebGpuApplication(): WebGpuGameApplication {
    return WebGpuGameApplication(
        vertexShaderResourcePath = HELLO_CUBE_WEBGPU_SHADER,
        fragmentShaderResourcePath = HELLO_CUBE_WEBGPU_SHADER,
        vertexStride = sampleVertexStride,
        game = helloCubeGame()
    )
}
