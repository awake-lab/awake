// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.app

import io.github.ronjunevaldoz.awake.engine.application.gameShaderSet
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.webgpu.application.WebGpuGameApplication

private val UiShowcaseShaders = gameShaderSet("triangle")

fun createUiShowcaseWebGpuApplication(): WebGpuGameApplication {
    return WebGpuGameApplication(
        shaderSet = UiShowcaseShaders,
        vertexFormat = VertexFormat.PositionColorUv,
        game = uiShowcase()
    )
}
