// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.app

import io.github.ronjunevaldoz.awake.asset.shaders.ShaderSet
import io.github.ronjunevaldoz.awake.engine.app.lifecycle.AppLifecycle
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.vulkan.application.VulkanEngine

actual class AwakeApplication actual constructor(
    shaderSet: ShaderSet,
    vertexFormat: VertexFormat,
    appLifecycle: AppLifecycle,
    additionalPipelines: Map<VertexFormat, ShaderSet>,
    wireframeSupport: Boolean,
    shadowShaderSet: ShaderSet?,
) : VulkanEngine(
    shaderSet = shaderSet,
    vertexFormat = vertexFormat,
    appLifecycle = appLifecycle,
    additionalPipelines = additionalPipelines,
    wireframeSupport = wireframeSupport,
    shadowShaderSet = shadowShaderSet,
)
