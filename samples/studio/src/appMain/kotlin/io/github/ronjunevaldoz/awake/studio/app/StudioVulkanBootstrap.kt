// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.app

import io.github.ronjunevaldoz.awake.engine.application.AwakeGame
import io.github.ronjunevaldoz.awake.engine.application.gameShaderSet
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.vulkan.application.VulkanGameApplication

private val StudioShaders = gameShaderSet("lit_shadow")
private val StudioSkinnedShaders = gameShaderSet("skinned")
private val StudioTexturedShaders = gameShaderSet("textured")
private val StudioShadowShaders = gameShaderSet("shadow_depth")

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
)
