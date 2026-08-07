// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.app

import io.github.ronjunevaldoz.awake.engine.application.gameShaderSet
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.webgpu.application.WebGpuGameApplication

private val StudioShaders = gameShaderSet("triangle")

fun createStudioWebGpuApplication(): WebGpuGameApplication = WebGpuGameApplication(
    shaderSet = StudioShaders,
    vertexFormat = VertexFormat.PositionNormalColor,
    game = studioGame(),
    wireframeSupport = true,
)
