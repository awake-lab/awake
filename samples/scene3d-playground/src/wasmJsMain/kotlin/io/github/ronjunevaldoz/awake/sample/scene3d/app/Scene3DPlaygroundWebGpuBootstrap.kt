// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.app

import io.github.ronjunevaldoz.awake.engine.application.gameShaderSet
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.webgpu.application.WebGpuGameApplication

private val Scene3DPlaygroundShaders = gameShaderSet("triangle")

fun createScene3DPlaygroundWebGpuApplication(): WebGpuGameApplication = WebGpuGameApplication(
    shaderSet = Scene3DPlaygroundShaders,
    vertexFormat = VertexFormat.PositionNormalColor,
    game = scene3DPlayground(),
    wireframeSupport = true,
)
