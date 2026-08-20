// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.app

import io.github.ronjunevaldoz.awake.asset.shaders.shaderSet
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.webgpu.application.WebGpuEngine

private val StudioShaders = shaderSet("triangle")
private val StudioTexturedShaders = shaderSet("textured")
private val StudioInstancedShaders = shaderSet("instanced")
private val StudioSkinnedInstancedShaders = shaderSet("skinned_instanced")
private val StudioSkyboxShaders = shaderSet("skybox")
private val StudioParticleShaders = shaderSet("particle")

fun createStudioWebGpuApplication(): WebGpuEngine = WebGpuEngine(
    shaderSet = StudioShaders,
    vertexFormat = VertexFormat.PositionNormalColor,
    appLifecycle = studioGame(),
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
    // Powers Renderer.showEnvironment; without it that flag draws nothing (no skybox
    // pipeline built).
    skyboxShaderSet = StudioSkyboxShaders,
    // Powers the "Particles" example; without it that example's ParticleEmitter DrawCall is
    // skipped entirely (no particle pipeline registered for its format).
    particleShaderSet = StudioParticleShaders,
)
