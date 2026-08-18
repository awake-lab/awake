// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.app

import io.github.ronjunevaldoz.awake.engine.game.AwakeGame
import io.github.ronjunevaldoz.awake.engine.game.gameShaderSet
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.vulkan.application.VulkanGameApplication

// "lit_shadow", not "triangle": this Vulkan-only bootstrap uses the shadow-sampling variant
// of the scene lit shader (see samples/scene3d-playground/src/commonMain/shaders/
// lit_shadow.wgsl's own doc comment for why it's a separate file rather than a triangle.wgsl
// edit -- WebGPU's own bootstrap keeps using "triangle" unmodified).
private val Scene3DPlaygroundShaders = gameShaderSet("lit_shadow")
private val Scene3DPlaygroundSkinnedShaders = gameShaderSet("skinned")
private val Scene3DPlaygroundTexturedShaders = gameShaderSet("textured")
private val Scene3DPlaygroundShadowShaders = gameShaderSet("shadow_depth")
private val Scene3DPlaygroundInstancedShaders = gameShaderSet("instanced")
private val Scene3DPlaygroundSkinnedInstancedShaders = gameShaderSet("skinned_instanced")
private val Scene3DPlaygroundSkyboxShaders = gameShaderSet("skybox")

fun createScene3DPlaygroundVulkanApplication(
    game: AwakeGame = scene3DPlayground(),
): VulkanGameApplication = VulkanGameApplication(
    shaderSet = Scene3DPlaygroundShaders,
    vertexFormat = VertexFormat.PositionNormalColor,
    game = game,
    additionalPipelines = mapOf(
        VertexFormat.PositionNormalColorSkin to Scene3DPlaygroundSkinnedShaders,
        VertexFormat.PositionNormalColorUv to Scene3DPlaygroundTexturedShaders,
    ),
    wireframeSupport = true,
    shadowShaderSet = Scene3DPlaygroundShadowShaders,
    // Powers InstancedCubesDemo; without it that demo's single DrawCall is skipped entirely.
    instancedShaderSet = Scene3DPlaygroundInstancedShaders,
    // Powers InstancedSkinnedDemo; without it that demo's single DrawCall is skipped entirely.
    skinnedInstancedShaderSet = Scene3DPlaygroundSkinnedInstancedShaders,
    // Powers Renderer.showEnvironment; without it that flag draws nothing (no skybox
    // pipeline built).
    skyboxShaderSet = Scene3DPlaygroundSkyboxShaders,
)
