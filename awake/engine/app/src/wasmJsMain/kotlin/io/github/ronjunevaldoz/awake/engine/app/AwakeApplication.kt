// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.app

import io.github.ronjunevaldoz.awake.engine.application.Game
import io.github.ronjunevaldoz.awake.engine.application.GameShaderSet
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.webgpu.application.WebGpuGameApplication

// additionalPipelines/shadowShaderSet are Vulkan-only (see AwakeApplication's commonMain doc
// comment) -- accepted so this constructor matches the expect signature, silently unused since
// WebGPU has no shadow pre-pass or per-vertex-format pipeline registry to feed them into.
@Suppress("UNUSED_PARAMETER")
actual class AwakeApplication actual constructor(
    shaderSet: GameShaderSet,
    vertexFormat: VertexFormat,
    game: Game,
    additionalPipelines: Map<VertexFormat, GameShaderSet>,
    wireframeSupport: Boolean,
    shadowShaderSet: GameShaderSet?,
) : WebGpuGameApplication(
    shaderSet = shaderSet,
    vertexFormat = vertexFormat,
    game = game,
    wireframeSupport = wireframeSupport,
)
