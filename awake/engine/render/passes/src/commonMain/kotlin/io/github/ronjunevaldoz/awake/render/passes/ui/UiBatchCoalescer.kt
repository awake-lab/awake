// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.render.passes.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiColoredTriangleMesh
import io.github.ronjunevaldoz.awake.ui.UiColoredVertex
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPath
import io.github.ronjunevaldoz.awake.ui.UiPoint
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveTransform
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec.RoundedRectangle
import io.github.ronjunevaldoz.awake.ui.UiTexturedTriangleMesh
import io.github.ronjunevaldoz.awake.ui.UiTexturedVertex
import io.github.ronjunevaldoz.awake.ui.UiTriangleMesh
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.contains
import io.github.ronjunevaldoz.awake.ui.api.layout.intersect
import io.github.ronjunevaldoz.awake.ui.bounds
import io.github.ronjunevaldoz.awake.ui.clipToConvexPaths
import io.github.ronjunevaldoz.awake.ui.convexClipContour
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.splitToCapacity
import io.github.ronjunevaldoz.awake.ui.tessellateFillAa
import io.github.ronjunevaldoz.awake.ui.tessellateStroke
import io.github.ronjunevaldoz.awake.ui.toPath
import io.github.ronjunevaldoz.awake.ui.toPx

/**
 * Coalesces raw [UiDrawPrimitive] lists into typed [UiStagedRun] batches, handling scissor stacks,
 * convex path clipping, AA tessellation, and vertex/index capacity chunking.
 */
object UiBatchCoalescer {

    private enum class ClipKind {
        Rect,
        Path,
    }

    private val UNBOUNDED_SAFE_INTERIOR_RECT = UiBounds(-1e9f, -1e9f, 2e9f, 2e9f)
    private val WHITE_COLOR = Color(1f, 1f, 1f, 1f)

    /**
     * Walks [primitives] in paint order and coalesces them into [UiStagedRun] instances.
     *
     * @param primitives The raw UI draw primitives emitted by the UI framework.
     * @param maxQuadsPerBatch The maximum number of quads allowed per single batch/mesh chunk.
     */
    fun coalesce(
        primitives: List<UiDrawPrimitive>,
        maxQuadsPerBatch: Int = 1024,
    ): List<UiStagedRun> {
        val runs = mutableListOf<UiStagedRun>()
        val activePathClips = ArrayList<UiPath>()
        val clipKindStack = ArrayDeque<ClipKind>()
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
                        chunkColoredTriangleMeshes(runs, tessellated, maxQuadsPerBatch)
                    } else {
                        var chunkStart = 0
                        while (chunkStart < quadSlice.size) {
                            val chunkEnd = minOf(chunkStart + maxQuadsPerBatch, quadSlice.size)
                            runs += buildQuadRun(quadSlice.subList(chunkStart, chunkEnd))
                            chunkStart = chunkEnd
                        }
                    }
                }
                is UiDrawPrimitive.GradientQuad -> {
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
                        chunkColoredVertexTriangleMeshes(runs, tessellated, maxQuadsPerBatch)
                    } else {
                        var chunkStart = 0
                        while (chunkStart < gradientSlice.size) {
                            val chunkEnd = minOf(chunkStart + maxQuadsPerBatch, gradientSlice.size)
                            runs += buildGradientQuadRun(gradientSlice.subList(chunkStart, chunkEnd))
                            chunkStart = chunkEnd
                        }
                    }
                }
                is UiDrawPrimitive.RoundedQuad -> {
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
                        chunkColoredVertexTriangleMeshes(runs, tessellated, maxQuadsPerBatch)
                    } else {
                        var chunkStart = 0
                        while (chunkStart < roundedSlice.size) {
                            val chunkEnd = minOf(chunkStart + maxQuadsPerBatch, roundedSlice.size)
                            runs += buildRoundedQuadRun(roundedSlice.subList(chunkStart, chunkEnd))
                            chunkStart = chunkEnd
                        }
                    }
                }
                is UiDrawPrimitive.FilledPath -> {
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
                    chunkColoredVertexTriangleMeshes(runs, tessellated, maxQuadsPerBatch)
                }
                is UiDrawPrimitive.StrokedPath -> {
                    @Suppress("UNCHECKED_CAST")
                    val strokedSlice = slice as List<UiDrawPrimitive.StrokedPath>
                    val tessellated = strokedSlice.map { tessellateStrokedPath(it, activePathClips, safeInteriorRect) }
                    chunkColoredTriangleMeshes(runs, tessellated, maxQuadsPerBatch)
                }
                is UiDrawPrimitive.Glyph -> {
                    @Suppress("UNCHECKED_CAST")
                    val glyphSlice = slice as List<UiDrawPrimitive.Glyph>
                    if (canExactClip(activePathClips)) {
                        val clipped = glyphSlice.map { glyph ->
                            val raw = texturedQuadMesh(glyph.x, glyph.y, glyph.w, glyph.h, glyph.u0, glyph.v0, glyph.u1, glyph.v1)
                            val mesh = if (canSkipExactClip(safeInteriorRect, glyph.x, glyph.y, glyph.w, glyph.h)) {
                                raw
                            } else {
                                exactClip(raw, activePathClips)
                            }
                            mesh to glyph.color
                        }
                        chunkTexturedTriangleMeshes(runs, clipped, maxQuadsPerBatch)
                    } else {
                        var chunkStart = 0
                        while (chunkStart < glyphSlice.size) {
                            val chunkEnd = minOf(chunkStart + maxQuadsPerBatch, glyphSlice.size)
                            runs += buildGlyphRun(glyphSlice.subList(chunkStart, chunkEnd))
                            chunkStart = chunkEnd
                        }
                    }
                }
                is UiDrawPrimitive.Texture -> {
                    @Suppress("UNCHECKED_CAST")
                    val textureSlice = slice as List<UiDrawPrimitive.Texture>
                    runs += buildTextureRun(textureSlice, activePathClips, safeInteriorRect)
                }
                is UiDrawPrimitive.ClipPathPush -> {
                    @Suppress("UNCHECKED_CAST")
                    (slice as List<UiDrawPrimitive.ClipPathPush>).forEach {
                        clipKindStack.addLast(ClipKind.Path)
                        activePathClips += it.path
                        val parentSafeInteriorRect = safeInteriorRectStack.lastOrNull() ?: UNBOUNDED_SAFE_INTERIOR_RECT
                        safeInteriorRectStack.addLast(
                            it.safeInteriorRect?.let { own -> parentSafeInteriorRect.intersect(own) },
                        )
                        runs += UiStagedRun.ClipRun(it.boundsRect)
                    }
                }
                is UiDrawPrimitive.ClipPush -> {
                    @Suppress("UNCHECKED_CAST")
                    (slice as List<UiDrawPrimitive.ClipPush>).forEach {
                        clipKindStack.addLast(ClipKind.Rect)
                        runs += UiStagedRun.ClipRun(it.rect)
                    }
                }
                is UiDrawPrimitive.ClipPop -> {
                    @Suppress("UNCHECKED_CAST")
                    (slice as List<UiDrawPrimitive.ClipPop>).forEach {
                        when (clipKindStack.removeLastOrNull()) {
                            ClipKind.Path -> {
                                if (activePathClips.isNotEmpty()) activePathClips.removeAt(activePathClips.lastIndex)
                                safeInteriorRectStack.removeLastOrNull()
                            }
                            ClipKind.Rect, null -> Unit
                        }
                        runs += UiStagedRun.ClipRun(it.restoreRect)
                    }
                }
                is UiDrawPrimitive.ShadowQuad -> {
                    @Suppress("UNCHECKED_CAST")
                    val shadowSlice = slice as List<UiDrawPrimitive.ShadowQuad>
                    var chunkStart = 0
                    while (chunkStart < shadowSlice.size) {
                        val chunkEnd = minOf(chunkStart + maxQuadsPerBatch, shadowSlice.size)
                        runs += buildShadowQuadRun(shadowSlice.subList(chunkStart, chunkEnd))
                        chunkStart = chunkEnd
                    }
                }
            }
        }
        return runs
    }

    fun canSkipExactClip(safeInteriorRect: UiBounds?, x: Float, y: Float, w: Float, h: Float): Boolean =
        safeInteriorRect != null && safeInteriorRect.contains(UiBounds(x, y, w, h))

    fun canExactClip(paths: List<UiPath>): Boolean = paths.isNotEmpty() && paths.all { it.convexClipContour() != null }

    fun exactClip(mesh: UiTriangleMesh, activePathClips: List<UiPath>): UiTriangleMesh =
        if (canExactClip(activePathClips)) mesh.clipToConvexPaths(activePathClips) else mesh

    fun exactClip(mesh: UiTexturedTriangleMesh, activePathClips: List<UiPath>): UiTexturedTriangleMesh =
        if (canExactClip(activePathClips)) mesh.clipToConvexPaths(activePathClips) else mesh

    fun exactClipColored(mesh: UiColoredTriangleMesh, activePathClips: List<UiPath>): UiColoredTriangleMesh =
        if (canExactClip(activePathClips)) mesh.clipToConvexPaths(activePathClips) else mesh

    fun buildQuadRun(quads: List<UiDrawPrimitive.Quad>): UiStagedRun.QuadRun {
        val vertices = FloatArray(quads.size * UiVertexLayout.VERTICES_PER_QUAD * UiVertexLayout.FLOATS_PER_VERTEX)
        val indices = IntArray(quads.size * UiVertexLayout.INDICES_PER_QUAD)
        var quadIndex = 0
        while (quadIndex < quads.size) {
            val quad = quads[quadIndex]
            val vertexBase = quadIndex * UiVertexLayout.VERTICES_PER_QUAD * UiVertexLayout.FLOATS_PER_VERTEX
            writeVertex(vertices, vertexBase + 0 * UiVertexLayout.FLOATS_PER_VERTEX, quad.x, quad.y, quad.color, quad.transform)
            writeVertex(vertices, vertexBase + 1 * UiVertexLayout.FLOATS_PER_VERTEX, quad.x + quad.w, quad.y, quad.color, quad.transform)
            writeVertex(vertices, vertexBase + 2 * UiVertexLayout.FLOATS_PER_VERTEX, quad.x + quad.w, quad.y + quad.h, quad.color, quad.transform)
            writeVertex(vertices, vertexBase + 3 * UiVertexLayout.FLOATS_PER_VERTEX, quad.x, quad.y + quad.h, quad.color, quad.transform)

            val vertexOffset = quadIndex * UiVertexLayout.VERTICES_PER_QUAD
            val indexBase = quadIndex * UiVertexLayout.INDICES_PER_QUAD
            indices[indexBase] = vertexOffset
            indices[indexBase + 1] = vertexOffset + 1
            indices[indexBase + 2] = vertexOffset + 2
            indices[indexBase + 3] = vertexOffset + 2
            indices[indexBase + 4] = vertexOffset + 3
            indices[indexBase + 5] = vertexOffset
            quadIndex += 1
        }
        return UiStagedRun.QuadRun(vertices, indices)
    }

    fun buildGradientQuadRun(quads: List<UiDrawPrimitive.GradientQuad>): UiStagedRun.QuadRun {
        val vertices = FloatArray(quads.size * UiVertexLayout.VERTICES_PER_QUAD * UiVertexLayout.FLOATS_PER_VERTEX)
        val indices = IntArray(quads.size * UiVertexLayout.INDICES_PER_QUAD)
        var quadIndex = 0
        while (quadIndex < quads.size) {
            val quad = quads[quadIndex]
            val vertexBase = quadIndex * UiVertexLayout.VERTICES_PER_QUAD * UiVertexLayout.FLOATS_PER_VERTEX
            writeVertex(vertices, vertexBase + 0 * UiVertexLayout.FLOATS_PER_VERTEX, quad.x, quad.y, quad.gradient.topLeft)
            writeVertex(vertices, vertexBase + 1 * UiVertexLayout.FLOATS_PER_VERTEX, quad.x + quad.w, quad.y, quad.gradient.topRight)
            writeVertex(vertices, vertexBase + 2 * UiVertexLayout.FLOATS_PER_VERTEX, quad.x + quad.w, quad.y + quad.h, quad.gradient.bottomRight)
            writeVertex(vertices, vertexBase + 3 * UiVertexLayout.FLOATS_PER_VERTEX, quad.x, quad.y + quad.h, quad.gradient.bottomLeft)

            val vertexOffset = quadIndex * UiVertexLayout.VERTICES_PER_QUAD
            val indexBase = quadIndex * UiVertexLayout.INDICES_PER_QUAD
            indices[indexBase] = vertexOffset
            indices[indexBase + 1] = vertexOffset + 1
            indices[indexBase + 2] = vertexOffset + 2
            indices[indexBase + 3] = vertexOffset + 2
            indices[indexBase + 4] = vertexOffset + 3
            indices[indexBase + 5] = vertexOffset
            quadIndex += 1
        }
        return UiStagedRun.QuadRun(vertices, indices)
    }

    fun buildRoundedQuadRun(quads: List<UiDrawPrimitive.RoundedQuad>): UiStagedRun.RoundedQuadRun {
        val floatsPerVertex = UiVertexLayout.ROUNDED_QUAD_FLOATS_PER_VERTEX
        val vertices = FloatArray(quads.size * UiVertexLayout.VERTICES_PER_QUAD * floatsPerVertex)
        val indices = IntArray(quads.size * UiVertexLayout.INDICES_PER_QUAD)
        var quadIndex = 0
        while (quadIndex < quads.size) {
            val quad = quads[quadIndex]
            val halfW = quad.w / 2f
            val halfH = quad.h / 2f
            val radius = quad.radius.coerceAtMost(minOf(halfW, halfH))
            val vertexBase = quadIndex * UiVertexLayout.VERTICES_PER_QUAD * floatsPerVertex
            writeRoundedQuadVertex(vertices, vertexBase + 0 * floatsPerVertex, quad.x, quad.y, -halfW, -halfH, halfW, halfH, radius, quad.smoothing, quad.color, quad.transform)
            writeRoundedQuadVertex(vertices, vertexBase + 1 * floatsPerVertex, quad.x + quad.w, quad.y, halfW, -halfH, halfW, halfH, radius, quad.smoothing, quad.color, quad.transform)
            writeRoundedQuadVertex(vertices, vertexBase + 2 * floatsPerVertex, quad.x + quad.w, quad.y + quad.h, halfW, halfH, halfW, halfH, radius, quad.smoothing, quad.color, quad.transform)
            writeRoundedQuadVertex(vertices, vertexBase + 3 * floatsPerVertex, quad.x, quad.y + quad.h, -halfW, halfH, halfW, halfH, radius, quad.smoothing, quad.color, quad.transform)

            val vertexOffset = quadIndex * UiVertexLayout.VERTICES_PER_QUAD
            val indexBase = quadIndex * UiVertexLayout.INDICES_PER_QUAD
            indices[indexBase] = vertexOffset
            indices[indexBase + 1] = vertexOffset + 1
            indices[indexBase + 2] = vertexOffset + 2
            indices[indexBase + 3] = vertexOffset + 2
            indices[indexBase + 4] = vertexOffset + 3
            indices[indexBase + 5] = vertexOffset
            quadIndex += 1
        }
        return UiStagedRun.RoundedQuadRun(vertices, indices)
    }

    fun buildShadowQuadRun(shadows: List<UiDrawPrimitive.ShadowQuad>): UiStagedRun.RoundedQuadRun {
        val floatsPerVertex = UiVertexLayout.ROUNDED_QUAD_FLOATS_PER_VERTEX
        val vertices = FloatArray(shadows.size * UiVertexLayout.VERTICES_PER_QUAD * floatsPerVertex)
        val indices = IntArray(shadows.size * UiVertexLayout.INDICES_PER_QUAD)
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
            val vertexBase = quadIndex * UiVertexLayout.VERTICES_PER_QUAD * floatsPerVertex
            writeRoundedQuadVertex(vertices, vertexBase + 0 * floatsPerVertex, left, top, -localW, -localH, sdfHalfW, sdfHalfH, sdfRadius, 0f, shadow.color)
            writeRoundedQuadVertex(vertices, vertexBase + 1 * floatsPerVertex, right, top, localW, -localH, sdfHalfW, sdfHalfH, sdfRadius, 0f, shadow.color)
            writeRoundedQuadVertex(vertices, vertexBase + 2 * floatsPerVertex, right, bottom, localW, localH, sdfHalfW, sdfHalfH, sdfRadius, 0f, shadow.color)
            writeRoundedQuadVertex(vertices, vertexBase + 3 * floatsPerVertex, left, bottom, -localW, localH, sdfHalfW, sdfHalfH, sdfRadius, 0f, shadow.color)

            val vertexOffset = quadIndex * UiVertexLayout.VERTICES_PER_QUAD
            val indexBase = quadIndex * UiVertexLayout.INDICES_PER_QUAD
            indices[indexBase] = vertexOffset
            indices[indexBase + 1] = vertexOffset + 1
            indices[indexBase + 2] = vertexOffset + 2
            indices[indexBase + 3] = vertexOffset + 2
            indices[indexBase + 4] = vertexOffset + 3
            indices[indexBase + 5] = vertexOffset
            quadIndex += 1
        }
        return UiStagedRun.RoundedQuadRun(vertices, indices)
    }

    fun buildGlyphRun(glyphs: List<UiDrawPrimitive.Glyph>): UiStagedRun.GlyphRun {
        val glyphVertices = FloatArray(glyphs.size * UiVertexLayout.VERTICES_PER_QUAD * UiVertexLayout.GLYPH_FLOATS_PER_VERTEX)
        val glyphIndices = IntArray(glyphs.size * UiVertexLayout.INDICES_PER_QUAD)
        var glyphIndex = 0
        while (glyphIndex < glyphs.size) {
            val glyph = glyphs[glyphIndex]
            val vertexBase = glyphIndex * UiVertexLayout.VERTICES_PER_QUAD * UiVertexLayout.GLYPH_FLOATS_PER_VERTEX
            writeGlyphVertex(glyphVertices, vertexBase + 0 * UiVertexLayout.GLYPH_FLOATS_PER_VERTEX, glyph.x, glyph.y, glyph.u0, glyph.v0, glyph.color, glyph.transform)
            writeGlyphVertex(glyphVertices, vertexBase + 1 * UiVertexLayout.GLYPH_FLOATS_PER_VERTEX, glyph.x + glyph.w, glyph.y, glyph.u1, glyph.v0, glyph.color, glyph.transform)
            writeGlyphVertex(glyphVertices, vertexBase + 2 * UiVertexLayout.GLYPH_FLOATS_PER_VERTEX, glyph.x + glyph.w, glyph.y + glyph.h, glyph.u1, glyph.v1, glyph.color, glyph.transform)
            writeGlyphVertex(glyphVertices, vertexBase + 3 * UiVertexLayout.GLYPH_FLOATS_PER_VERTEX, glyph.x, glyph.y + glyph.h, glyph.u0, glyph.v1, glyph.color, glyph.transform)

            val vertexOffset = glyphIndex * UiVertexLayout.VERTICES_PER_QUAD
            val indexBase = glyphIndex * UiVertexLayout.INDICES_PER_QUAD
            glyphIndices[indexBase] = vertexOffset
            glyphIndices[indexBase + 1] = vertexOffset + 1
            glyphIndices[indexBase + 2] = vertexOffset + 2
            glyphIndices[indexBase + 3] = vertexOffset + 2
            glyphIndices[indexBase + 4] = vertexOffset + 3
            glyphIndices[indexBase + 5] = vertexOffset
            glyphIndex += 1
        }
        return UiStagedRun.GlyphRun(glyphVertices, glyphIndices)
    }

    fun buildTextureRun(
        textures: List<UiDrawPrimitive.Texture>,
        activePathClips: List<UiPath>,
        safeInteriorRect: UiBounds? = null,
    ): UiStagedRun.TextureRun {
        val primitives = textures.map { primitive ->
            val raw = texturedQuadMesh(primitive.x, primitive.y, primitive.w, primitive.h)
            val clipped = if (canSkipExactClip(safeInteriorRect, primitive.x, primitive.y, primitive.w, primitive.h)) {
                raw
            } else {
                exactClip(raw, activePathClips)
            }
            val (vertices, indices) = texturedGeometryBuffers(clipped, WHITE_COLOR, primitive.transform)
            TexturedPrimitiveRun(primitive.material, vertices, indices)
        }
        return UiStagedRun.TextureRun(primitives)
    }

    fun tessellateStrokedPath(
        primitive: UiDrawPrimitive.StrokedPath,
        activePathClips: List<UiPath>,
        safeInteriorRect: UiBounds?,
    ): Pair<UiTriangleMesh, Color> {
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

    fun texturedQuadMesh(
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

    fun texturedGeometryBuffers(
        mesh: UiTexturedTriangleMesh,
        color: Color,
        transform: UiPrimitiveTransform? = null,
    ): Pair<FloatArray, IntArray> {
        val vertices = FloatArray(mesh.vertices.size * UiVertexLayout.GLYPH_FLOATS_PER_VERTEX)
        var offset = 0
        mesh.vertices.forEach { vertex ->
            writeGlyphVertex(vertices, offset, vertex.position.x, vertex.position.y, vertex.u, vertex.v, color, transform)
            offset += UiVertexLayout.GLYPH_FLOATS_PER_VERTEX
        }
        return vertices to mesh.indices
    }

    private fun chunkColoredTriangleMeshes(
        runs: MutableList<UiStagedRun>,
        geometries: List<Pair<UiTriangleMesh, Color>>,
        maxQuads: Int,
    ) {
        val maxVertices = maxQuads * UiVertexLayout.VERTICES_PER_QUAD
        val maxIndices = maxQuads * UiVertexLayout.INDICES_PER_QUAD
        var chunk = mutableListOf<Pair<UiTriangleMesh, Color>>()
        var chunkVertices = 0
        var chunkIndices = 0

        fun flushChunk() {
            if (chunk.isEmpty()) return
            runs += stageColoredTriangleMeshesToRun(chunk)
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
    }

    private fun chunkColoredVertexTriangleMeshes(
        runs: MutableList<UiStagedRun>,
        meshes: List<UiColoredTriangleMesh>,
        maxQuads: Int,
    ) {
        val maxVertices = maxQuads * UiVertexLayout.VERTICES_PER_QUAD
        val maxIndices = maxQuads * UiVertexLayout.INDICES_PER_QUAD
        var chunk = mutableListOf<UiColoredTriangleMesh>()
        var chunkVertices = 0
        var chunkIndices = 0

        fun flushChunk() {
            if (chunk.isEmpty()) return
            runs += stageColoredVertexTriangleMeshesToRun(chunk)
            chunk = mutableListOf()
            chunkVertices = 0
            chunkIndices = 0
        }

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
    }

    private fun chunkTexturedTriangleMeshes(
        runs: MutableList<UiStagedRun>,
        geometries: List<Pair<UiTexturedTriangleMesh, Color>>,
        maxQuads: Int,
    ) {
        val maxVertices = maxQuads * UiVertexLayout.VERTICES_PER_QUAD
        val maxIndices = maxQuads * UiVertexLayout.INDICES_PER_QUAD
        var chunk = mutableListOf<Pair<UiTexturedTriangleMesh, Color>>()
        var chunkVertices = 0
        var chunkIndices = 0

        fun flushChunk() {
            if (chunk.isEmpty()) return
            runs += stageTexturedTriangleMeshesToRun(chunk)
            chunk = mutableListOf()
            chunkVertices = 0
            chunkIndices = 0
        }

        for (pair in geometries) {
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
    }

    private fun stageColoredTriangleMeshesToRun(geometries: List<Pair<UiTriangleMesh, Color>>): UiStagedRun.QuadRun {
        val totalVertices = geometries.sumOf { it.first.points.size }
        val totalIndices = geometries.sumOf { it.first.indices.size }
        val vertices = FloatArray(totalVertices * UiVertexLayout.FLOATS_PER_VERTEX)
        val indices = IntArray(totalIndices)
        var vertexCursor = 0
        var indexCursor = 0
        var vertexOffset = 0

        geometries.forEach { (triangleMesh, color) ->
            triangleMesh.points.forEach { point ->
                writeVertex(vertices, vertexCursor, point.x, point.y, color)
                vertexCursor += UiVertexLayout.FLOATS_PER_VERTEX
            }
            triangleMesh.indices.forEach { index ->
                indices[indexCursor] = vertexOffset + index
                indexCursor += 1
            }
            vertexOffset += triangleMesh.points.size
        }
        return UiStagedRun.QuadRun(vertices, indices)
    }

    private fun stageColoredVertexTriangleMeshesToRun(meshes: List<UiColoredTriangleMesh>): UiStagedRun.QuadRun {
        val totalVertices = meshes.sumOf { it.vertices.size }
        val totalIndices = meshes.sumOf { it.indices.size }
        val vertices = FloatArray(totalVertices * UiVertexLayout.FLOATS_PER_VERTEX)
        val indices = IntArray(totalIndices)
        var vertexCursor = 0
        var indexCursor = 0
        var vertexOffset = 0

        meshes.forEach { triangleMesh ->
            triangleMesh.vertices.forEach { vertex ->
                writeVertex(vertices, vertexCursor, vertex.position.x, vertex.position.y, vertex.color)
                vertexCursor += UiVertexLayout.FLOATS_PER_VERTEX
            }
            triangleMesh.indices.forEach { index ->
                indices[indexCursor] = vertexOffset + index
                indexCursor += 1
            }
            vertexOffset += triangleMesh.vertices.size
        }
        return UiStagedRun.QuadRun(vertices, indices)
    }

    private fun stageTexturedTriangleMeshesToRun(geometries: List<Pair<UiTexturedTriangleMesh, Color>>): UiStagedRun.GlyphRun {
        val totalVertices = geometries.sumOf { it.first.vertices.size }
        val totalIndices = geometries.sumOf { it.first.indices.size }
        val vertices = FloatArray(totalVertices * UiVertexLayout.GLYPH_FLOATS_PER_VERTEX)
        val indices = IntArray(totalIndices)
        var vertexCursor = 0
        var indexCursor = 0
        var vertexOffset = 0

        geometries.forEach { (triangleMesh, color) ->
            triangleMesh.vertices.forEach { vertex ->
                writeGlyphVertex(vertices, vertexCursor, vertex.position.x, vertex.position.y, vertex.u, vertex.v, color)
                vertexCursor += UiVertexLayout.GLYPH_FLOATS_PER_VERTEX
            }
            triangleMesh.indices.forEach { index ->
                indices[indexCursor] = vertexOffset + index
                indexCursor += 1
            }
            vertexOffset += triangleMesh.vertices.size
        }
        return UiStagedRun.GlyphRun(vertices, indices)
    }
}
