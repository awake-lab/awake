// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.app

import io.github.ronjunevaldoz.awake.engine.game.AwakeGame
import io.github.ronjunevaldoz.awake.engine.game.gameShaderSet
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.vulkan.application.VulkanGameApplication

private val StudioShaders = gameShaderSet("lit_shadow")
private val StudioSkinnedShaders = gameShaderSet("skinned")
private val StudioTexturedShaders = gameShaderSet("textured")
private val StudioShadowShaders = gameShaderSet("shadow_depth")
private val StudioInstancedShaders = gameShaderSet("instanced")
private val StudioSkinnedInstancedShaders = gameShaderSet("skinned_instanced")
private val StudioSkyboxShaders = gameShaderSet("skybox")

fun createStudioVulkanApplication(
    game: AwakeGame = studioGame(),
): VulkanGameApplication = VulkanGameApplication(
    shaderSet = StudioShaders,
    vertexFormat = VertexFormat.PositionNormalColor,
    game = game,
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
)
