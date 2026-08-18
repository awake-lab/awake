// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.app

import io.github.ronjunevaldoz.awake.engine.game.gameShaderSet
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.webgpu.application.WebGpuGameApplication

private val Scene3DPlaygroundShaders = gameShaderSet("triangle")
private val Scene3DPlaygroundTexturedShaders = gameShaderSet("textured")
private val Scene3DPlaygroundInstancedShaders = gameShaderSet("instanced")
private val Scene3DPlaygroundSkinnedInstancedShaders = gameShaderSet("skinned_instanced")
private val Scene3DPlaygroundSkyboxShaders = gameShaderSet("skybox")

fun createScene3DPlaygroundWebGpuApplication(): WebGpuGameApplication = WebGpuGameApplication(
    shaderSet = Scene3DPlaygroundShaders,
    vertexFormat = VertexFormat.PositionNormalColor,
    game = scene3DPlayground(),
    wireframeSupport = true,
    // Textured only, unlike the Vulkan bootstrap: skinned.wgsl also needs the joint-palette
    // uniform path (DrawCall.extraUniformFloats), which this backend doesn't write yet.
    additionalPipelines = mapOf(VertexFormat.PositionNormalColorUv to Scene3DPlaygroundTexturedShaders),
    // Powers InstancedCubesDemo; without it that demo's single DrawCall is skipped entirely.
    instancedShaderSet = Scene3DPlaygroundInstancedShaders,
    // Powers InstancedSkinnedDemo; without it that demo's single DrawCall is skipped entirely.
    skinnedInstancedShaderSet = Scene3DPlaygroundSkinnedInstancedShaders,
    // Powers Renderer.showEnvironment; without it that flag draws nothing (no skybox
    // pipeline built).
    skyboxShaderSet = Scene3DPlaygroundSkyboxShaders,
)
