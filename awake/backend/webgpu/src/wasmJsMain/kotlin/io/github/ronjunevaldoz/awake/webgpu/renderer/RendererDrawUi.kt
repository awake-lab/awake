// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.renderer

import io.github.ronjunevaldoz.awake.ui.UiColoredTriangleMesh
import io.github.ronjunevaldoz.awake.ui.UiColoredVertex
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPath
import io.github.ronjunevaldoz.awake.ui.UiPoint
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.UiTexturedTriangleMesh
import io.github.ronjunevaldoz.awake.ui.UiTexturedVertex
import io.github.ronjunevaldoz.awake.ui.UiTriangleMesh
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.contains
import io.github.ronjunevaldoz.awake.ui.api.layout.intersect
import io.github.ronjunevaldoz.awake.ui.bounds
import io.github.ronjunevaldoz.awake.ui.clipToConvexPaths
import io.github.ronjunevaldoz.awake.ui.convexClipContour
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.render.passes.ui.writeGlyphVertex
import io.github.ronjunevaldoz.awake.render.passes.ui.writeRoundedQuadVertex
import io.github.ronjunevaldoz.awake.render.passes.ui.writeVertex
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.splitToCapacity
import io.github.ronjunevaldoz.awake.ui.tessellateFillAa
import io.github.ronjunevaldoz.awake.ui.tessellateStroke
import io.github.ronjunevaldoz.awake.ui.toPath
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.webgpu.ui.DynamicMesh
import io.github.ronjunevaldoz.awake.core.colors.Color as AwakeColor

/** UI primitive staging -- `performDrawUi` walks a frame's [UiDrawPrimitive] list once,
 * coalescing adjacent same-type entries into runs (see [Renderer.UiRun]'s doc comment) and
 * writing each run's vertex/index data into a pooled [DynamicMesh]. Issues no GPU commands
 * itself -- `performDraw` ([RendererDraw3D.kt]) consumes the staged [Renderer.uiRuns] on the
 * next frame's UI pass. See [Renderer]'s class doc comment for why this lives here as
 * `internal` extension functions rather than as members. */

private enum class ClipKind {
    Rect,
    Path,
}

/** Stages this frame's UI overlay content -- rewrites [Renderer.uiRuns]' pooled meshes but
 * issues no GPU commands itself. Must be called BEFORE `performDraw` (see
 * `WebGpuGameApplication.onRender()`'s ordering) so that pass's UI pass draws this frame's
 * widgets rather than lagging a frame behind. Lazily builds the (quad) UI pipeline on first
 * call, and the glyph pipeline on the first call that passes a non-null [font] -- see
 * [ensureUiQuadPipeline]/[ensureGlyphPipeline]'s doc comments.
 *
 * Walks [primitives] once, coalescing adjacent same-type entries into runs so `performDraw`
 * can issue draw calls in the SAME order [primitives] arrived in -- see Vulkan's
 * `Renderer.drawUi()`'s doc comment (this mirrors it) for the full "all quads, then all
 * glyphs" bug this fixes. Named `performDrawUi`, not `drawUi` -- see `performDraw`'s
 * ([RendererDraw3D.kt]) doc comment for why. */
internal fun Renderer.performDrawUi(primitives: List<UiDrawPrimitive>, font: UiFont?) {
    ensureUiQuadPipeline()
    if (font != null) ensureGlyphPipeline(font)
    if (primitives.any { it is UiDrawPrimitive.Texture }) ensureTextureQuadPipeline()
    // ShadowQuad draws through the SAME rounded-quad SDF pipeline -- see stageShadowQuadRun.
    if (primitives.any { it is UiDrawPrimitive.RoundedQuad || it is UiDrawPrimitive.ShadowQuad }) ensureRoundedQuadPipeline()

    val runs = mutableListOf<Renderer.UiRun>()
    var quadRunCount = 0
    var roundedQuadRunCount = 0
    var glyphRunCount = 0
    val activePathClips = ArrayList<UiPath>()
    val clipKindStack = ArrayDeque<ClipKind>()
    // Running intersection of every active clip's safeInteriorRect; null means at least one
    // active clip has no known safe interior, forcing the exact-clip fast path for the whole
    // stack. See canSkipExactClip. Mirrors Vulkan's identical safeInteriorRectStack.
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
                // Chunk by actual vertex/index budget under exact-clip (a clip-cut corner can add
                // vertices beyond the fixed 4-per-quad the fast path assumes), else by primitive
                // count. Mirrors the RoundedQuad/GradientQuad/FilledPath branches below and Vulkan.
                // Safe cast: slice was grouped by first::class above.
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
                        runs += Renderer.UiRun.QuadRun(mesh)
                        quadRunCount += 1
                        chunkStart = chunkEnd
                    }
                }
            }
            is UiDrawPrimitive.GradientQuad -> {
                // See the Quad branch's doc comment for why both the exact-clip and fast paths
                // need chunking, not just the exact-clip one.
                // slice only contains elements matching `first`'s runtime class (see the Quad
                // branch above); `first` was just smart-cast to GradientQuad by this branch.
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
                        runs += Renderer.UiRun.QuadRun(mesh)
                        quadRunCount += 1
                        chunkStart = chunkEnd
                    }
                }
            }
            is UiDrawPrimitive.RoundedQuad -> {
                // Exact-clip case tessellates a real rounded-rect path chunked by vertex/index
                // budget; non-clipped case uses the dedicated SDF pipeline chunked by primitive
                // count. Both need chunking -- a clip-cut corner or enough controls in one
                // rounded-clip container can overflow a single DynamicMesh.
                @Suppress("UNCHECKED_CAST")
                val roundedSlice = slice as List<UiDrawPrimitive.RoundedQuad>
                if (canExactClip(activePathClips)) {
                    val tessellated = roundedSlice.map { quad ->
                        val triangleMesh = UiShapeSpec.RoundedRectangle(quad.radius.px)
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
                        runs += Renderer.UiRun.RoundedQuadRun(mesh)
                        roundedQuadRunCount += 1
                        chunkStart = chunkEnd
                    }
                }
            }
            is UiDrawPrimitive.FilledPath -> {
                // Path tessellation always produces a variable vertex count (unlike Quad's fixed
                // 4), so this needs vertex-budget chunking regardless of whether a clip is active.
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
                // Chunk by actual vertex/index budget, not primitive count: stroke width and
                // path point count both vary the vertex count per primitive, so a fixed-size
                // chunk of primitives can still overflow DynamicMesh's capacity.
                @Suppress("UNCHECKED_CAST")
                val strokedSlice = slice as List<UiDrawPrimitive.StrokedPath>
                val tessellated = strokedSlice.map { tessellateStrokedPath(it, activePathClips, safeInteriorRect) }
                quadRunCount = stageChunkedColoredTriangleMeshes(runs, quadRunCount, tessellated, "stroked-path")
            }
            is UiDrawPrimitive.Glyph -> {
                // Chunk a same-type glyph run into MAX_UI_QUADS-sized sub-runs so a run over
                // MAX_UI_QUADS glyphs doesn't hit stageGlyphRun's capacity guard.
                @Suppress("UNCHECKED_CAST")
                val glyphSlice = slice as List<UiDrawPrimitive.Glyph>
                if (canExactClip(activePathClips)) {
                    // Clip once, then bin into chunks by the actual resulting vertex/index budget
                    // (not glyph count): clipping a glyph quad against a convex path can add
                    // vertices beyond the fixed 4-per-glyph the fast path assumes.
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
                    var chunk = mutableListOf<Pair<UiTexturedTriangleMesh, AwakeColor>>()
                    var chunkVertices = 0
                    var chunkIndices = 0
                    fun flushChunk() {
                        if (chunk.isEmpty()) return
                        val mesh = glyphMeshForRun(glyphRunCount)
                        stageTexturedTriangleMeshes(mesh, chunk, "glyph")
                        runs += Renderer.UiRun.GlyphRun(mesh)
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
                        runs += Renderer.UiRun.GlyphRun(mesh)
                        glyphRunCount += 1
                        chunkStart = chunkEnd
                    }
                }
            }
            is UiDrawPrimitive.Texture -> {
                // slice only contains elements matching `first`'s runtime class (see the Quad
                // branch above); `first` was just smart-cast to Texture by this branch.
                @Suppress("UNCHECKED_CAST")
                runs += Renderer.UiRun.TextureRun(stageTextureRun(slice as List<UiDrawPrimitive.Texture>, activePathClips, safeInteriorRect))
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
                    runs += Renderer.UiRun.ClipRun(it.boundsRect)
                }
            }
            is UiDrawPrimitive.ClipPush -> {
                // Each ClipPush/ClipPop is its own run (never coalesced with a sibling --
                // they're distinct classes, so the same-class grouping above already
                // isolates them one at a time), consumed at the exact point in the paint
                // order they were emitted, same as any other run.
                (slice as List<UiDrawPrimitive.ClipPush>).forEach {
                    clipKindStack.addLast(ClipKind.Rect)
                    runs += Renderer.UiRun.ClipRun(it.rect)
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
                    runs += Renderer.UiRun.ClipRun(it.restoreRect)
                }
            }
            is UiDrawPrimitive.ShadowQuad -> {
                // Mirrors Vulkan's identical branch -- see stageShadowQuadRun.
                @Suppress("UNCHECKED_CAST")
                val shadowSlice = slice as List<UiDrawPrimitive.ShadowQuad>
                var chunkStart = 0
                while (chunkStart < shadowSlice.size) {
                    val chunkEnd = minOf(chunkStart + Renderer.MAX_UI_QUADS, shadowSlice.size)
                    val mesh = roundedQuadMeshForRun(roundedQuadRunCount)
                    stageShadowQuadRun(mesh, shadowSlice.subList(chunkStart, chunkEnd))
                    runs += Renderer.UiRun.RoundedQuadRun(mesh)
                    roundedQuadRunCount += 1
                    chunkStart = chunkEnd
                }
            }
        }
    }
    uiRuns = runs
}

/** Mirrors Vulkan's identical constant -- see its doc comment. */
private val UNBOUNDED_SAFE_INTERIOR_RECT = UiBounds(-1e9f, -1e9f, 2e9f, 2e9f)

/** Mirrors Vulkan's identical `canSkipExactClip` -- see its doc comment. */
private fun canSkipExactClip(safeInteriorRect: UiBounds?, x: Float, y: Float, w: Float, h: Float): Boolean =
    safeInteriorRect != null && safeInteriorRect.contains(UiBounds(x, y, w, h))

/** Writes [quads] (one run's worth) into [mesh] -- extracted from the old single-mesh
 * `drawUi` body, unchanged vertex/index layout. */
private fun stageQuadRun(
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
    mesh.update(vertices, indices)
}

private fun stageGradientQuadRun(
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
    mesh.update(vertices, indices)
}

/** Tessellates+clips a single [UiDrawPrimitive.StrokedPath] -- extracted so the chunked
 * multi-run path in `performDrawUi`'s `StrokedPath` branch (see its doc comment for why
 * chunking is needed) can call this per-primitive instead of tessellating a whole run at once
 * before knowing whether it fits one [DynamicMesh]. */
private fun tessellateStrokedPath(
    primitive: UiDrawPrimitive.StrokedPath,
    activePathClips: List<UiPath>,
    safeInteriorRect: UiBounds?,
): Pair<UiTriangleMesh, AwakeColor> {
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
 * per chunk to [runs] -- mirrors Vulkan's identical `stageChunkedColoredTriangleMeshes`.
 * Returns the updated running `quadRunCount` for the caller to keep threading through its
 * `var`. */
private fun Renderer.stageChunkedColoredTriangleMeshes(
    runs: MutableList<Renderer.UiRun>,
    quadRunCount: Int,
    geometries: List<Pair<UiTriangleMesh, AwakeColor>>,
    label: String,
): Int {
    val maxVertices = Renderer.MAX_UI_QUADS * DynamicMesh.VERTICES_PER_QUAD
    val maxIndices = Renderer.MAX_UI_QUADS * DynamicMesh.INDICES_PER_QUAD
    var count = quadRunCount
    var chunk = mutableListOf<Pair<UiTriangleMesh, AwakeColor>>()
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

/** Per-vertex-colored sibling of [stageChunkedColoredTriangleMeshes] -- mirrors Vulkan's
 * identical `stageChunkedColoredVertexTriangleMeshes`. */
private fun Renderer.stageChunkedColoredVertexTriangleMeshes(
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
    // Split first: the loop below only flushes BETWEEN meshes, so a single oversized mesh
    // would otherwise land in an empty chunk and fail staging's capacity check.
    for (m in meshes.flatMap { it.splitToCapacity(maxVertices, maxIndices) }) {
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

/** Writes [quads] (one run's worth) into [mesh] using the rounded-quad vertex layout --
 * pos(vec2) + localPos(vec2, pixels relative to the quad's own center) + halfSize(vec2) +
 * radius(float) + color(vec4), consumed by `ui_rounded_quad.wgsl`'s distance-field test.
 * Mirrors Vulkan's `stageRoundedQuadRun`. */
private fun stageRoundedQuadRun(mesh: DynamicMesh, quads: List<UiDrawPrimitive.RoundedQuad>) {
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
        writeRoundedQuadVertex(vertices, vertexBase + 0 * floatsPerVertex, quad.x, quad.y, -halfW, -halfH, halfW, halfH, radius, quad.color, quad.transform)
        writeRoundedQuadVertex(vertices, vertexBase + 1 * floatsPerVertex, quad.x + quad.w, quad.y, halfW, -halfH, halfW, halfH, radius, quad.color, quad.transform)
        writeRoundedQuadVertex(vertices, vertexBase + 2 * floatsPerVertex, quad.x + quad.w, quad.y + quad.h, halfW, halfH, halfW, halfH, radius, quad.color, quad.transform)
        writeRoundedQuadVertex(vertices, vertexBase + 3 * floatsPerVertex, quad.x, quad.y + quad.h, -halfW, halfH, halfW, halfH, radius, quad.color, quad.transform)

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
    mesh.update(vertices, indices)
}

/** Writes [shadows] (one run's worth) into [mesh] using the rounded-quad vertex layout, so a
 * drop shadow renders through `ui_rounded_quad.wgsl` unchanged -- no shadow-specific pipeline,
 * shader, or mesh pool. Mirrors Vulkan's `stageShadowQuadRun`; see its doc comment for why
 * pre-dividing the SDF inputs by `blur` turns the shader's fixed 1px antialias band into the
 * soft box-shadow falloff. */
private fun stageShadowQuadRun(mesh: DynamicMesh, shadows: List<UiDrawPrimitive.ShadowQuad>) {
    require(shadows.size <= Renderer.MAX_UI_QUADS) {
        "UI shadow-quad run size (${shadows.size}) exceeds Renderer's DynamicMesh capacity (${Renderer.MAX_UI_QUADS})."
    }
    val floatsPerVertex = DynamicMesh.ROUNDED_QUAD_FLOATS_PER_VERTEX
    val vertices = FloatArray(shadows.size * DynamicMesh.VERTICES_PER_QUAD * floatsPerVertex)
    val indices = IntArray(shadows.size * DynamicMesh.INDICES_PER_QUAD)
    var quadIndex = 0
    while (quadIndex < shadows.size) {
        val shadow = shadows[quadIndex]
        val centerX = shadow.x + shadow.offsetX + shadow.w / 2f
        val centerY = shadow.y + shadow.offsetY + shadow.h / 2f
        val halfW = (shadow.w / 2f + shadow.spread).coerceAtLeast(0f)
        val halfH = (shadow.h / 2f + shadow.spread).coerceAtLeast(0f)
        val radius = (shadow.radius + shadow.spread).coerceIn(0f, minOf(halfW, halfH))
        val blur = shadow.blurRadius.coerceAtLeast(1f)
        val pad = blur + 1f
        val quadHalfW = halfW + pad
        val quadHalfH = halfH + pad
        val localW = quadHalfW / blur
        val localH = quadHalfH / blur
        val sdfHalfW = halfW / blur
        val sdfHalfH = halfH / blur
        val sdfRadius = radius / blur
        val left = centerX - quadHalfW
        val top = centerY - quadHalfH
        val right = centerX + quadHalfW
        val bottom = centerY + quadHalfH
        val vertexBase = quadIndex * DynamicMesh.VERTICES_PER_QUAD * floatsPerVertex
        writeRoundedQuadVertex(vertices, vertexBase + 0 * floatsPerVertex, left, top, -localW, -localH, sdfHalfW, sdfHalfH, sdfRadius, shadow.color)
        writeRoundedQuadVertex(vertices, vertexBase + 1 * floatsPerVertex, right, top, localW, -localH, sdfHalfW, sdfHalfH, sdfRadius, shadow.color)
        writeRoundedQuadVertex(vertices, vertexBase + 2 * floatsPerVertex, right, bottom, localW, localH, sdfHalfW, sdfHalfH, sdfRadius, shadow.color)
        writeRoundedQuadVertex(vertices, vertexBase + 3 * floatsPerVertex, left, bottom, -localW, localH, sdfHalfW, sdfHalfH, sdfRadius, shadow.color)

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
    mesh.update(vertices, indices)
}

private fun stageColoredTriangleMeshes(
    mesh: DynamicMesh,
    geometries: List<Pair<UiTriangleMesh, AwakeColor>>,
    label: String,
) {
    val maxVertices = Renderer.MAX_UI_QUADS * DynamicMesh.VERTICES_PER_QUAD
    val maxIndices = Renderer.MAX_UI_QUADS * DynamicMesh.INDICES_PER_QUAD
    val totalVertices = geometries.sumOf { it.first.points.size }
    val totalIndices = geometries.sumOf { it.first.indices.size }
    require(totalVertices <= maxVertices) {
        "UI $label run vertex count ($totalVertices) exceeds DynamicMesh capacity ($maxVertices). Staging is only reached via a chunker that pre-splits meshes with splitToCapacity(), so this means a caller staged a run directly, or a new mesh source bypassed that split. Route it through stageChunked*TriangleMeshes rather than raising MAX_UI_QUADS."
    }
    require(totalIndices <= maxIndices) {
        "UI $label run index count ($totalIndices) exceeds DynamicMesh capacity ($maxIndices). Staging is only reached via a chunker that pre-splits meshes with splitToCapacity(), so this means a caller staged a run directly, or a new mesh source bypassed that split. Route it through stageChunked*TriangleMeshes rather than raising MAX_UI_QUADS."
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
    mesh.update(vertices, indices)
}

private fun canExactClip(paths: List<UiPath>): Boolean = paths.isNotEmpty() && paths.all { it.convexClipContour() != null }

private fun exactClip(mesh: UiTriangleMesh, activePathClips: List<UiPath>): UiTriangleMesh =
    if (canExactClip(activePathClips)) mesh.clipToConvexPaths(activePathClips) else mesh

private fun exactClip(mesh: UiTexturedTriangleMesh, activePathClips: List<UiPath>): UiTexturedTriangleMesh =
    if (canExactClip(activePathClips)) mesh.clipToConvexPaths(activePathClips) else mesh

private fun exactClipColored(mesh: UiColoredTriangleMesh, activePathClips: List<UiPath>): UiColoredTriangleMesh =
    if (canExactClip(activePathClips)) mesh.clipToConvexPaths(activePathClips) else mesh

/** Per-vertex-colored sibling of [stageColoredTriangleMeshes] -- mirrors Vulkan's
 * `stageColoredVertexTriangleMeshes`: [UiDrawPrimitive.GradientQuad]'s exact-clip path needs
 * each vertex's OWN color, not one flat color per mesh. */
private fun stageColoredVertexTriangleMeshes(
    mesh: DynamicMesh,
    meshes: List<UiColoredTriangleMesh>,
    label: String,
) {
    val maxVertices = Renderer.MAX_UI_QUADS * DynamicMesh.VERTICES_PER_QUAD
    val maxIndices = Renderer.MAX_UI_QUADS * DynamicMesh.INDICES_PER_QUAD
    val totalVertices = meshes.sumOf { it.vertices.size }
    val totalIndices = meshes.sumOf { it.indices.size }
    require(totalVertices <= maxVertices) {
        "UI $label run vertex count ($totalVertices) exceeds DynamicMesh capacity ($maxVertices). Staging is only reached via a chunker that pre-splits meshes with splitToCapacity(), so this means a caller staged a run directly, or a new mesh source bypassed that split. Route it through stageChunked*TriangleMeshes rather than raising MAX_UI_QUADS."
    }
    require(totalIndices <= maxIndices) {
        "UI $label run index count ($totalIndices) exceeds DynamicMesh capacity ($maxIndices). Staging is only reached via a chunker that pre-splits meshes with splitToCapacity(), so this means a caller staged a run directly, or a new mesh source bypassed that split. Route it through stageChunked*TriangleMeshes rather than raising MAX_UI_QUADS."
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
    mesh.update(vertices, indices)
}

/** Writes one run's worth of glyphs into a mesh, clipped against any active path clips. */
private fun stageGlyphRun(
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
    mesh.update(glyphVertices, glyphIndices)
}

private fun stageTextureRun(
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

private fun stageTexturedTriangleMeshes(
    mesh: DynamicMesh,
    geometries: List<Pair<UiTexturedTriangleMesh, AwakeColor>>,
    label: String,
) {
    val maxVertices = Renderer.MAX_UI_QUADS * DynamicMesh.VERTICES_PER_QUAD
    val maxIndices = Renderer.MAX_UI_QUADS * DynamicMesh.INDICES_PER_QUAD
    val totalVertices = geometries.sumOf { it.first.vertices.size }
    val totalIndices = geometries.sumOf { it.first.indices.size }
    require(totalVertices <= maxVertices) {
        "UI $label run vertex count ($totalVertices) exceeds DynamicMesh capacity ($maxVertices). Staging is only reached via a chunker that pre-splits meshes with splitToCapacity(), so this means a caller staged a run directly, or a new mesh source bypassed that split. Route it through stageChunked*TriangleMeshes rather than raising MAX_UI_QUADS."
    }
    require(totalIndices <= maxIndices) {
        "UI $label run index count ($totalIndices) exceeds DynamicMesh capacity ($maxIndices). Staging is only reached via a chunker that pre-splits meshes with splitToCapacity(), so this means a caller staged a run directly, or a new mesh source bypassed that split. Route it through stageChunked*TriangleMeshes rather than raising MAX_UI_QUADS."
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
    mesh.update(vertices, indices)
}

private fun texturedQuadMesh(
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

private fun texturedGeometryBuffers(
    mesh: UiTexturedTriangleMesh,
    color: AwakeColor,
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
