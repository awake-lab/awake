// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.webgpu.renderer

import io.github.ronjunevaldoz.awake.core.colors.Color as AwakeColor
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPath
import io.github.ronjunevaldoz.awake.ui.UiPoint
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.UiTexturedTriangleMesh
import io.github.ronjunevaldoz.awake.ui.UiTexturedVertex
import io.github.ronjunevaldoz.awake.ui.UiTriangleMesh
import io.github.ronjunevaldoz.awake.ui.clipToConvexPaths
import io.github.ronjunevaldoz.awake.ui.convexClipContour
import io.github.ronjunevaldoz.awake.ui.font.UiFont
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.tessellateFill
import io.github.ronjunevaldoz.awake.ui.tessellateStroke
import io.github.ronjunevaldoz.awake.ui.toPath
import io.github.ronjunevaldoz.awake.webgpu.ui.DynamicMesh

/** UI primitive staging -- `performDrawUi` walks a frame's [UiDrawPrimitive] list once,
 * coalescing adjacent same-type entries into runs (see [Renderer.UiRun]'s doc comment) and
 * writing each run's vertex/index data into a pooled [DynamicMesh]. Issues no GPU commands
 * itself -- `performDraw` ([RendererDraw3D.kt]) consumes the staged [Renderer.uiRuns] on the
 * next frame's UI pass. See [Renderer]'s class doc comment for why this lives here as
 * `internal` extension functions rather than as members. */

private enum class ClipKind {
    Rect,
    Path
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
    if (primitives.any { it is UiDrawPrimitive.RoundedQuad }) ensureRoundedQuadPipeline()

    val runs = mutableListOf<Renderer.UiRun>()
    var quadRunCount = 0
    var roundedQuadRunCount = 0
    var glyphRunCount = 0
    val activePathClips = ArrayList<UiPath>()
    val clipKindStack = ArrayDeque<ClipKind>()
    var index = 0
    while (index < primitives.size) {
        val runStart = index
        val first = primitives[runStart]
        index += 1
        while (index < primitives.size && primitives[index]::class == first::class) index += 1
        val slice = primitives.subList(runStart, index)
        when (first) {
            is UiDrawPrimitive.Quad -> {
                @Suppress("UNCHECKED_CAST")
                val mesh = quadMeshForRun(quadRunCount)
                stageQuadRun(mesh, slice as List<UiDrawPrimitive.Quad>, activePathClips)
                runs += Renderer.UiRun.QuadRun(mesh)
                quadRunCount += 1
            }
            is UiDrawPrimitive.GradientQuad -> {
                @Suppress("UNCHECKED_CAST")
                val mesh = quadMeshForRun(quadRunCount)
                stageGradientQuadRun(mesh, slice as List<UiDrawPrimitive.GradientQuad>)
                runs += Renderer.UiRun.QuadRun(mesh)
                quadRunCount += 1
            }
            is UiDrawPrimitive.RoundedQuad -> {
                // Exact-clip case: tessellate a real rounded-rect path (same as the
                // FilledPath/StrokedPath branches). Non-clipped case: dedicated SDF
                // UiRoundedQuadRenderPipeline (mirrors Vulkan's uiRoundedQuadRenderPipeline)
                // instead of the old flat-Quad fallback that dropped the radius entirely.
                @Suppress("UNCHECKED_CAST")
                val roundedSlice = slice as List<UiDrawPrimitive.RoundedQuad>
                if (canExactClip(activePathClips)) {
                    val mesh = quadMeshForRun(quadRunCount)
                    stageRoundedQuadFillRun(mesh, roundedSlice, activePathClips)
                    runs += Renderer.UiRun.QuadRun(mesh)
                    quadRunCount += 1
                } else {
                    val mesh = roundedQuadMeshForRun(roundedQuadRunCount)
                    stageRoundedQuadRun(mesh, roundedSlice)
                    runs += Renderer.UiRun.RoundedQuadRun(mesh)
                    roundedQuadRunCount += 1
                }
            }
            is UiDrawPrimitive.FilledPath -> {
                @Suppress("UNCHECKED_CAST")
                val mesh = quadMeshForRun(quadRunCount)
                stageFilledPathRun(mesh, slice as List<UiDrawPrimitive.FilledPath>, activePathClips)
                runs += Renderer.UiRun.QuadRun(mesh)
                quadRunCount += 1
            }
            is UiDrawPrimitive.StrokedPath -> {
                @Suppress("UNCHECKED_CAST")
                val mesh = quadMeshForRun(quadRunCount)
                stageStrokedPathRun(mesh, slice as List<UiDrawPrimitive.StrokedPath>, activePathClips)
                runs += Renderer.UiRun.QuadRun(mesh)
                quadRunCount += 1
            }
            is UiDrawPrimitive.Glyph -> {
                // Chunk a same-type glyph run into MAX_UI_QUADS-sized sub-runs -- mirrors
                // Vulkan's Renderer.performDrawUi() Glyph branch, so a single contiguous
                // glyph run over MAX_UI_QUADS glyphs doesn't hit stageGlyphRun's
                // require(glyphs.size <= MAX_UI_QUADS) guard and throw.
                @Suppress("UNCHECKED_CAST")
                val glyphSlice = slice as List<UiDrawPrimitive.Glyph>
                var chunkStart = 0
                while (chunkStart < glyphSlice.size) {
                    val chunkEnd = minOf(chunkStart + Renderer.MAX_UI_QUADS, glyphSlice.size)
                    val mesh = glyphMeshForRun(glyphRunCount)
                    stageGlyphRun(mesh, glyphSlice.subList(chunkStart, chunkEnd), activePathClips)
                    runs += Renderer.UiRun.GlyphRun(mesh)
                    glyphRunCount += 1
                    chunkStart = chunkEnd
                }
            }
            is UiDrawPrimitive.Texture -> {
                @Suppress("UNCHECKED_CAST")
                runs += Renderer.UiRun.TextureRun(stageTextureRun(slice as List<UiDrawPrimitive.Texture>, activePathClips))
            }
            is UiDrawPrimitive.ClipPathPush -> {
                // Keep the resolved bounds scissor even when exact convex path clipping
                // is available: it stays the fallback for non-convex paths and still
                // trims work outside the path's enclosing rect.
                (slice as List<UiDrawPrimitive.ClipPathPush>).forEach {
                    clipKindStack.addLast(ClipKind.Path)
                    activePathClips += it.path
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
                        ClipKind.Path -> if (activePathClips.isNotEmpty()) activePathClips.removeAt(activePathClips.lastIndex)
                        ClipKind.Rect, null -> Unit
                    }
                    runs += Renderer.UiRun.ClipRun(it.restoreRect)
                }
            }
        }
    }
    uiRuns = runs
}

/** Writes [quads] (one run's worth) into [mesh] -- extracted from the old single-mesh
 * `drawUi` body, unchanged vertex/index layout. */
private fun stageQuadRun(mesh: DynamicMesh, quads: List<UiDrawPrimitive.Quad>, activePathClips: List<UiPath> = emptyList()) {
    if (canExactClip(activePathClips)) {
        stageColoredTriangleMeshes(
            mesh,
            quads.map { quad ->
                exactClip(
                    UiTriangleMesh(
                        points = listOf(
                            UiPoint(quad.x, quad.y),
                            UiPoint(quad.x + quad.w, quad.y),
                            UiPoint(quad.x + quad.w, quad.y + quad.h),
                            UiPoint(quad.x, quad.y + quad.h)
                        ),
                        indices = intArrayOf(0, 1, 2, 2, 3, 0)
                    ),
                    activePathClips
                ) to quad.color
            },
            "quad"
        )
        return
    }
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

private fun stageFilledPathRun(mesh: DynamicMesh, paths: List<UiDrawPrimitive.FilledPath>, activePathClips: List<UiPath>) {
    val tessellated = paths.map { it to exactClip(it.path.tessellateFill(), activePathClips) }
    stageColoredTriangleMeshes(mesh, tessellated.map { (primitive, triangleMesh) -> triangleMesh to primitive.color }, "filled-path")
}

private fun stageGradientQuadRun(mesh: DynamicMesh, quads: List<UiDrawPrimitive.GradientQuad>) {
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

private fun stageStrokedPathRun(mesh: DynamicMesh, paths: List<UiDrawPrimitive.StrokedPath>, activePathClips: List<UiPath>) {
    val tessellated = paths.map { it to exactClip(it.path.tessellateStroke(it.stroke), activePathClips) }
    stageColoredTriangleMeshes(mesh, tessellated.map { (primitive, triangleMesh) -> triangleMesh to primitive.color }, "stroked-path")
}

private fun stageRoundedQuadFillRun(mesh: DynamicMesh, quads: List<UiDrawPrimitive.RoundedQuad>, activePathClips: List<UiPath>) {
    stageColoredTriangleMeshes(
        mesh,
        quads.map { quad ->
            exactClip(
                UiShapeSpec.RoundedRectangle(quad.radius.px).toPath(UiBounds(quad.x, quad.y, quad.w, quad.h)).tessellateFill(),
                activePathClips
            ) to quad.color
        },
        "rounded-quad-clipped"
    )
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

private fun stageColoredTriangleMeshes(
    mesh: DynamicMesh,
    geometries: List<Pair<UiTriangleMesh, AwakeColor>>,
    label: String
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
    mesh.update(vertices, indices)
}

private fun canExactClip(paths: List<UiPath>): Boolean = paths.isNotEmpty() && paths.all { it.convexClipContour() != null }

private fun exactClip(mesh: UiTriangleMesh, activePathClips: List<UiPath>): UiTriangleMesh =
    if (canExactClip(activePathClips)) mesh.clipToConvexPaths(activePathClips) else mesh

private fun exactClip(mesh: UiTexturedTriangleMesh, activePathClips: List<UiPath>): UiTexturedTriangleMesh =
    if (canExactClip(activePathClips)) mesh.clipToConvexPaths(activePathClips) else mesh

/** Writes [glyphs] (one run's worth) into [mesh] -- extracted from the old single-mesh
 * `drawUi` body, unchanged vertex/index layout unless exact path clipping is active. */
private fun stageGlyphRun(mesh: DynamicMesh, glyphs: List<UiDrawPrimitive.Glyph>, activePathClips: List<UiPath> = emptyList()) {
    if (canExactClip(activePathClips)) {
        stageTexturedTriangleMeshes(
            mesh,
            glyphs.map { glyph ->
                exactClip(
                    texturedQuadMesh(glyph.x, glyph.y, glyph.w, glyph.h, glyph.u0, glyph.v0, glyph.u1, glyph.v1),
                    activePathClips
                ) to glyph.color
            },
            "glyph"
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

private fun stageTextureRun(textures: List<UiDrawPrimitive.Texture>, activePathClips: List<UiPath>): List<Renderer.TexturedPrimitiveRun> =
    textures.map { primitive ->
        val clipped = exactClip(texturedQuadMesh(primitive.x, primitive.y, primitive.w, primitive.h), activePathClips)
        val (vertices, indices) = texturedGeometryBuffers(clipped, Renderer.WHITE_RGBA, primitive.transform)
        Renderer.TexturedPrimitiveRun(primitive.material, vertices, indices)
    }

private fun stageTexturedTriangleMeshes(
    mesh: DynamicMesh,
    geometries: List<Pair<UiTexturedTriangleMesh, AwakeColor>>,
    label: String
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
    v1: Float = 1f
): UiTexturedTriangleMesh = UiTexturedTriangleMesh(
    vertices = listOf(
        UiTexturedVertex(UiPoint(x, y), u0, v0),
        UiTexturedVertex(UiPoint(x + w, y), u1, v0),
        UiTexturedVertex(UiPoint(x + w, y + h), u1, v1),
        UiTexturedVertex(UiPoint(x, y + h), u0, v1)
    ),
    indices = intArrayOf(0, 1, 2, 2, 3, 0)
)

private fun texturedGeometryBuffers(
    mesh: UiTexturedTriangleMesh,
    color: AwakeColor,
    transform: io.github.ronjunevaldoz.awake.ui.UiPrimitiveTransform? = null
): Pair<FloatArray, IntArray> {
    val vertices = FloatArray(mesh.vertices.size * DynamicMesh.GLYPH_FLOATS_PER_VERTEX)
    var offset = 0
    mesh.vertices.forEach { vertex ->
        writeGlyphVertex(vertices, offset, vertex.position.x, vertex.position.y, vertex.u, vertex.v, color, transform)
        offset += DynamicMesh.GLYPH_FLOATS_PER_VERTEX
    }
    return vertices to mesh.indices
}
