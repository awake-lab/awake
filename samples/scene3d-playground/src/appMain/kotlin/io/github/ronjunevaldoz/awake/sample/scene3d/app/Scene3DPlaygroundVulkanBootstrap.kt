// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.app

import io.github.ronjunevaldoz.awake.engine.application.AwakeGame
import io.github.ronjunevaldoz.awake.engine.application.gameShaderSet
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

fun createScene3DPlaygroundVulkanApplication(
    game: AwakeGame = scene3DPlayground()
): VulkanGameApplication {
    return VulkanGameApplication(
        shaderSet = Scene3DPlaygroundShaders,
        vertexFormat = VertexFormat.PositionNormalColor,
        game = game,
        additionalPipelines = mapOf(
            VertexFormat.PositionNormalColorSkin to Scene3DPlaygroundSkinnedShaders,
            VertexFormat.PositionNormalColorUv to Scene3DPlaygroundTexturedShaders
        ),
        wireframeSupport = true,
        shadowShaderSet = Scene3DPlaygroundShadowShaders
    )
}
