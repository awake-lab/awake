// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.app

import io.github.ronjunevaldoz.awake.engine.app.lifecycle.AppLifecycle
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.webgpu.application.WebGpuEngine

// additionalPipelines/shadowShaderSet are Vulkan-only (see AwakeApplication's commonMain doc
// comment) -- accepted so this constructor matches the expect signature, silently unused since
// WebGPU has no shadow pre-pass or per-vertex-format pipeline registry to feed them into.
@Suppress("UNUSED_PARAMETER")
actual class AwakeApplication actual constructor(
    shaderSet: ShaderSet,
    vertexFormat: VertexFormat,
    appLifecycle: AppLifecycle,
    additionalPipelines: Map<VertexFormat, ShaderSet>,
    wireframeSupport: Boolean,
    shadowShaderSet: ShaderSet?,
) : WebGpuEngine(
    shaderSet = shaderSet,
    vertexFormat = vertexFormat,
    appLifecycle = appLifecycle,
    wireframeSupport = wireframeSupport,
)
