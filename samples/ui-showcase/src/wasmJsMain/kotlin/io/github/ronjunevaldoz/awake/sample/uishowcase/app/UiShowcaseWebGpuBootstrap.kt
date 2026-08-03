// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.app

import io.github.ronjunevaldoz.awake.engine.application.gameShaderSet
import io.github.ronjunevaldoz.awake.webgpu.application.WebGpuGameApplication

private val UiShowcaseShaders = gameShaderSet("triangle")

// position(3) + color/normal(3) + uv(2) -- the "triangle" base shader's fixed vertex layout,
// required by WebGpuGameApplication's 3D pipeline setup even though this sample no longer
// renders any 3D content itself (see app/GameModule.kt's comment for why).
private val uiShowcaseVertexStride: Int = 8 * Float.SIZE_BYTES

fun createUiShowcaseWebGpuApplication(): WebGpuGameApplication {
    return WebGpuGameApplication(
        shaderSet = UiShowcaseShaders,
        vertexStride = uiShowcaseVertexStride,
        game = uiShowcase()
    )
}
