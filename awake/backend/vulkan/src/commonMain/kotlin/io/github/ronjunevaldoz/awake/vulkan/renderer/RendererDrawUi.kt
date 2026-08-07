// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.vulkan.renderer

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.render.texture.RenderTarget
import io.github.ronjunevaldoz.awake.ui.UiColoredTriangleMesh
import io.github.ronjunevaldoz.awake.ui.UiColoredVertex
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPath
import io.github.ronjunevaldoz.awake.ui.UiPoint
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec.RoundedRectangle
import io.github.ronjunevaldoz.awake.ui.UiTexturedTriangleMesh
import io.github.ronjunevaldoz.awake.ui.UiTexturedVertex
import io.github.ronjunevaldoz.awake.ui.UiTriangleMesh
import io.github.ronjunevaldoz.awake.ui.bounds
import io.github.ronjunevaldoz.awake.ui.clipToConvexPaths
import io.github.ronjunevaldoz.awake.ui.convexClipContour
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layout.contains
import io.github.ronjunevaldoz.awake.ui.layout.intersect
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.tessellateFillAa
import io.github.ronjunevaldoz.awake.ui.tessellateStroke
import io.github.ronjunevaldoz.awake.ui.toPath
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.vulkan.Vulkan
import io.github.ronjunevaldoz.awake.vulkan.enums.VkSubpassContents
import io.github.ronjunevaldoz.awake.vulkan.models.VkExtent2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkOffset2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkRect2D
import io.github.ronjunevaldoz.awake.vulkan.models.VkViewport
import io.github.ronjunevaldoz.awake.vulkan.models.info.VkRenderPassBeginInfo
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer.UiRun.ClipRun
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer.UiRun.GlyphRun
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer.UiRun.QuadRun
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer.UiRun.RoundedQuadRun
import io.github.ronjunevaldoz.awake.vulkan.renderer.Renderer.UiRun.TextureRun
import io.github.ronjunevaldoz.awake.vulkan.texture.OffscreenRenderTarget
import io.github.ronjunevaldoz.awake.vulkan.ui.DynamicMesh

/** UI overlay primitive staging -- [performDrawUi] walks a frame's
 * [io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive] list once, coalescing same-type runs
 * into pooled meshes, and every `stage*Run`/mesh-building helper it calls. See [Renderer]'s
 * class doc comment for why this lives here as `internal` extension functions rather than as
 * members. */

/** Not a real draw call -- clip kind bookkeeping for [performDrawUi]'s
 * `ClipPathPush`/`ClipPush`/`ClipPop` handling (whether to also pop [UiPath]-based exact-clip
 * state alongside the rect-scissor stack). File-private: only [performDrawUi] needs it. */
private enum class ClipKind {
    Rect,
    Path,
}

/** Stages this frame's UI overlay content -- rewrites [Renderer.uiRuns]' pooled meshes but
 * issues no GPU commands itself. Must be called BEFORE [performDraw] (see `VulkanGameApplication
 * .onRender()`'s ordering) so [recordCommandBuffer]'s UI pass, appended to the SAME
 * command buffer as the 3D pass inside that same [performDraw] call, draws this frame's
 * widgets rather than lagging a frame behind. Lazily builds the (quad) UI pipeline on
 * first call, and the glyph pipeline on the first call that passes a non-null [font] --
 * see [ensureUiQuadPipeline]/[ensureGlyphPipeline]'s doc comments.
 *
 * Walks [primitives] once, coalescing adjacent same-type entries into runs (rather than
 * partitioning the whole list by type up front) so [recordCommandBuffer] can issue draw
 * calls in the SAME order [primitives] arrived in -- painter's-algorithm order across
 * types, not just within one type. See this class's file-level bug-fix note for why
 * "all quads, then all glyphs, then all textures" broke overlay-on-top-of-sibling
 * ordering (e.g. a dropdown's overlay quad, emitted via `emitOverlay()`, must draw
 * after a sibling button's OWN label glyph if it comes later in [primitives], but a
 * fixed per-type pass order always drew every glyph after every quad regardless).
 *
 * Named `performDrawUi`, not `drawUi` -- see [performDraw]'s doc comment for why. */
internal fun Renderer.performDrawUi(primitives: List<UiDrawPrimitive>, font: UiFont?) {
    waitForCurrentFrameResourceSlot()
    if (swapchainManager.imageViews.isNotEmpty()) {
        ensureUiQuadPipeline()
        if (font != null) ensureGlyphPipeline(font)
        if (primitives.any { it is UiDrawPrimitive.Texture }) ensureTextureQuadPipeline()
        if (primitives.any { it is UiDrawPrimitive.RoundedQuad }) ensureRoundedQuadPipeline()
    }

    val runs = mutableListOf<Renderer.UiRun>()
    var quadRunCount = 0
    var roundedQuadRunCount = 0
    var glyphRunCount = 0
    val activePathClips = ArrayList<UiPath>()
    val clipKindStack = ArrayDeque<ClipKind>()
    // Per active ClipPathPush, the running intersection of every active clip's OWN
    // safeInteriorRect -- null means "at least one active clip has no known safe interior"
    // (e.g. pushed via the raw path-only `clip()` overload), so the exact-clip fast path
    // must always run for the whole stack, same as before this optimization existed. See
    // canSkipExactClip's doc comment for how this is consumed per primitive.
    val safeInteriorRectStack = ArrayDeque<UiBounds?>()
    var index = 0
    while (index < primitives.size) {
        val runStart = index
        val first = primitives[runStart]
        index += 1
        while (index < primitives.size && primitives[index]::class == first::class) index += 1
        val slice = primitives.subList(runStart, index)
        val safeInteriorRect = safeInteriorRectStack.lastOrNull()
        when (first) {
            is UiDrawPrimitive.Quad -> {
                // Chunk by actual vertex/index budget under exact-clip (a clip-cut corner can
                // add vertices beyond the fixed 4-per-quad the fast path assumes), else by
                // primitive count -- mirrors the RoundedQuad/GradientQuad/FilledPath fix below
                // and the pre-existing Glyph/StrokedPath fix. Real crash this guards against:
                // "UI rounded-quad-clipped run vertex count (1026) exceeds DynamicMesh capacity
                // (1024 vertices)" from 10 shadcn sliders' rounded-track quads inside one
                // rounded-clip container -- the identical unchunked assumption applied here too.
                @Suppress("UNCHECKED_CAST")
                val quadSlice = slice as List<UiDrawPrimitive.Quad>
                if (canExactClip(activePathClips)) {
                    val tessellated = quadSlice.map { quad ->
                        val raw = UiTriangleMesh(
                            points = listOf(
                                UiPoint(quad.x, quad.y),
                                UiPoint(quad.x + quad.w, quad.y),
                                UiPoint(quad.x + quad.w, quad.y + quad.h),
                                UiPoint(quad.x, quad.y + quad.h),
                            ),
                            indices = intArrayOf(0, 1, 2, 2, 3, 0),
                        )
                        val clipped = if (canSkipExactClip(safeInteriorRect, quad.x, quad.y, quad.w, quad.h)) {
                            raw
                        } else {
                            exactClip(raw, activePathClips)
                        }
                        clipped to quad.color
                    }
                    quadRunCount = stageChunkedColoredTriangleMeshes(runs, quadRunCount, tessellated, "quad")
                } else {
                    var chunkStart = 0
                    while (chunkStart < quadSlice.size) {
                        val chunkEnd = minOf(chunkStart + Renderer.MAX_UI_QUADS, quadSlice.size)
                        val mesh = quadMeshForRun(quadRunCount)
                        stageQuadRun(mesh, quadSlice.subList(chunkStart, chunkEnd))
                        runs += QuadRun(mesh)
                        quadRunCount += 1
                        chunkStart = chunkEnd
                    }
                }
            }
            is UiDrawPrimitive.GradientQuad -> {
                // See the Quad branch's doc comment for why both the exact-clip and fast paths
                // need chunking, not just the exact-clip one.
                @Suppress("UNCHECKED_CAST")
                val gradientSlice = slice as List<UiDrawPrimitive.GradientQuad>
                if (canExactClip(activePathClips)) {
                    val tessellated = gradientSlice.map { quad ->
                        val raw = UiColoredTriangleMesh(
                            vertices = listOf(
                                UiColoredVertex(UiPoint(quad.x, quad.y), quad.gradient.topLeft),
                                UiColoredVertex(UiPoint(quad.x + quad.w, quad.y), quad.gradient.topRight),
                                UiColoredVertex(UiPoint(quad.x + quad.w, quad.y + quad.h), quad.gradient.bottomRight),
                                UiColoredVertex(UiPoint(quad.x, quad.y + quad.h), quad.gradient.bottomLeft),
                            ),
                            indices = intArrayOf(0, 1, 2, 2, 3, 0),
                        )
                        if (canSkipExactClip(safeInteriorRect, quad.x, quad.y, quad.w, quad.h)) {
                            raw
                        } else {
                            exactClipColored(raw, activePathClips)
                        }
                    }
                    quadRunCount = stageChunkedColoredVertexTriangleMeshes(runs, quadRunCount, tessellated, "gradient-quad")
                } else {
                    var chunkStart = 0
                    while (chunkStart < gradientSlice.size) {
                        val chunkEnd = minOf(chunkStart + Renderer.MAX_UI_QUADS, gradientSlice.size)
                        val mesh = quadMeshForRun(quadRunCount)
                        stageGradientQuadRun(mesh, gradientSlice.subList(chunkStart, chunkEnd))
                        runs += QuadRun(mesh)
                        quadRunCount += 1
                        chunkStart = chunkEnd
                    }
                }
            }
            is UiDrawPrimitive.RoundedQuad -> {
                // Real crash this branch guards against: "UI rounded-quad-clipped run vertex
                // count (1026) exceeds DynamicMesh capacity (1024 vertices)" -- 10 shadcn
                // slider controls' rounded tracks landing inside one rounded-clip container
                // pushed this exact-clip tessellated run over one DynamicMesh's budget. Chunk
                // by actual vertex/index budget here (tessellateFillAa's AA fringe ring adds a
                // variable vertex count per quad, same as FilledPath below), and the SDF fast
                // path (no active clip) by primitive count -- it previously had neither.
                @Suppress("UNCHECKED_CAST")
                val roundedSlice = slice as List<UiDrawPrimitive.RoundedQuad>
                if (canExactClip(activePathClips)) {
                    val tessellated = roundedSlice.map { quad ->
                        val triangleMesh = RoundedRectangle(quad.radius.px)
                            .toPath(UiBounds(quad.x, quad.y, quad.w, quad.h))
                            .tessellateFillAa(quad.color)
                        if (canSkipExactClip(safeInteriorRect, quad.x, quad.y, quad.w, quad.h)) {
                            triangleMesh
                        } else {
                            exactClipColored(triangleMesh, activePathClips)
                        }
                    }
                    quadRunCount = stageChunkedColoredVertexTriangleMeshes(runs, quadRunCount, tessellated, "rounded-quad-clipped")
                } else {
                    var chunkStart = 0
                    while (chunkStart < roundedSlice.size) {
                        val chunkEnd = minOf(chunkStart + Renderer.MAX_UI_QUADS, roundedSlice.size)
                        val mesh = roundedQuadMeshForRun(roundedQuadRunCount)
                        stageRoundedQuadRun(mesh, roundedSlice.subList(chunkStart, chunkEnd))
                        runs += RoundedQuadRun(mesh)
                        roundedQuadRunCount += 1
                        chunkStart = chunkEnd
                    }
                }
            }
            is UiDrawPrimitive.FilledPath -> {
                // Path tessellation always produces a variable vertex count (unlike Quad's
                // fixed 4), so this needs vertex-budget chunking regardless of whether a clip
                // is active -- see the RoundedQuad branch's doc comment above.
                @Suppress("UNCHECKED_CAST")
                val pathSlice = slice as List<UiDrawPrimitive.FilledPath>
                val tessellated = pathSlice.map { primitive ->
                    val bounds = primitive.path.bounds()
                    val triangleMesh = primitive.path.tessellateFillAa(primitive.color)
                    if (canSkipExactClip(safeInteriorRect, bounds.x, bounds.y, bounds.width, bounds.height)) {
                        triangleMesh
                    } else {
                        exactClipColored(triangleMesh, activePathClips)
                    }
                }
                quadRunCount = stageChunkedColoredVertexTriangleMeshes(runs, quadRunCount, tessellated, "filled-path")
            }
            is UiDrawPrimitive.StrokedPath -> {
                // Chunk by actual vertex/index budget, not primitive count -- mirrors the
                // Glyph branch above (and the identical WebGPU-backend fix). Stroke
                // tessellation produces a variable vertex count per primitive (depends on
                // stroke width and path point count), so a fixed-size chunk of primitives can
                // still overflow DynamicMesh's capacity -- confirmed by a real crash from the
                // F3 debug wireframe overlay (UiDebugOverlayColors, 3 stroked rects per
                // semantic node) on a content-heavy page: "UI stroked-path run vertex count
                // (4192) exceeds DynamicMesh capacity (1024 vertices)".
                @Suppress("UNCHECKED_CAST")
                val strokedSlice = slice as List<UiDrawPrimitive.StrokedPath>
                val tessellated = strokedSlice.map { tessellateStrokedPath(it, activePathClips, safeInteriorRect) }
                quadRunCount = stageChunkedColoredTriangleMeshes(runs, quadRunCount, tessellated, "stroked-path")
            }
            is UiDrawPrimitive.Glyph -> {
                @Suppress("UNCHECKED_CAST")
                val glyphSlice = slice as List<UiDrawPrimitive.Glyph>
                if (canExactClip(activePathClips)) {
                    // Clipping a glyph quad against a convex path can add vertices beyond the
                    // fixed 4-per-glyph the fast path below assumes (e.g. a corner cut
                    // introduces a new vertex at the intersection) -- chunking by raw glyph
                    // count alone (as the fast path does) can overflow DynamicMesh capacity
                    // once enough glyphs land inside a clipped/rounded surface (real crash:
                    // "glyph run vertex count (1134) exceeds DynamicMesh capacity (1024)").
                    // Clip once, then bin into chunks by the actual resulting vertex/index
                    // budget instead of glyph count. Glyphs whose own bounds are already
                    // safely inside every active clip's safeInteriorRect skip the polygon
                    // clip entirely (see canSkipExactClip) -- their quad always stays the
                    // fixed 4 vertices, same as the un-clipped fast path below.
                    val clipped = glyphSlice.map { glyph ->
                        val raw = texturedQuadMesh(glyph.x, glyph.y, glyph.w, glyph.h, glyph.u0, glyph.v0, glyph.u1, glyph.v1)
                        val mesh = if (canSkipExactClip(safeInteriorRect, glyph.x, glyph.y, glyph.w, glyph.h)) {
                            raw
                        } else {
                            exactClip(raw, activePathClips)
                        }
                        mesh to glyph.color
                    }
                    val maxVertices = Renderer.MAX_UI_QUADS * DynamicMesh.VERTICES_PER_QUAD
                    val maxIndices = Renderer.MAX_UI_QUADS * DynamicMesh.INDICES_PER_QUAD
                    var chunk = mutableListOf<Pair<UiTexturedTriangleMesh, Color>>()
                    var chunkVertices = 0
                    var chunkIndices = 0
                    fun flushChunk() {
                        if (chunk.isEmpty()) return
                        val mesh = glyphMeshForRun(glyphRunCount)
                        stageTexturedTriangleMeshes(mesh, chunk, "glyph")
                        runs += GlyphRun(mesh)
                        glyphRunCount += 1
                        chunk = mutableListOf()
                        chunkVertices = 0
                        chunkIndices = 0
                    }
                    for (pair in clipped) {
                        val vertexCount = pair.first.vertices.size
                        val indexCount = pair.first.indices.size
                        if (chunk.isNotEmpty() && (chunkVertices + vertexCount > maxVertices || chunkIndices + indexCount > maxIndices)) {
                            flushChunk()
                        }
                        chunk += pair
                        chunkVertices += vertexCount
                        chunkIndices += indexCount
                    }
                    flushChunk()
                } else {
                    var chunkStart = 0
                    while (chunkStart < glyphSlice.size) {
                        val chunkEnd = minOf(chunkStart + Renderer.MAX_UI_QUADS, glyphSlice.size)
                        val mesh = glyphMeshForRun(glyphRunCount)
                        stageGlyphRun(mesh, glyphSlice.subList(chunkStart, chunkEnd), activePathClips, safeInteriorRect)
                        runs += GlyphRun(mesh)
                        glyphRunCount += 1
                        chunkStart = chunkEnd
                    }
                }
            }
            is UiDrawPrimitive.Texture -> {
                @Suppress("UNCHECKED_CAST")
                runs += TextureRun(stageTextureRun(slice as List<UiDrawPrimitive.Texture>, activePathClips, safeInteriorRect))
            }
            is UiDrawPrimitive.ClipPathPush -> {
                // Keep the resolved bounds scissor even when exact convex path clipping
                // is available: it stays the fallback for non-convex paths and still
                // trims work outside the path's enclosing rect.
                (slice as List<UiDrawPrimitive.ClipPathPush>).forEach {
                    clipKindStack.addLast(ClipKind.Path)
                    activePathClips += it.path
                    val parentSafeInteriorRect = safeInteriorRectStack.lastOrNull() ?: UNBOUNDED_SAFE_INTERIOR_RECT
                    safeInteriorRectStack.addLast(
                        it.safeInteriorRect?.let { own -> parentSafeInteriorRect?.intersect(own) },
                    )
                    runs += ClipRun(it.boundsRect)
                }
            }
            is UiDrawPrimitive.ClipPush -> {
                // Each ClipPush/ClipPop is its own run (never coalesced with a sibling --
                // they're distinct classes, so the same-class grouping above already
                // isolates them one at a time), consumed at the exact point in the paint
                // order they were emitted, same as any other run.
                (slice as List<UiDrawPrimitive.ClipPush>).forEach {
                    clipKindStack.addLast(ClipKind.Rect)
                    runs += ClipRun(it.rect)
                }
            }
            is UiDrawPrimitive.ClipPop -> {
                (slice as List<UiDrawPrimitive.ClipPop>).forEach {
                    when (clipKindStack.removeLastOrNull()) {
                        ClipKind.Path -> {
                            if (activePathClips.isNotEmpty()) activePathClips.removeAt(activePathClips.lastIndex)
                            safeInteriorRectStack.removeLastOrNull()
                        }
                        ClipKind.Rect, null -> Unit
                    }
                    runs += ClipRun(it.restoreRect)
                }
            }

            is UiDrawPrimitive.ShadowQuad -> TODO()
        }
    }
    uiRuns = runs
}

/** Effectively-infinite rect used as the "no ancestor constraint yet" starting point when
 * intersecting [UiDrawPrimitive.ClipPathPush.safeInteriorRect]s down the active clip stack
 * (see [performDrawUi]'s `safeInteriorRectStack`) -- intersecting any real rect against this
 * returns that rect unchanged. */
private val UNBOUNDED_SAFE_INTERIOR_RECT = UiBounds(-1e9f, -1e9f, 2e9f, 2e9f)

/** True when a primitive at [x]/[y] sized [w]x[h] is provably fully inside [safeInteriorRect]
 * (see [UiDrawPrimitive.ClipPathPush.safeInteriorRect]'s doc comment) -- when true, the
 * caller can skip the expensive exact convex-path clip for this ONE primitive entirely and
 * emit its plain unclipped geometry instead, since it geometrically cannot touch any active
 * clip's rounded/cut corner region. `safeInteriorRect == null` (no active clip, or at least
 * one active clip has no known safe interior) is always conservative -- never skips. */
internal fun canSkipExactClip(safeInteriorRect: UiBounds?, x: Float, y: Float, w: Float, h: Float): Boolean =
    safeInteriorRect != null && safeInteriorRect.contains(UiBounds(x, y, w, h))

/** Writes [quads] (one run's worth, not necessarily this frame's whole quad count) into
 * [mesh] -- extracted from the old single-mesh `drawUi` body, unchanged vertex/index
 * layout. */
internal fun Renderer.stageQuadRun(
    mesh: DynamicMesh,
    quads: List<UiDrawPrimitive.Quad>,
) {
    require(quads.size <= Renderer.MAX_UI_QUADS) {
        "UI quad run size (${quads.size}) exceeds Renderer's DynamicMesh capacity (${Renderer.MAX_UI_QUADS})."
    }
    val vertices = FloatArray(quads.size * DynamicMesh.VERTICES_PER_QUAD * DynamicMesh.FLOATS_PER_VERTEX)
    val indices = IntArray(quads.size * DynamicMesh.INDICES_PER_QUAD)
    var quadIndex = 0
    while (quadIndex < quads.size) {
        val quad = quads[quadIndex]
        val vertexBase = quadIndex * DynamicMesh.VERTICES_PER_QUAD * DynamicMesh.FLOATS_PER_VERTEX
        // Triangle-list quad, corners in TL/TR/BR/BL order (pixel space, Y-down).
        writeVertex(vertices, vertexBase + 0 * DynamicMesh.FLOATS_PER_VERTEX, quad.x, quad.y, quad.color, quad.transform)
        writeVertex(vertices, vertexBase + 1 * DynamicMesh.FLOATS_PER_VERTEX, quad.x + quad.w, quad.y, quad.color, quad.transform)
        writeVertex(vertices, vertexBase + 2 * DynamicMesh.FLOATS_PER_VERTEX, quad.x + quad.w, quad.y + quad.h, quad.color, quad.transform)
        writeVertex(vertices, vertexBase + 3 * DynamicMesh.FLOATS_PER_VERTEX, quad.x, quad.y + quad.h, quad.color, quad.transform)

        val vertexOffset = quadIndex * DynamicMesh.VERTICES_PER_QUAD
        val indexBase = quadIndex * DynamicMesh.INDICES_PER_QUAD
        indices[indexBase] = vertexOffset
        indices[indexBase + 1] = vertexOffset + 1
        indices[indexBase + 2] = vertexOffset + 2
        indices[indexBase + 3] = vertexOffset + 2
        indices[indexBase + 4] = vertexOffset + 3
        indices[indexBase + 5] = vertexOffset
        quadIndex += 1
    }
    mesh.update(swapchainManager.currentFrame, vertices, indices)
}

internal fun Renderer.stageGradientQuadRun(
    mesh: DynamicMesh,
    quads: List<UiDrawPrimitive.GradientQuad>,
) {
    require(quads.size <= Renderer.MAX_UI_QUADS) {
        "UI gradient quad run size (${quads.size}) exceeds Renderer's DynamicMesh capacity (${Renderer.MAX_UI_QUADS})."
    }
    val vertices = FloatArray(quads.size * DynamicMesh.VERTICES_PER_QUAD * DynamicMesh.FLOATS_PER_VERTEX)
    val indices = IntArray(quads.size * DynamicMesh.INDICES_PER_QUAD)
    var quadIndex = 0
    while (quadIndex < quads.size) {
        val quad = quads[quadIndex]
        val vertexBase = quadIndex * DynamicMesh.VERTICES_PER_QUAD * DynamicMesh.FLOATS_PER_VERTEX
        writeVertex(vertices, vertexBase + 0 * DynamicMesh.FLOATS_PER_VERTEX, quad.x, quad.y, quad.gradient.topLeft)
        writeVertex(vertices, vertexBase + 1 * DynamicMesh.FLOATS_PER_VERTEX, quad.x + quad.w, quad.y, quad.gradient.topRight)
        writeVertex(vertices, vertexBase + 2 * DynamicMesh.FLOATS_PER_VERTEX, quad.x + quad.w, quad.y + quad.h, quad.gradient.bottomRight)
        writeVertex(vertices, vertexBase + 3 * DynamicMesh.FLOATS_PER_VERTEX, quad.x, quad.y + quad.h, quad.gradient.bottomLeft)

        val vertexOffset = quadIndex * DynamicMesh.VERTICES_PER_QUAD
        val indexBase = quadIndex * DynamicMesh.INDICES_PER_QUAD
        indices[indexBase] = vertexOffset
        indices[indexBase + 1] = vertexOffset + 1
        indices[indexBase + 2] = vertexOffset + 2
        indices[indexBase + 3] = vertexOffset + 2
        indices[indexBase + 4] = vertexOffset + 3
        indices[indexBase + 5] = vertexOffset
        quadIndex += 1
    }
    mesh.update(swapchainManager.currentFrame, vertices, indices)
}

/** Tessellates+clips a single [UiDrawPrimitive.StrokedPath] -- extracted so `performDrawUi`'s
 * `StrokedPath` branch can chunk by actual vertex/index budget (see its doc comment) instead of
 * tessellating a whole run at once before knowing whether it fits one [DynamicMesh]. Mirrors
 * the identical WebGPU-backend fix. */
internal fun Renderer.tessellateStrokedPath(
    primitive: UiDrawPrimitive.StrokedPath,
    activePathClips: List<UiPath>,
    safeInteriorRect: UiBounds?,
): Pair<UiTriangleMesh, Color> {
    // Stroke geometry extends up to a full stroke-width beyond the raw path's own point
    // bounds (perpendicular offset on each side, plus square-cap extension along the
    // tangent at open-contour ends) -- widen the containment check by the full width
    // (not just half) to stay conservative rather than exactly modeling miter joins.
    val rawBounds = primitive.path.bounds()
    val strokeMargin = primitive.stroke.width.toPx()
    val paintedBounds = UiBounds(
        rawBounds.x - strokeMargin,
        rawBounds.y - strokeMargin,
        rawBounds.width + 2f * strokeMargin,
        rawBounds.height + 2f * strokeMargin,
    )
    val triangleMesh = primitive.path.tessellateStroke(primitive.stroke)
    val clipped = if (canSkipExactClip(safeInteriorRect, paintedBounds.x, paintedBounds.y, paintedBounds.width, paintedBounds.height)) {
        triangleMesh
    } else {
        exactClip(triangleMesh, activePathClips)
    }
    return clipped to primitive.color
}

/** Chunks already-tessellated [geometries] by actual vertex/index budget (not primitive
 * count) across as many pooled quad meshes as needed, appending one [Renderer.UiRun.QuadRun]
 * per chunk to [runs] -- shared by every "flat color per triangle mesh" run
 * (Quad/StrokedPath's exact-clip paths) that can produce a variable vertex count per
 * primitive, so a run whose total tessellated size exceeds one [DynamicMesh]'s capacity
 * splits across multiple runs instead of throwing (the exact crash class Glyph/StrokedPath
 * were fixed against earlier -- see this file's history). Returns the updated running
 * `quadRunCount` for the caller to keep threading through its `var`. */
internal fun Renderer.stageChunkedColoredTriangleMeshes(
    runs: MutableList<Renderer.UiRun>,
    quadRunCount: Int,
    geometries: List<Pair<UiTriangleMesh, Color>>,
    label: String,
): Int {
    val maxVertices = Renderer.MAX_UI_QUADS * DynamicMesh.VERTICES_PER_QUAD
    val maxIndices = Renderer.MAX_UI_QUADS * DynamicMesh.INDICES_PER_QUAD
    var count = quadRunCount
    var chunk = mutableListOf<Pair<UiTriangleMesh, Color>>()
    var chunkVertices = 0
    var chunkIndices = 0
    fun flushChunk() {
        if (chunk.isEmpty()) return
        val mesh = quadMeshForRun(count)
        stageColoredTriangleMeshes(mesh, chunk, label)
        runs += Renderer.UiRun.QuadRun(mesh)
        count += 1
        chunk = mutableListOf()
        chunkVertices = 0
        chunkIndices = 0
    }
    for (pair in geometries) {
        val vertexCount = pair.first.points.size
        val indexCount = pair.first.indices.size
        if (chunk.isNotEmpty() && (chunkVertices + vertexCount > maxVertices || chunkIndices + indexCount > maxIndices)) {
            flushChunk()
        }
        chunk += pair
        chunkVertices += vertexCount
        chunkIndices += indexCount
    }
    flushChunk()
    return count
}

/** Per-vertex-colored sibling of [stageChunkedColoredTriangleMeshes] -- shared by every
 * already-tessellated [UiColoredTriangleMesh] run (RoundedQuad/FilledPath/GradientQuad's
 * exact-clip paths), same chunk-by-actual-budget behavior. */
internal fun Renderer.stageChunkedColoredVertexTriangleMeshes(
    runs: MutableList<Renderer.UiRun>,
    quadRunCount: Int,
    meshes: List<UiColoredTriangleMesh>,
    label: String,
): Int {
    val maxVertices = Renderer.MAX_UI_QUADS * DynamicMesh.VERTICES_PER_QUAD
    val maxIndices = Renderer.MAX_UI_QUADS * DynamicMesh.INDICES_PER_QUAD
    var count = quadRunCount
    var chunk = mutableListOf<UiColoredTriangleMesh>()
    var chunkVertices = 0
    var chunkIndices = 0
    fun flushChunk() {
        if (chunk.isEmpty()) return
        val mesh = quadMeshForRun(count)
        stageColoredVertexTriangleMeshes(mesh, chunk, label)
        runs += Renderer.UiRun.QuadRun(mesh)
        count += 1
        chunk = mutableListOf()
        chunkVertices = 0
        chunkIndices = 0
    }
    for (m in meshes) {
        val vertexCount = m.vertices.size
        val indexCount = m.indices.size
        if (chunk.isNotEmpty() && (chunkVertices + vertexCount > maxVertices || chunkIndices + indexCount > maxIndices)) {
            flushChunk()
        }
        chunk += m
        chunkVertices += vertexCount
        chunkIndices += indexCount
    }
    flushChunk()
    return count
}

internal fun Renderer.stageColoredTriangleMeshes(
    mesh: DynamicMesh,
    geometries: List<Pair<UiTriangleMesh, Color>>,
    label: String,
) {
    val maxVertices = Renderer.MAX_UI_QUADS * DynamicMesh.VERTICES_PER_QUAD
    val maxIndices = Renderer.MAX_UI_QUADS * DynamicMesh.INDICES_PER_QUAD
    val totalVertices = geometries.sumOf { it.first.points.size }
    val totalIndices = geometries.sumOf { it.first.indices.size }
    require(totalVertices <= maxVertices) {
        "UI $label run vertex count ($totalVertices) exceeds DynamicMesh capacity ($maxVertices vertices)."
    }
    require(totalIndices <= maxIndices) {
        "UI $label run index count ($totalIndices) exceeds DynamicMesh capacity ($maxIndices indices)."
    }

    val vertices = FloatArray(totalVertices * DynamicMesh.FLOATS_PER_VERTEX)
    val indices = IntArray(totalIndices)
    var vertexCursor = 0
    var indexCursor = 0
    var vertexOffset = 0

    geometries.forEach { (triangleMesh, color) ->
        triangleMesh.points.forEach { point ->
            writeVertex(vertices, vertexCursor, point.x, point.y, color)
            vertexCursor += DynamicMesh.FLOATS_PER_VERTEX
        }
        triangleMesh.indices.forEach { index ->
            indices[indexCursor] = vertexOffset + index
            indexCursor += 1
        }
        vertexOffset += triangleMesh.points.size
    }
    mesh.update(swapchainManager.currentFrame, vertices, indices)
}

internal fun canExactClip(paths: List<UiPath>): Boolean = paths.isNotEmpty() && paths.all { it.convexClipContour() != null }

internal fun exactClip(mesh: UiTriangleMesh, activePathClips: List<UiPath>): UiTriangleMesh =
    if (canExactClip(activePathClips)) mesh.clipToConvexPaths(activePathClips) else mesh

internal fun exactClip(mesh: UiTexturedTriangleMesh, activePathClips: List<UiPath>): UiTexturedTriangleMesh =
    if (canExactClip(activePathClips)) mesh.clipToConvexPaths(activePathClips) else mesh

internal fun exactClipColored(mesh: UiColoredTriangleMesh, activePathClips: List<UiPath>): UiColoredTriangleMesh =
    if (canExactClip(activePathClips)) mesh.clipToConvexPaths(activePathClips) else mesh

/** Per-vertex-colored sibling of [stageColoredTriangleMeshes] -- [UiDrawPrimitive.GradientQuad]'s
 * exact-clip path needs each vertex's OWN color (its corner of the gradient, or an interpolated
 * color at a clip-cut edge), not one flat color per mesh like every other exact-clipped
 * primitive. */
internal fun Renderer.stageColoredVertexTriangleMeshes(
    mesh: DynamicMesh,
    meshes: List<UiColoredTriangleMesh>,
    label: String,
) {
    val maxVertices = Renderer.MAX_UI_QUADS * DynamicMesh.VERTICES_PER_QUAD
    val maxIndices = Renderer.MAX_UI_QUADS * DynamicMesh.INDICES_PER_QUAD
    val totalVertices = meshes.sumOf { it.vertices.size }
    val totalIndices = meshes.sumOf { it.indices.size }
    require(totalVertices <= maxVertices) {
        "UI $label run vertex count ($totalVertices) exceeds DynamicMesh capacity ($maxVertices vertices)."
    }
    require(totalIndices <= maxIndices) {
        "UI $label run index count ($totalIndices) exceeds DynamicMesh capacity ($maxIndices indices)."
    }

    val vertices = FloatArray(totalVertices * DynamicMesh.FLOATS_PER_VERTEX)
    val indices = IntArray(totalIndices)
    var vertexCursor = 0
    var indexCursor = 0
    var vertexOffset = 0

    meshes.forEach { triangleMesh ->
        triangleMesh.vertices.forEach { vertex ->
            writeVertex(vertices, vertexCursor, vertex.position.x, vertex.position.y, vertex.color)
            vertexCursor += DynamicMesh.FLOATS_PER_VERTEX
        }
        triangleMesh.indices.forEach { index ->
            indices[indexCursor] = vertexOffset + index
            indexCursor += 1
        }
        vertexOffset += triangleMesh.vertices.size
    }
    mesh.update(swapchainManager.currentFrame, vertices, indices)
}

/** Writes [quads] (one run's worth) into [mesh] using the rounded-quad vertex layout --
 * pos(vec2) + localPos(vec2, pixels relative to the quad's own center) + halfSize(vec2) +
 * radius(float) + color(vec4), consumed by `ui_rounded_quad.frag`'s distance-field test.
 * [localPos] is exactly `±halfSize` at each of the 4 corners; the GPU linearly
 * interpolates it across each fragment inside the quad, giving the fragment shader the
 * pixel-accurate local coordinate its SDF needs without a second render pass. */
internal fun Renderer.stageRoundedQuadRun(mesh: DynamicMesh, quads: List<UiDrawPrimitive.RoundedQuad>) {
    require(quads.size <= Renderer.MAX_UI_QUADS) {
        "UI rounded-quad run size (${quads.size}) exceeds Renderer's DynamicMesh capacity (${Renderer.MAX_UI_QUADS})."
    }
    val floatsPerVertex = DynamicMesh.ROUNDED_QUAD_FLOATS_PER_VERTEX
    val vertices = FloatArray(quads.size * DynamicMesh.VERTICES_PER_QUAD * floatsPerVertex)
    val indices = IntArray(quads.size * DynamicMesh.INDICES_PER_QUAD)
    var quadIndex = 0
    while (quadIndex < quads.size) {
        val quad = quads[quadIndex]
        val halfW = quad.w / 2f
        val halfH = quad.h / 2f
        // Radius can't exceed either half-dimension -- a corner radius bigger than the
        // quad itself would make the SDF math produce a self-intersecting shape.
        val radius = quad.radius.coerceAtMost(minOf(halfW, halfH))
        val vertexBase = quadIndex * DynamicMesh.VERTICES_PER_QUAD * floatsPerVertex
        writeRoundedQuadVertex(vertices, vertexBase + 0 * floatsPerVertex, quad.x, quad.y, -halfW, -halfH, halfW, halfH, radius, quad.smoothing, quad.color, quad.transform)
        writeRoundedQuadVertex(vertices, vertexBase + 1 * floatsPerVertex, quad.x + quad.w, quad.y, halfW, -halfH, halfW, halfH, radius, quad.smoothing, quad.color, quad.transform)
        writeRoundedQuadVertex(vertices, vertexBase + 2 * floatsPerVertex, quad.x + quad.w, quad.y + quad.h, halfW, halfH, halfW, halfH, radius, quad.smoothing, quad.color, quad.transform)
        writeRoundedQuadVertex(vertices, vertexBase + 3 * floatsPerVertex, quad.x, quad.y + quad.h, -halfW, halfH, halfW, halfH, radius, quad.smoothing, quad.color, quad.transform)

        val vertexOffset = quadIndex * DynamicMesh.VERTICES_PER_QUAD
        val indexBase = quadIndex * DynamicMesh.INDICES_PER_QUAD
        indices[indexBase] = vertexOffset
        indices[indexBase + 1] = vertexOffset + 1
        indices[indexBase + 2] = vertexOffset + 2
        indices[indexBase + 3] = vertexOffset + 2
        indices[indexBase + 4] = vertexOffset + 3
        indices[indexBase + 5] = vertexOffset
        quadIndex += 1
    }
    mesh.update(swapchainManager.currentFrame, vertices, indices)
}

/** Writes [glyphs] (one run's worth) into [mesh] -- extracted from the old single-mesh
 * `drawUi` body, unchanged vertex/index layout unless exact path clipping is active. */
internal fun Renderer.stageGlyphRun(
    mesh: DynamicMesh,
    glyphs: List<UiDrawPrimitive.Glyph>,
    activePathClips: List<UiPath> = emptyList(),
    safeInteriorRect: UiBounds? = null,
) {
    if (canExactClip(activePathClips)) {
        stageTexturedTriangleMeshes(
            mesh,
            glyphs.map { glyph ->
                val raw = texturedQuadMesh(glyph.x, glyph.y, glyph.w, glyph.h, glyph.u0, glyph.v0, glyph.u1, glyph.v1)
                val clipped = if (canSkipExactClip(safeInteriorRect, glyph.x, glyph.y, glyph.w, glyph.h)) {
                    raw
                } else {
                    exactClip(raw, activePathClips)
                }
                clipped to glyph.color
            },
            "glyph",
        )
        return
    }
    require(glyphs.size <= Renderer.MAX_UI_QUADS) {
        "UI glyph run size (${glyphs.size}) exceeds Renderer's DynamicMesh capacity (${Renderer.MAX_UI_QUADS})."
    }
    val glyphVertices = FloatArray(glyphs.size * DynamicMesh.VERTICES_PER_QUAD * DynamicMesh.GLYPH_FLOATS_PER_VERTEX)
    val glyphIndices = IntArray(glyphs.size * DynamicMesh.INDICES_PER_QUAD)
    var glyphIndex = 0
    while (glyphIndex < glyphs.size) {
        val glyph = glyphs[glyphIndex]
        val vertexBase = glyphIndex * DynamicMesh.VERTICES_PER_QUAD * DynamicMesh.GLYPH_FLOATS_PER_VERTEX
        writeGlyphVertex(glyphVertices, vertexBase + 0 * DynamicMesh.GLYPH_FLOATS_PER_VERTEX, glyph.x, glyph.y, glyph.u0, glyph.v0, glyph.color, glyph.transform)
        writeGlyphVertex(glyphVertices, vertexBase + 1 * DynamicMesh.GLYPH_FLOATS_PER_VERTEX, glyph.x + glyph.w, glyph.y, glyph.u1, glyph.v0, glyph.color, glyph.transform)
        writeGlyphVertex(glyphVertices, vertexBase + 2 * DynamicMesh.GLYPH_FLOATS_PER_VERTEX, glyph.x + glyph.w, glyph.y + glyph.h, glyph.u1, glyph.v1, glyph.color, glyph.transform)
        writeGlyphVertex(glyphVertices, vertexBase + 3 * DynamicMesh.GLYPH_FLOATS_PER_VERTEX, glyph.x, glyph.y + glyph.h, glyph.u0, glyph.v1, glyph.color, glyph.transform)

        val vertexOffset = glyphIndex * DynamicMesh.VERTICES_PER_QUAD
        val indexBase = glyphIndex * DynamicMesh.INDICES_PER_QUAD
        glyphIndices[indexBase] = vertexOffset
        glyphIndices[indexBase + 1] = vertexOffset + 1
        glyphIndices[indexBase + 2] = vertexOffset + 2
        glyphIndices[indexBase + 3] = vertexOffset + 2
        glyphIndices[indexBase + 4] = vertexOffset + 3
        glyphIndices[indexBase + 5] = vertexOffset
        glyphIndex += 1
    }
    mesh.update(swapchainManager.currentFrame, glyphVertices, glyphIndices)
}

internal fun Renderer.stageTextureRun(
    textures: List<UiDrawPrimitive.Texture>,
    activePathClips: List<UiPath>,
    safeInteriorRect: UiBounds? = null,
): List<Renderer.TexturedPrimitiveRun> =
    textures.map { primitive ->
        val raw = texturedQuadMesh(primitive.x, primitive.y, primitive.w, primitive.h)
        val clipped = if (canSkipExactClip(safeInteriorRect, primitive.x, primitive.y, primitive.w, primitive.h)) {
            raw
        } else {
            exactClip(raw, activePathClips)
        }
        val (vertices, indices) = texturedGeometryBuffers(clipped, Renderer.WHITE_RGBA, primitive.transform)
        Renderer.TexturedPrimitiveRun(primitive.material, vertices, indices)
    }

internal fun Renderer.stageTexturedTriangleMeshes(
    mesh: DynamicMesh,
    geometries: List<Pair<UiTexturedTriangleMesh, Color>>,
    label: String,
) {
    val maxVertices = Renderer.MAX_UI_QUADS * DynamicMesh.VERTICES_PER_QUAD
    val maxIndices = Renderer.MAX_UI_QUADS * DynamicMesh.INDICES_PER_QUAD
    val totalVertices = geometries.sumOf { it.first.vertices.size }
    val totalIndices = geometries.sumOf { it.first.indices.size }
    require(totalVertices <= maxVertices) {
        "UI $label run vertex count ($totalVertices) exceeds DynamicMesh capacity ($maxVertices vertices)."
    }
    require(totalIndices <= maxIndices) {
        "UI $label run index count ($totalIndices) exceeds DynamicMesh capacity ($maxIndices indices)."
    }

    val vertices = FloatArray(totalVertices * DynamicMesh.GLYPH_FLOATS_PER_VERTEX)
    val indices = IntArray(totalIndices)
    var vertexCursor = 0
    var indexCursor = 0
    var vertexOffset = 0

    geometries.forEach { (triangleMesh, color) ->
        triangleMesh.vertices.forEach { vertex ->
            writeGlyphVertex(vertices, vertexCursor, vertex.position.x, vertex.position.y, vertex.u, vertex.v, color)
            vertexCursor += DynamicMesh.GLYPH_FLOATS_PER_VERTEX
        }
        triangleMesh.indices.forEach { index ->
            indices[indexCursor] = vertexOffset + index
            indexCursor += 1
        }
        vertexOffset += triangleMesh.vertices.size
    }
    mesh.update(swapchainManager.currentFrame, vertices, indices)
}

internal fun texturedQuadMesh(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    u0: Float = 0f,
    v0: Float = 0f,
    u1: Float = 1f,
    v1: Float = 1f,
): UiTexturedTriangleMesh = UiTexturedTriangleMesh(
    vertices = listOf(
        UiTexturedVertex(UiPoint(x, y), u0, v0),
        UiTexturedVertex(UiPoint(x + w, y), u1, v0),
        UiTexturedVertex(UiPoint(x + w, y + h), u1, v1),
        UiTexturedVertex(UiPoint(x, y + h), u0, v1),
    ),
    indices = intArrayOf(0, 1, 2, 2, 3, 0),
)

internal fun texturedGeometryBuffers(
    mesh: UiTexturedTriangleMesh,
    color: Color,
    transform: io.github.ronjunevaldoz.awake.ui.UiPrimitiveTransform? = null,
): Pair<FloatArray, IntArray> {
    val vertices = FloatArray(mesh.vertices.size * DynamicMesh.GLYPH_FLOATS_PER_VERTEX)
    var offset = 0
    mesh.vertices.forEach { vertex ->
        writeGlyphVertex(vertices, offset, vertex.position.x, vertex.position.y, vertex.u, vertex.v, color, transform)
        offset += DynamicMesh.GLYPH_FLOATS_PER_VERTEX
    }
    return vertices to mesh.indices
}

/**
 * Headless/offscreen test hook: renders a full frame's worth of [UiDrawPrimitive]s (quads,
 * rounded quads, glyphs, clip-rect scissors) into [target] through the real Vulkan UI
 * pipelines, not the CPU rasterizer ([io.github.ronjunevaldoz.awake.testing.ui
 * .saveAwakeUiPreview]'s path). Exists so a real animation can be captured frame-by-frame
 * exactly as the live app's Vulkan backend actually draws it (frame pacing, clip-scissor
 * timing, and all) instead of only ever sampling logical [UiBounds] -- see
 * `UiAnimationFrameCapture` (`desktopTest`) for the reusable N-frame recording loop built on
 * top of this.
 *
 * Deliberately narrower than [performDrawUi]/[recordCommandBuffer]'s swapchain UI pass:
 * [UiDrawPrimitive.Texture] runs are skipped (no offscreen texture-quad pipeline exists yet --
 * add one the same way [ensureOffscreenQuadPipeline] was added if a captured animation ever
 * needs to show a render-target-backed texture, e.g. a minimap). Every shadcn widget this was
 * built to investigate (cards, buttons, collapsibles, text) only emits quads/rounded
 * quads/glyphs/clips, so that gap doesn't block real use yet.
 *
 * Reuses [target]'s own framebuffer/render pass ([Renderer.renderPipeline]'s, same as
 * [Renderer.renderToTexture]/[renderUiGlyphsToTexture]) rather than the swapchain UI pass's
 * private render pass -- see [Renderer.offscreenQuadRenderPipeline]'s doc comment for why the
 * swapchain pipeline can't be reused directly headless.
 */
fun Renderer.renderUiToTexture(target: RenderTarget, primitives: List<UiDrawPrimitive>, font: UiFont?) {
    val offscreen = target as OffscreenRenderTarget
    performDrawUi(primitives, font)
    val frameIndex = swapchainManager.currentFrame

    ensureOffscreenQuadPipeline()
    if (font != null) ensureOffscreenGlyphPipeline(font)
    if (primitives.any { it is UiDrawPrimitive.RoundedQuad }) ensureOffscreenRoundedQuadPipeline()

    val quadPipeline = requireNotNull(offscreenQuadRenderPipeline)
    quadPipeline.writeScreenSize(offscreen.width.toFloat(), offscreen.height.toFloat())
    offscreenRoundedQuadRenderPipeline?.writeScreenSize(offscreen.width.toFloat(), offscreen.height.toFloat())
    offscreenGlyphRenderPipeline?.writeScreenSize(offscreen.width.toFloat(), offscreen.height.toFloat())

    runOffscreenCommands { commandBuffer ->
        val renderPassInfo = VkRenderPassBeginInfo(
            renderPass = renderPipeline.renderPass,
            framebuffer = offscreen.framebuffer,
            renderArea = VkRect2D(extent = VkExtent2D(offscreen.width, offscreen.height)),
            pClearValues = arrayOf(clearColorValue, Renderer.clearDepthValue),
        )
        Vulkan.vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE)
        val viewport = VkViewport(width = offscreen.width.toFloat(), height = offscreen.height.toFloat())
        Vulkan.vkCmdSetViewport(commandBuffer, 0, arrayOf(viewport))
        val fullScissor = VkRect2D(extent = VkExtent2D(offscreen.width, offscreen.height))
        Vulkan.vkCmdSetScissor(commandBuffer, 0, arrayOf(fullScissor))

        var runIndex = 0
        while (runIndex < uiRuns.size) {
            when (val run = uiRuns[runIndex]) {
                is Renderer.UiRun.QuadRun -> {
                    quadPipeline.bind(commandBuffer)
                    run.mesh.bind(frameIndex, commandBuffer)
                    run.mesh.draw(frameIndex, commandBuffer)
                }
                is Renderer.UiRun.RoundedQuadRun -> {
                    offscreenRoundedQuadRenderPipeline?.let { pipeline ->
                        pipeline.bind(commandBuffer)
                        run.mesh.bind(frameIndex, commandBuffer)
                        run.mesh.draw(frameIndex, commandBuffer)
                    }
                }
                is Renderer.UiRun.GlyphRun -> {
                    offscreenGlyphRenderPipeline?.let { pipeline ->
                        pipeline.bind(commandBuffer)
                        run.mesh.bind(frameIndex, commandBuffer)
                        run.mesh.draw(frameIndex, commandBuffer)
                    }
                }
                is Renderer.UiRun.ClipRun -> {
                    // Same defensive clamp recordCommandBuffer's swapchain UI pass applies.
                    val maxX = offscreen.width
                    val maxY = offscreen.height
                    val x = run.rect.x.toInt().coerceIn(0, maxX)
                    val y = run.rect.y.toInt().coerceIn(0, maxY)
                    val width = run.rect.width.toInt().coerceAtLeast(0).coerceAtMost(maxX - x)
                    val height = run.rect.height.toInt().coerceAtLeast(0).coerceAtMost(maxY - y)
                    val scissor = VkRect2D(offset = VkOffset2D(x, y), extent = VkExtent2D(width, height))
                    Vulkan.vkCmdSetScissor(commandBuffer, 0, arrayOf(scissor))
                }
                is Renderer.UiRun.TextureRun -> Unit // see doc comment above: not supported yet.
            }
            runIndex += 1
        }

        Vulkan.vkCmdEndRenderPass(commandBuffer)
        offscreen.transitionToShaderReadOnly(commandBuffer)
    }
}

/**
 * Headless/offscreen test hook: renders glyph primitives into [target] using the real
 * Vulkan glyph pipeline rather than the CPU snapshot path. Kept on the concrete Vulkan
 * renderer (not the backend-neutral interface) because it exists to baseline this
 * backend's own font pipeline, not to become cross-backend API surface.
 */
fun Renderer.renderUiGlyphsToTexture(target: RenderTarget, glyphs: List<UiDrawPrimitive.Glyph>, font: UiFont) {
    val offscreen = target as OffscreenRenderTarget
    waitForCurrentFrameResourceSlot()
    val frameIndex = swapchainManager.currentFrame
    ensureOffscreenGlyphPipeline(font)
    val glyphPipeline = requireNotNull(offscreenGlyphRenderPipeline)
    glyphPipeline.writeScreenSize(offscreen.width.toFloat(), offscreen.height.toFloat())

    val glyphRunMeshes = buildList {
        var chunkStart = 0
        var glyphRunCount = 0
        while (chunkStart < glyphs.size) {
            val chunkEnd = minOf(chunkStart + Renderer.MAX_UI_QUADS, glyphs.size)
            val mesh = glyphMeshForRun(glyphRunCount)
            stageGlyphRun(mesh, glyphs.subList(chunkStart, chunkEnd))
            add(mesh)
            glyphRunCount += 1
            chunkStart = chunkEnd
        }
    }

    runOffscreenCommands { commandBuffer ->
        val renderPassInfo = VkRenderPassBeginInfo(
            renderPass = renderPipeline.renderPass,
            framebuffer = offscreen.framebuffer,
            renderArea = VkRect2D(extent = VkExtent2D(offscreen.width, offscreen.height)),
            pClearValues = arrayOf(clearColorValue, Renderer.clearDepthValue),
        )
        Vulkan.vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE)
        val viewport = VkViewport(width = offscreen.width.toFloat(), height = offscreen.height.toFloat())
        Vulkan.vkCmdSetViewport(commandBuffer, 0, arrayOf(viewport))
        val scissor = VkRect2D(extent = VkExtent2D(offscreen.width, offscreen.height))
        Vulkan.vkCmdSetScissor(commandBuffer, 0, arrayOf(scissor))
        glyphPipeline.bind(commandBuffer)
        glyphRunMeshes.forEach { mesh ->
            mesh.bind(frameIndex, commandBuffer)
            mesh.draw(frameIndex, commandBuffer)
        }
        Vulkan.vkCmdEndRenderPass(commandBuffer)
        offscreen.transitionToShaderReadOnly(commandBuffer)
    }
}
