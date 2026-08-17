// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.app

import io.github.ronjunevaldoz.awake.engine.game.gameShaderSet
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.webgpu.application.WebGpuGameApplication

private val StudioShaders = gameShaderSet("triangle")
private val StudioTexturedShaders = gameShaderSet("textured")
private val StudioInstancedShaders = gameShaderSet("instanced")
private val StudioSkinnedInstancedShaders = gameShaderSet("skinned_instanced")

fun createStudioWebGpuApplication(): WebGpuGameApplication = WebGpuGameApplication(
    shaderSet = StudioShaders,
    vertexFormat = VertexFormat.PositionNormalColor,
    game = studioGame(),
    wireframeSupport = true,
    // Textured only, unlike the Vulkan bootstrap: skinned.wgsl also needs the joint-palette
    // uniform path (DrawCall.extraUniformFloats), which this backend doesn't write yet.
    additionalPipelines = mapOf(VertexFormat.PositionNormalColorUv to StudioTexturedShaders),
    // Powers the "Instanced cubes" example; without it that example's single DrawCall is
    // skipped entirely (no instanced pipeline registered for its format).
    instancedShaderSet = StudioInstancedShaders,
    // Powers the "Instanced skinned" example; without it that example's single DrawCall is
    // skipped entirely (no skinned-instanced pipeline registered for its format).
    skinnedInstancedShaderSet = StudioSkinnedInstancedShaders,
)
