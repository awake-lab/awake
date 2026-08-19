// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.pipeline

import io.github.ronjunevaldoz.awake.render.mesh.VertexFormat
import io.github.ronjunevaldoz.awake.vulkan.texture.ShadowMap

/** Every 3D [RenderPipeline] a [io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer] can draw
 * with -- replaces that class's own `renderPipeline`/`additionalPipelinesByFormat`/
 * `wireframePipelinesByFormat`/`instancedPipelinesByFormat`/`skinnedInstancedPipelinesByFormat`
 * constructor params (5 -> 1). [byFormat] deliberately does NOT include [primary] -- the
 * renderer merges that itself (`mapOf(primary.vertexFormat to primary) + byFormat`), the same
 * single-source-of-truth shape its own `pipelinesByFormat` computed val always had, so a caller
 * can never forget the merge and silently drop the primary pipeline from format lookups. */
class PipelineTable(
    val primary: RenderPipeline,
    val byFormat: Map<VertexFormat, RenderPipeline> = emptyMap(),
    val wireframeByFormat: Map<VertexFormat, RenderPipeline> = emptyMap(),
    val instancedByFormat: Map<VertexFormat, RenderPipeline> = emptyMap(),
    val skinnedInstancedByFormat: Map<VertexFormat, RenderPipeline> = emptyMap(),
)

/** The 4 UI shader pairs every [io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer] loads --
 * replaces that class's own `uiShaders`/`uiGlyphShaders`/`uiTextureShaders`/
 * `uiRoundedQuadShaders` constructor params (4 -> 1). */
data class UiShaderPairs(
    val quad: ShaderPair,
    val glyph: ShaderPair,
    val texture: ShaderPair,
    val roundedQuad: ShaderPair,
)

/** [map] and [pipeline] are always built together, never independently (see
 * `VulkanGameApplication`'s own `shadowShaderSet` doc comment) -- bundling them replaces
 * `Renderer`'s separate `shadowMap`/`shadowRenderPipeline` nullable params (2 -> 1) and removes
 * the invalid "one set, one null" state two independent nullable params silently allowed. */
class ShadowResources(val map: ShadowMap, val pipeline: ShadowRenderPipeline)
