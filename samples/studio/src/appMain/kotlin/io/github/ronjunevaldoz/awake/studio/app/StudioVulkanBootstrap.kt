// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.app

import io.github.ronjunevaldoz.awake.asset.shaders.shaderSet
import io.github.ronjunevaldoz.awake.engine.app.lifecycle.AwakeAppLifecycle
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.vulkan.application.VulkanEngine

private val StudioShaders = shaderSet("lit_shadow")
private val StudioSkinnedShaders = shaderSet("skinned")
private val StudioTexturedShaders = shaderSet("textured")
private val StudioShadowShaders = shaderSet("shadow_depth")
private val StudioInstancedShaders = shaderSet("instanced")
private val StudioSkinnedInstancedShaders = shaderSet("skinned_instanced")
private val StudioSkyboxShaders = shaderSet("skybox")
private val StudioParticleShaders = shaderSet("particle")

fun createStudioVulkanApplication(
    game: AwakeAppLifecycle = studioApp(),
): VulkanEngine = VulkanEngine(
    shaderSet = StudioShaders,
    vertexFormat = VertexFormat.PositionNormalColor,
    appLifecycle = game,
    additionalPipelines = mapOf(
        VertexFormat.PositionNormalColorSkin to StudioSkinnedShaders,
        VertexFormat.PositionNormalColorUv to StudioTexturedShaders,
    ),
    wireframeSupport = true,
    shadowShaderSet = StudioShadowShaders,
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
