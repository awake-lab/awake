// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.app

import io.github.ronjunevaldoz.awake.engine.game.gameShaderSet
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.webgpu.application.WebGpuGameApplication

private val StudioShaders = gameShaderSet("triangle")
private val StudioTexturedShaders = gameShaderSet("textured")

fun createStudioWebGpuApplication(): WebGpuGameApplication = WebGpuGameApplication(
    shaderSet = StudioShaders,
    vertexFormat = VertexFormat.PositionNormalColor,
    game = studioGame(),
    wireframeSupport = true,
    // Textured only, unlike the Vulkan bootstrap: skinned.wgsl also needs the joint-palette
    // uniform path (DrawCall.extraUniformFloats), which this backend doesn't write yet.
    additionalPipelines = mapOf(VertexFormat.PositionNormalColorUv to StudioTexturedShaders),
)
