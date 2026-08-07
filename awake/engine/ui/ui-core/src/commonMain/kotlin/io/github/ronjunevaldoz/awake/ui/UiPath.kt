// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

enum class UiFillRule {
    NonZero,
    EvenOdd,
}

enum class UiStrokeCap {
    Butt,
    Round,
    Square,
}

enum class UiStrokeJoin {
    Miter,
    Round,
    Bevel,
}

data class UiStroke(
    val width: Dp = 1f.dp,
    val cap: UiStrokeCap = UiStrokeCap.Butt,
    val join: UiStrokeJoin = UiStrokeJoin.Miter,
)

data class UiPoint(
    val x: Float,
    val y: Float,
)

data class UiPathContour(
    val points: List<UiPoint>,
    val closed: Boolean,
)

data class UiTriangleMesh(
    val points: List<UiPoint>,
    val indices: IntArray,
)

data class UiTexturedVertex(
    val position: UiPoint,
    val u: Float,
    val v: Float,
)

data class UiTexturedTriangleMesh(
    val vertices: List<UiTexturedVertex>,
    val indices: IntArray,
)

/** Per-vertex-colored analog of [UiTexturedVertex]/[UiTexturedTriangleMesh] -- backs
 * [UiDrawPrimitive.GradientQuad]'s exact convex-path clip (mirrors how [UiTexturedVertex]
 * interpolates u/v across a clipped edge, but interpolates [Color] instead). */
data class UiColoredVertex(
    val position: UiPoint,
    val color: Color,
)

data class UiColoredTriangleMesh(
    val vertices: List<UiColoredVertex>,
    val indices: IntArray,
)

sealed interface UiPathCommand {
    data class MoveTo(val x: Float, val y: Float) : UiPathCommand
    data class LineTo(val x: Float, val y: Float) : UiPathCommand
    data class QuadTo(val cx: Float, val cy: Float, val x: Float, val y: Float) : UiPathCommand
    data class CubicTo(
        val c1x: Float,
        val c1y: Float,
        val c2x: Float,
        val c2y: Float,
        val x: Float,
        val y: Float,
    ) : UiPathCommand

    /**
     * Elliptical arc inside the given bounds, using screen-space coordinates (Y-down) and
     * degree angles so backends can choose whether they flatten, tessellate, or shader-draw
     * the segment later.
     */
    data class ArcTo(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float,
        val startDegrees: Float,
        val sweepDegrees: Float,
    ) : UiPathCommand

    data object Close : UiPathCommand
}

data class UiPath(
    val fillRule: UiFillRule = UiFillRule.NonZero,
    val commands: List<UiPathCommand>,
) {
    companion object {
        fun build(fillRule: UiFillRule = UiFillRule.NonZero, block: UiPathBuilder.() -> Unit): UiPath =
            uiPath(fillRule, block)
    }
}

class UiPathBuilder internal constructor(
    private val fillRule: UiFillRule,
) {
    private val commands = ArrayList<UiPathCommand>()

    fun moveTo(x: Float, y: Float) {
        commands += UiPathCommand.MoveTo(x, y)
    }

    fun lineTo(x: Float, y: Float) {
        commands += UiPathCommand.LineTo(x, y)
    }

    fun quadTo(cx: Float, cy: Float, x: Float, y: Float) {
        commands += UiPathCommand.QuadTo(cx, cy, x, y)
    }

    fun cubicTo(c1x: Float, c1y: Float, c2x: Float, c2y: Float, x: Float, y: Float) {
        commands += UiPathCommand.CubicTo(c1x, c1y, c2x, c2y, x, y)
    }

    fun arcTo(left: Float, top: Float, right: Float, bottom: Float, startDegrees: Float, sweepDegrees: Float) {
        commands += UiPathCommand.ArcTo(left, top, right, bottom, startDegrees, sweepDegrees)
    }

    fun close() {
        commands += UiPathCommand.Close
    }

    internal fun build(): UiPath = UiPath(fillRule = fillRule, commands = commands.toList())
}

fun uiPath(fillRule: UiFillRule = UiFillRule.NonZero, block: UiPathBuilder.() -> Unit): UiPath {
    val builder = UiPathBuilder(fillRule)
    builder.block()
    return builder.build()
}

sealed interface UiShapeSpec {
    data object Rectangle : UiShapeSpec
    data class RoundedRectangle(val radius: Dp) : UiShapeSpec
    data object Circle : UiShapeSpec
    data object Pill : UiShapeSpec
    data class CutCorner(val size: Dp) : UiShapeSpec
}

fun UiShapeSpec.toPath(bounds: UiBounds, fillRule: UiFillRule = UiFillRule.NonZero): UiPath = when (this) {
    UiShapeSpec.Rectangle -> rectanglePath(bounds, fillRule)
    is UiShapeSpec.RoundedRectangle -> roundedRectanglePath(bounds, radius, fillRule)
    UiShapeSpec.Circle -> circlePath(bounds, fillRule)
    UiShapeSpec.Pill -> pillPath(bounds, fillRule)
    is UiShapeSpec.CutCorner -> cutCornerPath(bounds, size, fillRule)
}

/**
 * Max inset that's safe to shrink a shape's own [bounds] by such that ANY primitive whose own
 * (axis-aligned) bounds fit entirely inside the shrunk rect provably cannot touch this shape's
 * rounded/cut corner region -- backs the "skip exact convex-path clipping" fast path in each
 * backend's `RendererDrawUi.kt`. Uses the exact same clamp each shape's own path-building
 * function ([roundedRectanglePath]/[cutCornerPath] et al, this file) applies to its own
 * radius/cut-size, so the margin here always matches the real geometry, never over-claims.
 * [UiShapeSpec.Rectangle] has no curved/cut corner at all, so its margin is 0 -- the shape's
 * whole bounds are already safe.
 */
fun UiShapeSpec.safeInteriorMargin(bounds: UiBounds): Float = when (this) {
    UiShapeSpec.Rectangle -> 0f
    is UiShapeSpec.RoundedRectangle -> radius.toPx().coerceIn(0f, min(bounds.width, bounds.height) / 2f)
    // Both built from roundedRectanglePath with radius == min(w, h) / 2 (see circlePath/
    // pillPath below) -- same margin formula, not a separate derivation.
    UiShapeSpec.Circle, UiShapeSpec.Pill -> min(bounds.width, bounds.height) / 2f
    is UiShapeSpec.CutCorner -> size.toPx().coerceIn(0f, min(bounds.width, bounds.height) / 2f)
}

fun UiPath.bounds(): UiBounds {
    if (commands.isEmpty()) return UiBounds(0f, 0f, 0f, 0f)

    var minX = Float.POSITIVE_INFINITY
    var minY = Float.POSITIVE_INFINITY
    var maxX = Float.NEGATIVE_INFINITY
    var maxY = Float.NEGATIVE_INFINITY

    fun include(x: Float, y: Float) {
        minX = minOf(minX, x)
        minY = minOf(minY, y)
        maxX = maxOf(maxX, x)
        maxY = maxOf(maxY, y)
    }

    commands.forEach { command ->
        when (command) {
            is UiPathCommand.MoveTo -> include(command.x, command.y)
            is UiPathCommand.LineTo -> include(command.x, command.y)
            is UiPathCommand.QuadTo -> {
                include(command.cx, command.cy)
                include(command.x, command.y)
            }
            is UiPathCommand.CubicTo -> {
                include(command.c1x, command.c1y)
                include(command.c2x, command.c2y)
                include(command.x, command.y)
            }
            is UiPathCommand.ArcTo -> {
                include(command.left, command.top)
                include(command.right, command.bottom)
            }
            UiPathCommand.Close -> Unit
        }
    }

    if (!minX.isFinite() || !minY.isFinite() || !maxX.isFinite() || !maxY.isFinite()) {
        return UiBounds(0f, 0f, 0f, 0f)
    }
    return UiBounds(minX, minY, (maxX - minX).coerceAtLeast(0f), (maxY - minY).coerceAtLeast(0f))
}

fun UiPath.transform(
    scaleX: Float = 1f,
    scaleY: Float = 1f,
    translateX: Float = 0f,
    translateY: Float = 0f,
): UiPath = copy(
    commands = commands.map { command ->
        when (command) {
            is UiPathCommand.MoveTo -> UiPathCommand.MoveTo(
                x = command.x * scaleX + translateX,
                y = command.y * scaleY + translateY,
            )
            is UiPathCommand.LineTo -> UiPathCommand.LineTo(
                x = command.x * scaleX + translateX,
                y = command.y * scaleY + translateY,
            )
            is UiPathCommand.QuadTo -> UiPathCommand.QuadTo(
                cx = command.cx * scaleX + translateX,
                cy = command.cy * scaleY + translateY,
                x = command.x * scaleX + translateX,
                y = command.y * scaleY + translateY,
            )
            is UiPathCommand.CubicTo -> UiPathCommand.CubicTo(
                c1x = command.c1x * scaleX + translateX,
                c1y = command.c1y * scaleY + translateY,
                c2x = command.c2x * scaleX + translateX,
                c2y = command.c2y * scaleY + translateY,
                x = command.x * scaleX + translateX,
                y = command.y * scaleY + translateY,
            )
            is UiPathCommand.ArcTo -> UiPathCommand.ArcTo(
                left = command.left * scaleX + translateX,
                top = command.top * scaleY + translateY,
                right = command.right * scaleX + translateX,
                bottom = command.bottom * scaleY + translateY,
                startDegrees = command.startDegrees,
                sweepDegrees = command.sweepDegrees,
            )
            UiPathCommand.Close -> UiPathCommand.Close
        }
    },
)

fun UiPath.flattenContours(
    curveSteps: Int = 8,
    arcStepDegrees: Float = 15f,
): List<UiPathContour> {
    if (commands.isEmpty()) return emptyList()

    val contours = ArrayList<UiPathContour>()
    var points = ArrayList<UiPoint>()
    var cursor: UiPoint? = null
    var subpathStart: UiPoint? = null
    var closed = false

    fun appendPoint(point: UiPoint) {
        if (points.lastOrNull() != point) points += point
        cursor = point
    }

    fun finishContour() {
        if (points.isEmpty()) return
        contours += UiPathContour(points.toList(), closed)
        points = ArrayList()
        subpathStart = null
        closed = false
    }

    commands.forEach { command ->
        when (command) {
            is UiPathCommand.MoveTo -> {
                finishContour()
                val point = UiPoint(command.x, command.y)
                points += point
                cursor = point
                subpathStart = point
            }

            is UiPathCommand.LineTo -> appendPoint(UiPoint(command.x, command.y))

            is UiPathCommand.QuadTo -> {
                val start = cursor ?: UiPoint(command.x, command.y)
                val steps = curveSteps.coerceAtLeast(1)
                repeat(steps) { step ->
                    val t = (step + 1) / steps.toFloat()
                    val oneMinusT = 1f - t
                    appendPoint(
                        UiPoint(
                            x = oneMinusT * oneMinusT * start.x + 2f * oneMinusT * t * command.cx + t * t * command.x,
                            y = oneMinusT * oneMinusT * start.y + 2f * oneMinusT * t * command.cy + t * t * command.y,
                        ),
                    )
                }
            }

            is UiPathCommand.CubicTo -> {
                val start = cursor ?: UiPoint(command.x, command.y)
                val steps = curveSteps.coerceAtLeast(1)
                repeat(steps) { step ->
                    val t = (step + 1) / steps.toFloat()
                    val oneMinusT = 1f - t
                    appendPoint(
                        UiPoint(
                            x = oneMinusT * oneMinusT * oneMinusT * start.x +
                                3f * oneMinusT * oneMinusT * t * command.c1x +
                                3f * oneMinusT * t * t * command.c2x +
                                t * t * t * command.x,
                            y = oneMinusT * oneMinusT * oneMinusT * start.y +
                                3f * oneMinusT * oneMinusT * t * command.c1y +
                                3f * oneMinusT * t * t * command.c2y +
                                t * t * t * command.y,
                        ),
                    )
                }
            }

            is UiPathCommand.ArcTo -> {
                val centerX = (command.left + command.right) / 2f
                val centerY = (command.top + command.bottom) / 2f
                val radiusX = (command.right - command.left) / 2f
                val radiusY = (command.bottom - command.top) / 2f
                val steps = ceil(abs(command.sweepDegrees) / arcStepDegrees.coerceAtLeast(1f)).toInt().coerceAtLeast(1)
                repeat(steps) { step ->
                    val t = (step + 1) / steps.toFloat()
                    val angleDegrees = command.startDegrees + command.sweepDegrees * t
                    val angleRadians = angleDegrees * PI.toFloat() / 180f
                    appendPoint(
                        UiPoint(
                            x = centerX + cos(angleRadians) * radiusX,
                            y = centerY + sin(angleRadians) * radiusY,
                        ),
                    )
                }
            }

            UiPathCommand.Close -> {
                closed = true
                cursor = subpathStart ?: cursor
                finishContour()
            }
        }
    }

    finishContour()
    return contours
}

/**
 * A fill contour plus the hole contours cut out of it. [holes] points are already oriented
 * opposite to [outer] (see [resolveFillGroups]) so winding-derived math (the AA fringe's
 * outward normal) lands on the correct side without re-checking.
 */
private class UiFillGroup(
    val outer: List<UiPoint>,
    val holes: List<List<UiPoint>>,
)

/**
 * Groups a path's closed contours into fill-outers and the holes nested inside them, honoring
 * [UiPath.fillRule]:
 * - [UiFillRule.EvenOdd]: a contour contained in an odd number of other contours is a hole
 *   (SVG icon sources -- e.g. Heroicons' clock/camera glyphs -- express holes this way).
 * - [UiFillRule.NonZero]: a nested contour is a hole only when it winds opposite to its
 *   containing contour; same-winding nested contours keep filling (per the winding rule).
 * Open contours and contours under 3 points are returned as plain hole-less groups, matching
 * their previous behavior. Containment is tested with one representative vertex per contour --
 * exact-touching contours may misclassify, and then the triangulation falls back to the old
 * independent-fill rendering rather than crashing.
 */
private fun resolveFillGroups(contours: List<UiPathContour>, fillRule: UiFillRule): List<UiFillGroup> {
    val closed = ArrayList<UiPathContour>()
    val simple = ArrayList<UiFillGroup>()
    contours.forEach { contour ->
        if (contour.closed && contour.points.size >= 3) closed += contour
        else if (contour.points.size >= 3) simple += UiFillGroup(contour.points, emptyList())
    }
    if (closed.size <= 1) {
        closed.forEach { simple += UiFillGroup(it.points, emptyList()) }
        return simple
    }

    val areas = closed.map { polygonSignedArea(it.points) }
    // containers[i] = indices of contours containing contour i's first vertex.
    val containers = closed.indices.map { i ->
        val probe = closed[i].points.first()
        closed.indices.filter { j -> j != i && closed[j].windingContribution(probe.x, probe.y) != 0 }
    }
    val isHole = BooleanArray(closed.size)
    for (i in closed.indices) {
        val depth = containers[i].size
        if (depth == 0) continue
        val parent = containers[i].minByOrNull { abs(areas[it]) } ?: continue
        isHole[i] = when (fillRule) {
            UiFillRule.EvenOdd -> depth % 2 == 1
            UiFillRule.NonZero -> areas[i] * areas[parent] < 0f
        }
    }

    val groups = ArrayList<UiFillGroup>(simple)
    for (i in closed.indices) {
        if (isHole[i]) continue
        val holes = closed.indices
            .filter { h ->
                isHole[h] && i == containers[h].filter { !isHole[it] }.minByOrNull { abs(areas[it]) }
            }
            .map { h ->
                // Orient each hole opposite its outer so winding math (scanline fill, the AA
                // fringe's outward normal) is uniform.
                if (areas[h] * areas[i] > 0f) closed[h].points.asReversed() else closed[h].points
            }
        groups += UiFillGroup(closed[i].points, holes)
    }
    return groups
}

/**
 * Scanline-trapezoid triangulation over a set of contours, honoring [fillRule] -- the general
 * fill for anything a centroid fan can't do: concave contours, nested holes, and multiple
 * islands, with NO hole-bridging step. (An ear-clipping + zero-width-bridge reduction was tried
 * first and is a known trap: on the weakly simple polygon bridging produces, an "ear" can cross
 * the zero-width corridor without containing any vertex, mis-filling hole icons like Heroicons'
 * clock/camera.) Slabs are cut at every distinct vertex y, so no edge starts or ends strictly
 * inside a slab; each slab's crossings are sorted at the slab's midline (which never passes
 * through a vertex) and inside spans become two triangles each.
 */
// ponytail: emits ~4 vertices per span per slab with no sharing -- a few hundred vertices for a
// typical icon, an order more than ear clipping would; fine for icon/UI paths, revisit with a
// real polygon triangulator if fills ever get huge or per-frame tessellation shows up in traces.
private fun scanlineFillTriangulation(
    contours: List<List<UiPoint>>,
    fillRule: UiFillRule,
): Pair<List<UiPoint>, IntArray> {
    class Edge(val x0: Float, val y0: Float, val x1: Float, val y1: Float, val dir: Int)

    val edges = ArrayList<Edge>()
    contours.forEach { polygon ->
        for (i in polygon.indices) {
            val a = polygon[i]
            val b = polygon[(i + 1) % polygon.size]
            if (a.y == b.y) continue
            edges += if (a.y < b.y) Edge(a.x, a.y, b.x, b.y, 1) else Edge(b.x, b.y, a.x, a.y, -1)
        }
    }
    if (edges.isEmpty()) return emptyList<UiPoint>() to IntArray(0)

    fun xAt(edge: Edge, y: Float): Float =
        edge.x0 + (edge.x1 - edge.x0) * (y - edge.y0) / (edge.y1 - edge.y0)

    val ys = edges.flatMap { listOf(it.y0, it.y1) }.distinct().sorted()
    val points = ArrayList<UiPoint>()
    val indices = ArrayList<Int>()
    for (s in 0 until ys.size - 1) {
        val yTop = ys[s]
        val yBottom = ys[s + 1]
        if (yBottom <= yTop) continue
        val midY = (yTop + yBottom) / 2f
        val active = edges.filter { it.y0 <= yTop && it.y1 >= yBottom }.sortedBy { xAt(it, midY) }
        var winding = 0
        var crossings = 0
        var openEdge: Edge? = null
        active.forEach { edge ->
            val wasInside = when (fillRule) {
                UiFillRule.NonZero -> winding != 0
                UiFillRule.EvenOdd -> crossings % 2 == 1
            }
            winding += edge.dir
            crossings += 1
            val isInside = when (fillRule) {
                UiFillRule.NonZero -> winding != 0
                UiFillRule.EvenOdd -> crossings % 2 == 1
            }
            if (!wasInside && isInside) {
                openEdge = edge
            } else if (wasInside && !isInside) {
                val left = openEdge ?: return@forEach
                openEdge = null
                val leftTopX = xAt(left, yTop)
                val rightTopX = xAt(edge, yTop)
                val leftBottomX = xAt(left, yBottom)
                val rightBottomX = xAt(edge, yBottom)
                if (rightTopX <= leftTopX && rightBottomX <= leftBottomX) return@forEach
                val base = points.size
                points += UiPoint(leftTopX, yTop)
                points += UiPoint(rightTopX, yTop)
                points += UiPoint(rightBottomX, yBottom)
                points += UiPoint(leftBottomX, yBottom)
                indices += base
                indices += base + 1
                indices += base + 2
                indices += base + 2
                indices += base + 3
                indices += base
            }
        }
    }
    return points to indices.toIntArray()
}

fun UiPath.tessellateFill(): UiTriangleMesh {
    val contours = flattenContours()
    if (contours.isEmpty()) return UiTriangleMesh(emptyList(), IntArray(0))

    val points = ArrayList<UiPoint>()
    val indices = ArrayList<Int>()
    resolveFillGroups(contours, fillRule).forEach { group ->
        appendGroupFill(group, onTriangulated = { meshPoints, meshIndices ->
            val base = points.size
            points += meshPoints
            meshIndices.forEach { indices += base + it }
        }, onFanned = { polygon ->
            appendCentroidFan(polygon, points, indices)
        })
    }
    return UiTriangleMesh(points, indices.toIntArray())
}

/**
 * Shared interior-triangulation strategy for [tessellateFill]/[tessellateFillAa]:
 * centroid-fan convex hole-less contours (the hot path -- buttons, cards, every rounded rect),
 * scanline-triangulate everything else (concave contours, groups with holes). [onTriangulated]
 * receives a self-contained (points, indices) mesh; [onFanned] receives the raw polygon.
 */
private inline fun appendGroupFill(
    group: UiFillGroup,
    onTriangulated: (List<UiPoint>, IntArray) -> Unit,
    onFanned: (List<UiPoint>) -> Unit,
) {
    if (group.holes.isEmpty() && isConvex(group.outer)) {
        onFanned(group.outer)
        return
    }
    // Concave contour (e.g. a chevron icon) or holes (e.g. a clock glyph): a centroid fan is
    // invalid here -- the centroid can sit outside the polygon (overfilling a chevron's notch)
    // and fans know nothing about holes. resolveFillGroups already oriented holes opposite
    // their outer, so NonZero winding cancels inside them regardless of the source fill rule.
    val (meshPoints, meshIndices) = scanlineFillTriangulation(
        contours = listOf(group.outer) + group.holes,
        fillRule = UiFillRule.NonZero,
    )
    if (meshIndices.isEmpty() && group.holes.isEmpty()) {
        // Degenerate (e.g. zero-area) contour: keep the old fan behavior rather than dropping it.
        onFanned(group.outer)
    } else {
        onTriangulated(meshPoints, meshIndices)
    }
}

private fun appendCentroidFan(polygon: List<UiPoint>, points: ArrayList<UiPoint>, indices: ArrayList<Int>) {
    if (polygon.size < 3) return
    // Fan from the polygon's centroid rather than vertex 0: for wide, shallow convex
    // shapes (e.g. a 600x36 rounded-rect button), a vertex-0 fan produces near-degenerate
    // sliver triangles between points on the far side of the shape (e.g. two points a few
    // tenths of a pixel apart in y but ~600px apart in x, both opposite the fan origin).
    // The rasterizer's edge function subtracts products of that huge x-span against a
    // tiny y-span, and float32 catastrophic cancellation flips the inside/outside test for
    // pixels near that sliver -- visible as a seam/gap along the top edge on the side far
    // from vertex 0. A centroid fan keeps every triangle's extent close to the shape's own
    // size, avoiding the cancellation. Valid for convex contours only (rect/rounded-rect/
    // circle/pill/cut-corner); concave/holed groups scanline-triangulate in appendGroupFill and
    // only fall through here on degenerate (zero-area) input.
    var centroidX = 0f
    var centroidY = 0f
    polygon.forEach {
        centroidX += it.x
        centroidY += it.y
    }
    centroidX /= polygon.size
    centroidY /= polygon.size

    val base = points.size
    points += UiPoint(centroidX, centroidY)
    points += polygon
    for (i in 0 until polygon.size) {
        indices += base
        indices += base + 1 + i
        indices += base + 1 + (i + 1) % polygon.size
    }
}

/** Default AA fringe width in pixels for [tessellateFillAa] -- ~1px matches the rounded-quad
 * SDF pipeline's smoothstep band (see `ui_rounded_quad.frag`/`.wgsl`'s `smoothstep(-1.0, 1.0,
 * dist)`), so a plain [UiPath] fill's diagonal/curved edges get a comparable amount of
 * softening to what [UiDrawPrimitive.RoundedQuad] already gets for free from its SDF. */
private const val AA_FRINGE_PX = 1f

/**
 * Antialiased sibling of [tessellateFill] -- identical interior centroid-fan triangulation,
 * plus a thin (~1px) "fringe" ring of triangles along each contour's boundary whose OUTER
 * edge fades [color]'s alpha to 0. This is the standard vector-graphics AA technique used by
 * e.g. NanoVG ("feathering"): a solid interior plus a soft ~1px border, blended through the UI
 * quad pipeline's EXISTING src-alpha/one-minus-src-alpha blend state
 * ([io.github.ronjunevaldoz.awake.vulkan.ui.UiRenderPipeline]'s `blendEnable = true`, mirrored
 * on WebGPU) -- gives every diagonal/curved [io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
 * .FilledPath] edge (e.g. the dropdown chevron icon) real antialiasing with zero new render
 * pass, framebuffer attachment, or per-pipeline multisample state. A general MSAA setup would
 * touch the shared swapchain/offscreen render pass AND every pipeline in both backends just to
 * fix hard triangle edges on an already-flat-shaded 2D overlay -- not worth it here.
 *
 * Returns [UiColoredTriangleMesh] (not the plain [UiTriangleMesh] [tessellateFill] returns)
 * since the fringe ring needs a PER-VERTEX alpha, not one flat color for the whole mesh. Both
 * backends already have the plumbing for this from [UiDrawPrimitive.GradientQuad]'s exact-clip
 * path ([clipToConvexPaths] on [UiColoredTriangleMesh], `stageColoredVertexTriangleMeshes`) --
 * this only reuses it, it adds no new backend machinery.
 */
fun UiPath.tessellateFillAa(color: Color, fringePx: Float = AA_FRINGE_PX): UiColoredTriangleMesh {
    val contours = flattenContours()
    if (contours.isEmpty()) return UiColoredTriangleMesh(emptyList(), IntArray(0))

    val vertices = ArrayList<UiColoredVertex>()
    val indices = ArrayList<Int>()
    val transparent = color.withAlpha(0f)

    resolveFillGroups(contours, fillRule).forEach { group ->
        // Interior -- same triangulation strategy as tessellateFill (scanline for concave/holed
        // groups, centroid fan for convex; see its doc comments), full-alpha color throughout.
        appendGroupFill(group, onTriangulated = { meshPoints, meshIndices ->
            val base = vertices.size
            meshPoints.forEach { vertices += UiColoredVertex(it, color) }
            meshIndices.forEach { indices += base + it }
        }, onFanned = { polygon ->
            var centroidX = 0f
            var centroidY = 0f
            polygon.forEach {
                centroidX += it.x
                centroidY += it.y
            }
            centroidX /= polygon.size
            centroidY /= polygon.size

            val fanBase = vertices.size
            vertices += UiColoredVertex(UiPoint(centroidX, centroidY), color)
            polygon.forEach { vertices += UiColoredVertex(it, color) }
            for (i in polygon.indices) {
                indices += fanBase
                indices += fanBase + 1 + i
                indices += fanBase + 1 + (i + 1) % polygon.size
            }
        })

        // Fringe every boundary of the group -- the outer contour fringes outward as before;
        // hole contours were re-oriented opposite the outer in resolveFillGroups, so the same
        // winding-derived normal lands their fringe on the hole's (non-filled) side.
        (listOf(group.outer) + group.holes).forEach { polygon ->
            appendBoundaryFringe(polygon, color, transparent, fringePx, vertices, indices)
        }
    }
    return UiColoredTriangleMesh(vertices, indices.toIntArray())
}

private fun appendBoundaryFringe(
    polygon: List<UiPoint>,
    color: Color,
    transparent: Color,
    fringePx: Float,
    vertices: ArrayList<UiColoredVertex>,
    indices: ArrayList<Int>,
) {
    if (polygon.size < 3) return
    run {
        // Fringe ring -- one quad per edge, offset outward along that edge's normal, alpha
        // fading from color's own alpha (at the true polygon boundary) to fully transparent
        // (at the offset outer edge). Per-edge (not mitered) normals leave an invisible
        // sub-pixel gap/overlap at convex corners -- fine at a ~1px fringe width, and
        // correctness-safe since overlapping alpha-blended fringe triangles at a corner can
        // only ever shift a sub-pixel sliver's alpha, never break the interior fill itself.
        //
        // Outward direction depends on winding: every UiShapeSpec path in this file (rect,
        // rounded-rect, circle/pill, cut-corner) winds clockwise in this screen's Y-down space
        // (top-left -> top-right -> bottom-right -> bottom-left), which is a positive
        // polygonSignedArea -- verified against rectanglePath: edge (top-left -> top-right)
        // needs outward normal (0, -1) (up), and (dy, -dx) with this edge's direction (1, 0)
        // gives exactly (0, -1). A negative-area (counter-clockwise) contour needs the sign
        // flipped so custom caller-built [UiPath]s with the opposite winding still fringe
        // outward, not inward.
        val orientation = polygonSignedArea(polygon)
        val outwardSign = if (orientation >= 0f) 1f else -1f
        for (i in polygon.indices) {
            val a = polygon[i]
            val b = polygon[(i + 1) % polygon.size]
            var dx = b.x - a.x
            var dy = b.y - a.y
            val length = hypot(dx, dy)
            if (length <= 0f) continue
            dx /= length
            dy /= length
            val nx = dy * fringePx * outwardSign
            val ny = -dx * fringePx * outwardSign

            val base = vertices.size
            vertices += UiColoredVertex(a, color)
            vertices += UiColoredVertex(b, color)
            vertices += UiColoredVertex(UiPoint(b.x + nx, b.y + ny), transparent)
            vertices += UiColoredVertex(UiPoint(a.x + nx, a.y + ny), transparent)
            indices += base
            indices += base + 1
            indices += base + 2
            indices += base + 2
            indices += base + 3
            indices += base
        }
    }
}

fun UiPath.tessellateStroke(stroke: UiStroke): UiTriangleMesh {
    val contours = flattenContours()
    if (contours.isEmpty()) return UiTriangleMesh(emptyList(), IntArray(0))

    val halfWidth = stroke.width.toPx() / 2f
    if (halfWidth <= 0f) return UiTriangleMesh(emptyList(), IntArray(0))

    val points = ArrayList<UiPoint>()
    val indices = ArrayList<Int>()
    contours.forEach { contour ->
        val vertices = contour.points
        if (vertices.size < 2) return@forEach

        val segmentCount = if (contour.closed) vertices.size else vertices.size - 1
        for (i in 0 until segmentCount) {
            val start = vertices[i]
            val end = vertices[(i + 1) % vertices.size]
            var dx = end.x - start.x
            var dy = end.y - start.y
            val length = hypot(dx, dy)
            if (length <= 0f) continue

            dx /= length
            dy /= length
            val extend = if (!contour.closed && stroke.cap == UiStrokeCap.Square) halfWidth else 0f
            val startX = if (!contour.closed && i == 0) start.x - dx * extend else start.x
            val startY = if (!contour.closed && i == 0) start.y - dy * extend else start.y
            val endX = if (!contour.closed && i == segmentCount - 1) end.x + dx * extend else end.x
            val endY = if (!contour.closed && i == segmentCount - 1) end.y + dy * extend else end.y

            val nx = -dy * halfWidth
            val ny = dx * halfWidth
            val base = points.size
            points += UiPoint(startX + nx, startY + ny)
            points += UiPoint(endX + nx, endY + ny)
            points += UiPoint(endX - nx, endY - ny)
            points += UiPoint(startX - nx, startY - ny)

            indices += base
            indices += base + 1
            indices += base + 2
            indices += base + 2
            indices += base + 3
            indices += base
        }
    }
    return UiTriangleMesh(points, indices.toIntArray())
}

fun UiPath.convexClipContour(): List<UiPoint>? {
    val contour = flattenContours().firstOrNull { it.closed && it.points.size >= 3 }?.points ?: return null
    return if (isConvex(contour)) contour else null
}

fun UiTriangleMesh.clipToConvexPath(path: UiPath): UiTriangleMesh {
    val clipContour = path.convexClipContour() ?: return this
    return clipToConvexContour(clipContour)
}

fun UiTexturedTriangleMesh.clipToConvexPath(path: UiPath): UiTexturedTriangleMesh {
    val clipContour = path.convexClipContour() ?: return this
    return clipToConvexContour(clipContour)
}

fun UiTriangleMesh.clipToConvexPaths(paths: List<UiPath>): UiTriangleMesh {
    var current = this
    paths.forEach { path ->
        val contour = path.convexClipContour() ?: return current
        current = current.clipToConvexContour(contour)
    }
    return current
}

fun UiTexturedTriangleMesh.clipToConvexPaths(paths: List<UiPath>): UiTexturedTriangleMesh {
    var current = this
    paths.forEach { path ->
        val contour = path.convexClipContour() ?: return current
        current = current.clipToConvexContour(contour)
    }
    return current
}

fun UiColoredTriangleMesh.clipToConvexPaths(paths: List<UiPath>): UiColoredTriangleMesh {
    var current = this
    paths.forEach { path ->
        val contour = path.convexClipContour() ?: return current
        current = current.clipToConvexContour(contour)
    }
    return current
}

private fun UiTriangleMesh.clipToConvexContour(clipContour: List<UiPoint>): UiTriangleMesh {
    if (points.isEmpty() || indices.isEmpty()) return this

    val clippedPoints = ArrayList<UiPoint>()
    val clippedIndices = ArrayList<Int>()
    var index = 0
    while (index + 2 < indices.size) {
        val triangle = listOf(
            points[indices[index]],
            points[indices[index + 1]],
            points[indices[index + 2]],
        )
        val clippedPolygon = clipPolygonToConvexContour(triangle, clipContour)
        if (clippedPolygon.size >= 3) {
            val base = clippedPoints.size
            clippedPoints += clippedPolygon
            for (i in 1 until clippedPolygon.lastIndex) {
                clippedIndices += base
                clippedIndices += base + i
                clippedIndices += base + i + 1
            }
        }
        index += 3
    }
    return UiTriangleMesh(clippedPoints, clippedIndices.toIntArray())
}

private fun UiTexturedTriangleMesh.clipToConvexContour(clipContour: List<UiPoint>): UiTexturedTriangleMesh {
    if (vertices.isEmpty() || indices.isEmpty()) return this

    val clippedVertices = ArrayList<UiTexturedVertex>()
    val clippedIndices = ArrayList<Int>()
    var index = 0
    while (index + 2 < indices.size) {
        val triangle = listOf(
            vertices[indices[index]],
            vertices[indices[index + 1]],
            vertices[indices[index + 2]],
        )
        val clippedPolygon = clipTexturedPolygonToConvexContour(triangle, clipContour)
        if (clippedPolygon.size >= 3) {
            val base = clippedVertices.size
            clippedVertices += clippedPolygon
            for (i in 1 until clippedPolygon.lastIndex) {
                clippedIndices += base
                clippedIndices += base + i
                clippedIndices += base + i + 1
            }
        }
        index += 3
    }
    return UiTexturedTriangleMesh(clippedVertices, clippedIndices.toIntArray())
}

private fun UiColoredTriangleMesh.clipToConvexContour(clipContour: List<UiPoint>): UiColoredTriangleMesh {
    if (vertices.isEmpty() || indices.isEmpty()) return this

    val clippedVertices = ArrayList<UiColoredVertex>()
    val clippedIndices = ArrayList<Int>()
    var index = 0
    while (index + 2 < indices.size) {
        val triangle = listOf(
            vertices[indices[index]],
            vertices[indices[index + 1]],
            vertices[indices[index + 2]],
        )
        val clippedPolygon = clipColoredPolygonToConvexContour(triangle, clipContour)
        if (clippedPolygon.size >= 3) {
            val base = clippedVertices.size
            clippedVertices += clippedPolygon
            for (i in 1 until clippedPolygon.lastIndex) {
                clippedIndices += base
                clippedIndices += base + i
                clippedIndices += base + i + 1
            }
        }
        index += 3
    }
    return UiColoredTriangleMesh(clippedVertices, clippedIndices.toIntArray())
}

fun UiPath.containsPoint(x: Float, y: Float): Boolean {
    val contours = flattenContours()
    if (contours.isEmpty()) return false
    return when (fillRule) {
        UiFillRule.EvenOdd -> contours.count { contour -> contour.closed && contour.containsPoint(x, y) } % 2 == 1
        UiFillRule.NonZero -> contours.sumOf { contour -> if (contour.closed) contour.windingContribution(x, y) else 0 } != 0
    }
}

private fun UiPathContour.containsPoint(x: Float, y: Float): Boolean = windingContribution(x, y) != 0

private fun UiPathContour.windingContribution(x: Float, y: Float): Int {
    val polygon = points
    if (polygon.size < 3) return 0

    var windingNumber = 0
    for (i in polygon.indices) {
        val a = polygon[i]
        val b = polygon[(i + 1) % polygon.size]
        if (a.y <= y) {
            if (b.y > y && isLeft(a, b, x, y) > 0f) windingNumber += 1
        } else if (b.y <= y && isLeft(a, b, x, y) < 0f) {
            windingNumber -= 1
        }
    }
    return windingNumber
}

private fun isLeft(a: UiPoint, b: UiPoint, x: Float, y: Float): Float =
    (b.x - a.x) * (y - a.y) - (x - a.x) * (b.y - a.y)

private fun isConvex(points: List<UiPoint>): Boolean {
    if (points.size < 3) return false
    var sign = 0
    for (i in points.indices) {
        val a = points[i]
        val b = points[(i + 1) % points.size]
        val c = points[(i + 2) % points.size]
        val cross = (b.x - a.x) * (c.y - b.y) - (b.y - a.y) * (c.x - b.x)
        if (cross == 0f) continue
        val currentSign = if (cross > 0f) 1 else -1
        if (sign == 0) {
            sign = currentSign
        } else if (sign != currentSign) {
            return false
        }
    }
    return sign != 0
}

private fun clipPolygonToConvexContour(subject: List<UiPoint>, clip: List<UiPoint>): List<UiPoint> {
    if (subject.isEmpty()) return emptyList()
    var output = subject
    val orientation = polygonSignedArea(clip)
    if (orientation == 0f) return subject
    val isCounterClockwise = orientation > 0f

    for (i in clip.indices) {
        val a = clip[i]
        val b = clip[(i + 1) % clip.size]
        if (output.isEmpty()) break
        val input = output
        output = buildList {
            var prev = input.last()
            input.forEach { curr ->
                val currInside = isInsideConvexEdge(curr, a, b, isCounterClockwise)
                val prevInside = isInsideConvexEdge(prev, a, b, isCounterClockwise)
                if (currInside) {
                    if (!prevInside) add(lineIntersection(prev, curr, a, b))
                    add(curr)
                } else if (prevInside) {
                    add(lineIntersection(prev, curr, a, b))
                }
                prev = curr
            }
        }
    }
    return output
}

private fun clipTexturedPolygonToConvexContour(subject: List<UiTexturedVertex>, clip: List<UiPoint>): List<UiTexturedVertex> {
    if (subject.isEmpty()) return emptyList()
    var output = subject
    val orientation = polygonSignedArea(clip)
    if (orientation == 0f) return subject
    val isCounterClockwise = orientation > 0f

    for (i in clip.indices) {
        val a = clip[i]
        val b = clip[(i + 1) % clip.size]
        if (output.isEmpty()) break
        val input = output
        output = buildList {
            var prev = input.last()
            input.forEach { curr ->
                val currInside = isInsideConvexEdge(curr.position, a, b, isCounterClockwise)
                val prevInside = isInsideConvexEdge(prev.position, a, b, isCounterClockwise)
                if (currInside) {
                    if (!prevInside) add(lineIntersection(prev, curr, a, b))
                    add(curr)
                } else if (prevInside) {
                    add(lineIntersection(prev, curr, a, b))
                }
                prev = curr
            }
        }
    }
    return output
}

private fun clipColoredPolygonToConvexContour(subject: List<UiColoredVertex>, clip: List<UiPoint>): List<UiColoredVertex> {
    if (subject.isEmpty()) return emptyList()
    var output = subject
    val orientation = polygonSignedArea(clip)
    if (orientation == 0f) return subject
    val isCounterClockwise = orientation > 0f

    for (i in clip.indices) {
        val a = clip[i]
        val b = clip[(i + 1) % clip.size]
        if (output.isEmpty()) break
        val input = output
        output = buildList {
            var prev = input.last()
            input.forEach { curr ->
                val currInside = isInsideConvexEdge(curr.position, a, b, isCounterClockwise)
                val prevInside = isInsideConvexEdge(prev.position, a, b, isCounterClockwise)
                if (currInside) {
                    if (!prevInside) add(lineIntersection(prev, curr, a, b))
                    add(curr)
                } else if (prevInside) {
                    add(lineIntersection(prev, curr, a, b))
                }
                prev = curr
            }
        }
    }
    return output
}

private fun polygonSignedArea(points: List<UiPoint>): Float {
    var area = 0f
    for (i in points.indices) {
        val a = points[i]
        val b = points[(i + 1) % points.size]
        area += a.x * b.y - b.x * a.y
    }
    return area / 2f
}

private fun isInsideConvexEdge(point: UiPoint, edgeStart: UiPoint, edgeEnd: UiPoint, counterClockwise: Boolean): Boolean {
    val cross = isLeft(edgeStart, edgeEnd, point.x, point.y)
    return if (counterClockwise) cross >= 0f else cross <= 0f
}

private fun lineIntersection(p1: UiPoint, p2: UiPoint, a: UiPoint, b: UiPoint): UiPoint {
    val s1x = p2.x - p1.x
    val s1y = p2.y - p1.y
    val s2x = b.x - a.x
    val s2y = b.y - a.y
    val denominator = -s2x * s1y + s1x * s2y
    if (denominator == 0f) return p2
    val s = (-s1y * (p1.x - a.x) + s1x * (p1.y - a.y)) / denominator
    return UiPoint(a.x + s * s2x, a.y + s * s2y)
}

private fun lineIntersection(p1: UiTexturedVertex, p2: UiTexturedVertex, a: UiPoint, b: UiPoint): UiTexturedVertex {
    val intersection = lineIntersection(p1.position, p2.position, a, b)
    val dx = p2.position.x - p1.position.x
    val dy = p2.position.y - p1.position.y
    val t = when {
        abs(dx) >= abs(dy) && dx != 0f -> (intersection.x - p1.position.x) / dx
        dy != 0f -> (intersection.y - p1.position.y) / dy
        else -> 0f
    }.coerceIn(0f, 1f)
    return UiTexturedVertex(
        position = intersection,
        u = p1.u + (p2.u - p1.u) * t,
        v = p1.v + (p2.v - p1.v) * t,
    )
}

private fun lineIntersection(p1: UiColoredVertex, p2: UiColoredVertex, a: UiPoint, b: UiPoint): UiColoredVertex {
    val intersection = lineIntersection(p1.position, p2.position, a, b)
    val dx = p2.position.x - p1.position.x
    val dy = p2.position.y - p1.position.y
    val t = when {
        abs(dx) >= abs(dy) && dx != 0f -> (intersection.x - p1.position.x) / dx
        dy != 0f -> (intersection.y - p1.position.y) / dy
        else -> 0f
    }.coerceIn(0f, 1f)
    return UiColoredVertex(
        position = intersection,
        color = p1.color.lerp(p2.color, t),
    )
}

private fun rectanglePath(bounds: UiBounds, fillRule: UiFillRule): UiPath = uiPath(fillRule) {
    moveTo(bounds.x, bounds.y)
    lineTo(bounds.x + bounds.width, bounds.y)
    lineTo(bounds.x + bounds.width, bounds.y + bounds.height)
    lineTo(bounds.x, bounds.y + bounds.height)
    close()
}

private fun roundedRectanglePath(bounds: UiBounds, radius: Dp, fillRule: UiFillRule): UiPath {
    val r = radius.toPx().coerceIn(0f, min(bounds.width, bounds.height) / 2f)
    if (r == 0f) return rectanglePath(bounds, fillRule)

    val left = bounds.x
    val top = bounds.y
    val right = bounds.x + bounds.width
    val bottom = bounds.y + bounds.height

    return uiPath(fillRule) {
        moveTo(left + r, top)
        lineTo(right - r, top)
        arcTo(right - 2f * r, top, right, top + 2f * r, -90f, 90f)
        lineTo(right, bottom - r)
        arcTo(right - 2f * r, bottom - 2f * r, right, bottom, 0f, 90f)
        lineTo(left + r, bottom)
        arcTo(left, bottom - 2f * r, left + 2f * r, bottom, 90f, 90f)
        lineTo(left, top + r)
        arcTo(left, top, left + 2f * r, top + 2f * r, 180f, 90f)
        close()
    }
}

private fun circlePath(bounds: UiBounds, fillRule: UiFillRule): UiPath {
    val diameter = min(bounds.width, bounds.height)
    val insetX = (bounds.width - diameter) / 2f
    val insetY = (bounds.height - diameter) / 2f
    return roundedRectanglePath(
        bounds = UiBounds(bounds.x + insetX, bounds.y + insetY, diameter, diameter),
        radius = (diameter / 2f).px,
        fillRule = fillRule,
    )
}

private fun pillPath(bounds: UiBounds, fillRule: UiFillRule): UiPath {
    val radiusPx = min(bounds.width, bounds.height) / 2f
    return roundedRectanglePath(bounds, radiusPx.px, fillRule)
}

private fun cutCornerPath(bounds: UiBounds, size: Dp, fillRule: UiFillRule): UiPath {
    val cut = size.toPx().coerceIn(0f, min(bounds.width, bounds.height) / 2f)
    if (cut == 0f) return rectanglePath(bounds, fillRule)

    val left = bounds.x
    val top = bounds.y
    val right = bounds.x + bounds.width
    val bottom = bounds.y + bounds.height

    return uiPath(fillRule) {
        moveTo(left + cut, top)
        lineTo(right - cut, top)
        lineTo(right, top + cut)
        lineTo(right, bottom - cut)
        lineTo(right - cut, bottom)
        lineTo(left + cut, bottom)
        lineTo(left, bottom - cut)
        lineTo(left, top + cut)
        close()
    }
}
