// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.app

import io.github.ronjunevaldoz.awake.engine.game.Game
import io.github.ronjunevaldoz.awake.engine.game.GameShaderSet
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.vulkan.application.VulkanGameApplication

actual class AwakeApplication actual constructor(
    shaderSet: GameShaderSet,
    vertexFormat: VertexFormat,
    game: Game,
    additionalPipelines: Map<VertexFormat, GameShaderSet>,
    wireframeSupport: Boolean,
    shadowShaderSet: GameShaderSet?,
) : VulkanGameApplication(
    shaderSet = shaderSet,
    vertexFormat = vertexFormat,
    game = game,
    additionalPipelines = additionalPipelines,
    wireframeSupport = wireframeSupport,
    shadowShaderSet = shadowShaderSet,
)
