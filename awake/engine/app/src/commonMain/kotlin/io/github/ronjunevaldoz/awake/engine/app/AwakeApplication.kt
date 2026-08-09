// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.engine.app

import io.github.ronjunevaldoz.awake.engine.application.Game
import io.github.ronjunevaldoz.awake.engine.application.GameShaderSet
import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat

/**
 * The one public entry point a consumer builds against: picks `VulkanGameApplication`
 * (android/ios/desktop) or `WebGpuGameApplication` (wasmJs) per target, so `commonMain` never
 * imports a backend module directly. See
 * docs/tasks/2026-08-09-application-seam-and-module-naming-plan.md, Part 1.
 *
 * [additionalPipelines] and [shadowShaderSet] are Vulkan-only (WebGPU has no shadow pre-pass or
 * per-vertex-format pipeline registry yet) -- the wasmJs `actual` accepts and ignores them
 * rather than failing a `commonMain` call site that also targets Vulkan.
 */
expect class AwakeApplication(
    shaderSet: GameShaderSet,
    vertexFormat: VertexFormat = VertexFormat.PositionColorUv,
    game: Game,
    additionalPipelines: Map<VertexFormat, GameShaderSet> = emptyMap(),
    wireframeSupport: Boolean = false,
    shadowShaderSet: GameShaderSet? = null,
)
