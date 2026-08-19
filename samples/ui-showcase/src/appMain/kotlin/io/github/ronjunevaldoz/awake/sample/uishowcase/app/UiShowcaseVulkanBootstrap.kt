// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.app

import io.github.ronjunevaldoz.awake.asset.shaders.shaderSet
import io.github.ronjunevaldoz.awake.engine.app.lifecycle.AwakeAppLifecycle
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.vulkan.application.VulkanEngine

private val UiShowcaseShaders = shaderSet("triangle")

fun createUiShowcaseVulkanApplication(
    game: AwakeAppLifecycle = uiShowcase(),
): VulkanEngine = VulkanEngine(
    shaderSet = UiShowcaseShaders,
    vertexFormat = VertexFormat.PositionColorUv,
    appLifecycle = game,
)
