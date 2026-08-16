// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

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

/**
 * Splits a mesh into pieces that each fit a backend's fixed per-draw buffer, cutting on triangle
 * boundaries and re-indexing each piece against its own vertex list.
 *
 * Backends chunk a *list* of meshes into draws, but their chunkers only ever flush between
 * meshes -- so a single mesh larger than one buffer was never split and blew the capacity check
 * at staging time. That held while every fill was a centroid fan (one vertex per contour point
 * plus a centre); it stopped holding once concave and holed paths started scanline-filling,
 * which emits a quad per span per slab. Run meshes through this before chunking so the
 * "one mesh fits one buffer" assumption is enforced rather than assumed.
 *
 * Returns the receiver unchanged when it already fits, so the common case allocates nothing.
 */
fun UiColoredTriangleMesh.splitToCapacity(maxVertices: Int, maxIndices: Int): List<UiColoredTriangleMesh> {
    if (vertices.size <= maxVertices && indices.size <= maxIndices) return listOf(this)
    // A triangle needs 3 indices and can introduce up to 3 vertices, so anything below that
    // cannot make progress -- emit as-is and let the caller's own capacity check report it.
    if (maxVertices < 3 || maxIndices < 3) return listOf(this)

    val pieces = ArrayList<UiColoredTriangleMesh>()
    var pieceVertices = ArrayList<UiColoredVertex>()
    var pieceIndices = ArrayList<Int>()
    var remap = HashMap<Int, Int>()

    fun flush() {
        if (pieceIndices.isEmpty()) return
        pieces += UiColoredTriangleMesh(pieceVertices.toList(), pieceIndices.toIntArray())
        pieceVertices = ArrayList()
        pieceIndices = ArrayList()
        remap = HashMap()
    }

    var at = 0
    while (at + 3 <= indices.size) {
        val triangle = intArrayOf(indices[at], indices[at + 1], indices[at + 2])
        val newVertices = triangle.distinct().count { it !in remap }
        if (pieceIndices.isNotEmpty() &&
            (pieceVertices.size + newVertices > maxVertices || pieceIndices.size + 3 > maxIndices)
        ) {
            flush()
        }
        triangle.forEach { source ->
            val mapped = remap.getOrPut(source) {
                pieceVertices += vertices[source]
                pieceVertices.size - 1
            }
            pieceIndices += mapped
        }
        at += 3
    }
    flush()
    return pieces
}

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

    /**
     * A rectangle whose corners round independently -- Compose's `RoundedCornerShape(topStart,
     * topEnd, bottomEnd, bottomStart)`.
     *
     * A segmented control needs its end members rounded on the outside and square where they meet
     * a neighbour; one uniform radius cannot say that. Rendered as a filled path rather than a
     * [UiDrawPrimitive.RoundedQuad], whose SDF carries a single radius -- and deliberately not by
     * clipping the group, which would eat children's focus rings.
     */
    data class RoundedCorners(
        val topLeft: Dp,
        val topRight: Dp,
        val bottomRight: Dp,
        val bottomLeft: Dp,
    ) : UiShapeSpec
}

fun UiShapeSpec.toPath(bounds: UiBounds, fillRule: UiFillRule = UiFillRule.NonZero): UiPath = when (this) {
    UiShapeSpec.Rectangle -> rectanglePath(bounds, fillRule)
    is UiShapeSpec.RoundedRectangle -> roundedRectanglePath(bounds, radius, fillRule)
    UiShapeSpec.Circle -> circlePath(bounds, fillRule)
    UiShapeSpec.Pill -> pillPath(bounds, fillRule)
    is UiShapeSpec.CutCorner -> cutCornerPath(bounds, size, fillRule)
    is UiShapeSpec.RoundedCorners -> roundedCornersPath(bounds, this, fillRule)
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
    // The largest corner governs -- the margin has to clear every one of them.
    is UiShapeSpec.RoundedCorners -> maxOf(
        topLeft.toPx(),
        topRight.toPx(),
        bottomRight.toPx(),
        bottomLeft.toPx(),
    ).coerceIn(0f, min(bounds.width, bounds.height) / 2f)
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

/** Lower/upper bound for [adaptiveCurveSteps]/[adaptiveArcSteps]'s chosen step count -- a floor
 * keeps a degenerate/zero-length curve from collapsing to a visible flat corner, a ceiling keeps
 * a huge curve (or a caller-supplied giant sweep) from generating an unbounded vertex count. */
private const val MIN_ADAPTIVE_STEPS = 4
private const val MAX_ADAPTIVE_STEPS = 64

/** Target max deviation (px) between a flattened polyline and the true curve/arc that
 * [adaptiveCurveSteps]/[adaptiveArcSteps] solve for -- sub-pixel on any display this engine
 * targets, comparable to the ~0.1-0.3px tessellation tolerance NanoVG/Skia use internally. */
private const val CURVE_FLATNESS_TOLERANCE_PX = 0.25f

/**
 * Step count for flattening one quad/cubic Bezier segment so its max deviation from the
 * resulting polyline stays under [CURVE_FLATNESS_TOLERANCE_PX]. A uniform-parameter
 * subdivision's deviation from the true curve shrinks like `1/steps^2`, and a curve's own
 * control-polygon length upper-bounds how far it can bow away from a single chord, so solving
 * `tolerance ~= controlNetLength / (8 * steps^2)` for steps gives
 * `steps = ceil(sqrt(controlNetLength / (8 * tolerance)))` -- more steps for a long/tight
 * control net, fewer for a short one (a 12px icon glyph), instead of one fixed count for both
 * (the shipped complaint: "a 400px circle facets, a 12px icon wastes segments"). [minSteps] is
 * `flattenContours`' `curveSteps` param, still a floor an explicit caller can raise above the
 * adaptive minimum.
 */
private fun adaptiveCurveSteps(controlNetLength: Float, minSteps: Int): Int {
    val estimate = ceil(sqrt(controlNetLength / (8f * CURVE_FLATNESS_TOLERANCE_PX)))
    val bounded = if (estimate.isFinite()) estimate.toInt() else MAX_ADAPTIVE_STEPS
    return bounded.coerceIn(minSteps.coerceIn(MIN_ADAPTIVE_STEPS, MAX_ADAPTIVE_STEPS), MAX_ADAPTIVE_STEPS)
}

/**
 * Step count for flattening one [UiPathCommand.ArcTo] sweep so its max sagitta (the chord's
 * bulge past the true arc) stays under [CURVE_FLATNESS_TOLERANCE_PX]. The sagitta of a chord
 * spanning angle `theta` on a circle of radius `r` is `r * (1 - cos(theta / 2))`; solving that
 * for `theta` at the tolerance gives the widest angle one step can span,
 * `theta = 2 * acos(1 - tolerance / r)` -- more steps for a big-radius arc (a large circular
 * avatar facets visibly at a fixed angle), fewer for a small one (a 2px button-corner radius
 * doesn't need the same step count as a full-size circle). [maxStepDegrees] is
 * `flattenContours`' `arcStepDegrees` param, kept as a per-step angle ceiling (== a step-count
 * floor) an explicit caller can still tighten.
 */
private fun adaptiveArcSteps(sweepDegrees: Float, radiusPx: Float, maxStepDegrees: Float): Int {
    val radius = radiusPx.coerceAtLeast(0f)
    val sagittaStepDegrees = if (radius <= CURVE_FLATNESS_TOLERANCE_PX) {
        Float.MAX_VALUE // sub-pixel radius: even a single step is already an invisible facet.
    } else {
        2f * acos(1f - CURVE_FLATNESS_TOLERANCE_PX / radius) * 180f / PI.toFloat()
    }
    val stepDegrees = min(sagittaStepDegrees, maxStepDegrees).coerceAtLeast(1f)
    return ceil(abs(sweepDegrees) / stepDegrees).toInt().coerceIn(MIN_ADAPTIVE_STEPS, MAX_ADAPTIVE_STEPS)
}

/**
 * Flattens every curve/arc command into line segments, adaptively: [curveSteps] (quad/cubic) and
 * [arcStepDegrees] (arc, default 90 -- a non-binding ceiling for every sweep this engine itself
 * emits, all of which are exactly 90 degrees corner arcs) no longer set one fixed count/angle for
 * every curve regardless of size -- see [adaptiveCurveSteps]/[adaptiveArcSteps]. They now act as
 * a floor an explicit caller can raise; the common (default-argument) case scales with each
 * curve's own size instead.
 */
fun UiPath.flattenContours(
    curveSteps: Int = MIN_ADAPTIVE_STEPS,
    arcStepDegrees: Float = 90f,
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
                val controlNetLength = hypot(command.cx - start.x, command.cy - start.y) +
                    hypot(command.x - command.cx, command.y - command.cy)
                val steps = adaptiveCurveSteps(controlNetLength, curveSteps)
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
                val controlNetLength = hypot(command.c1x - start.x, command.c1y - start.y) +
                    hypot(command.c2x - command.c1x, command.c2y - command.c1y) +
                    hypot(command.x - command.c2x, command.y - command.c2y)
                val steps = adaptiveCurveSteps(controlNetLength, curveSteps)
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
                val steps = adaptiveArcSteps(command.sweepDegrees, max(radiusX, radiusY), arcStepDegrees)
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
        if (contour.closed && contour.points.size >= 3) {
            closed += contour
        } else if (contour.points.size >= 3) {
            simple += UiFillGroup(contour.points, emptyList())
        }
    }
    if (closed.size <= 1) {
        closed.forEach { simple += UiFillGroup(it.points, emptyList()) }
        return simple
    }

    val areas = closed.map { polygonSignedArea(it.points) }
    // containers[i] = indices of contours containing contour i's first vertex.
    val containers = closed.indices.map { i ->
        closed.indices.filter { j -> j != i && closed[j].containsContour(closed[i]) }
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
 * Antialiased sibling of [tessellateFill] -- same interior triangulation strategy (see
 * [appendGroupFill]), run on each boundary INSET by half the fringe width, plus a thin (~1px)
 * "fringe" ring of triangles straddling each contour's TRUE boundary: full [color] alpha at
 * that inset edge, fading to 0 at the boundary outset by the other half. Centering the band on
 * the true geometry this way (inset interior + symmetric fringe, not an interior at full alpha
 * all the way to the edge plus an outward-only fade) is the standard vector-graphics AA
 * technique used by e.g. NanoVG ("feathering") -- coverage crosses 50% exactly at the path's
 * real outline instead of every filled path rendering about half the fringe width bolder than
 * its actual geometry. Blended through the UI quad pipeline's EXISTING
 * src-alpha/one-minus-src-alpha blend state
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
    val halfFringe = fringePx / 2f

    resolveFillGroups(contours, fillRule).forEach { group ->
        // Inset every boundary by half the fringe width for the interior mesh, and outset it by
        // the other half for the fringe's zero-alpha edge -- the band straddles the TRUE
        // boundary instead of sitting entirely outside it (see this function's doc comment).
        // Holes are already re-oriented opposite their outer in resolveFillGroups, so the same
        // winding-derived offsetPolygon call growing the hole polygon by fringePx/2 is exactly
        // the ring's interior insetting away from the hole edge.
        val insetOuter = offsetPolygon(group.outer, -halfFringe)
        val outsetOuter = offsetPolygon(group.outer, halfFringe)
        val insetHoles = group.holes.map { offsetPolygon(it, -halfFringe) }
        val outsetHoles = group.holes.map { offsetPolygon(it, halfFringe) }

        // Interior -- same triangulation strategy as tessellateFill (scanline for concave/holed
        // groups, centroid fan for convex; see appendGroupFill's doc comment), run on the inset
        // group, full-alpha color throughout.
        appendGroupFill(UiFillGroup(insetOuter, insetHoles), onTriangulated = { meshPoints, meshIndices ->
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

        appendBoundaryFringe(insetOuter, outsetOuter, color, transparent, vertices, indices)
        for (i in group.holes.indices) {
            appendBoundaryFringe(insetHoles[i], outsetHoles[i], color, transparent, vertices, indices)
        }
    }
    return UiColoredTriangleMesh(vertices, indices.toIntArray())
}

/** Miter-scale ceiling for [offsetPolygon] -- 4 matches the common SVG/Skia stroke miter-limit
 * default, chosen for the same reason: past this, a sharp/reflex corner's offset vertex is
 * clamped shorter than a true miter join rather than allowed to spike outward. */
private const val MAX_MITER_SCALE = 4f

/**
 * Moves every vertex of a closed [polygon] along its own outward normal by [distance] (negative
 * insets/shrinks, positive outsets/grows) -- backs [tessellateFillAa]'s symmetric AA fringe.
 * Per-vertex normals are the miter join of the two adjacent edges' unit normals (same
 * winding-derived convention the old per-edge fringe normal used): averaged, normalized, and
 * scaled by `1 / cos(halfAngle)` so the offset polygon comes out a clean, gap-free ring instead
 * of per-edge normals' small gap/overlap at each corner. Clamped by [MAX_MITER_SCALE] so a
 * near-180-degree turn (e.g. a chevron's reflex notch) doesn't spike the offset vertex towards
 * infinity, and by each vertex's own adjacent edge lengths so [distance] can never move a vertex
 * past its neighbor -- the interior can't self-intersect even on a polygon thinner than the
 * fringe (a tiny icon glyph), it just insets less there instead.
 */
private fun offsetPolygon(polygon: List<UiPoint>, distance: Float): List<UiPoint> {
    val n = polygon.size
    if (n < 3) return polygon
    val outwardSign = if (polygonSignedArea(polygon) >= 0f) 1f else -1f

    fun edgeNormal(a: UiPoint, b: UiPoint): Pair<Float, Float> {
        val dx = b.x - a.x
        val dy = b.y - a.y
        val length = hypot(dx, dy)
        if (length <= 0f) return 0f to 0f
        return (dy / length * outwardSign) to (-dx / length * outwardSign)
    }

    return polygon.indices.map { i ->
        val prev = polygon[(i - 1 + n) % n]
        val curr = polygon[i]
        val next = polygon[(i + 1) % n]
        val (prevNx, prevNy) = edgeNormal(prev, curr)
        val (nextNx, nextNy) = edgeNormal(curr, next)

        val sumX = prevNx + nextNx
        val sumY = prevNy + nextNy
        val sumLength = hypot(sumX, sumY)
        val (unitX, unitY, scale) = if (sumLength > 1e-4f) {
            val ux = sumX / sumLength
            val uy = sumY / sumLength
            val cosHalfAngle = prevNx * ux + prevNy * uy
            Triple(ux, uy, (1f / cosHalfAngle).coerceAtMost(MAX_MITER_SCALE))
        } else {
            // Adjacent edges point opposite ways (a near-180deg cusp): no shared miter direction
            // exists -- fall back to the incoming edge's own unit normal, unscaled.
            Triple(prevNx, prevNy, 1f)
        }

        val maxStep = 0.5f * min(
            hypot(curr.x - prev.x, curr.y - prev.y),
            hypot(next.x - curr.x, next.y - curr.y),
        )
        val offset = (distance * scale).coerceIn(-maxStep, maxStep)
        UiPoint(curr.x + unitX * offset, curr.y + unitY * offset)
    }
}

/**
 * Ring of triangles between two matched point lists -- same size, same winding, same per-vertex
 * correspondence, since both [innerRing] and [outerRing] are produced by [offsetPolygon] from
 * the same source polygon: full [color] alpha along [innerRing], fading to fully transparent
 * along [outerRing]. Sharing per-vertex miter directions between the two rings means consecutive
 * quads share vertices at every corner -- an exact, gap-free band (the old per-edge-normal
 * fringe left an invisible sub-pixel gap/overlap at each corner instead).
 */
private fun appendBoundaryFringe(
    innerRing: List<UiPoint>,
    outerRing: List<UiPoint>,
    color: Color,
    transparent: Color,
    vertices: ArrayList<UiColoredVertex>,
    indices: ArrayList<Int>,
) {
    val n = innerRing.size
    if (n < 3 || outerRing.size != n) return
    val base = vertices.size
    for (i in 0 until n) {
        vertices += UiColoredVertex(innerRing[i], color)
        vertices += UiColoredVertex(outerRing[i], transparent)
    }
    for (i in 0 until n) {
        val next = (i + 1) % n
        val innerA = base + i * 2
        val outerA = innerA + 1
        val innerB = base + next * 2
        val outerB = innerB + 1
        indices += innerA
        indices += innerB
        indices += outerB
        indices += outerB
        indices += outerA
        indices += innerA
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

/**
 * Converts a stroked centerline into an equivalent FILLED outline, so a stroke can be rendered
 * through [tessellateFill]/[tessellateFillAa] (real joins, real AA fringe, real capacity
 * splitting) instead of [tessellateStroke]'s independent per-segment quads, which have no join
 * geometry at all -- see [tessellateStroke]'s doc history. [tessellateStroke] itself is
 * untouched: its existing callers (border strokes, the debug overlay) keep the old behavior.
 *
 * Each open contour becomes ONE ring: walk the +halfWidth offset forward, round the tip (or cut
 * it flat for [UiStrokeCap.Butt]/[UiStrokeCap.Square]), walk the -halfWidth offset back, round
 * the other tip, close. At interior vertices the ring is rounded ([UiStrokeJoin.Round]) or cut
 * flat (otherwise) on BOTH sides, not just the convex side -- the concave side then
 * self-overlaps slightly, which [UiFillRule.NonZero]'s winding-number fill renders correctly
 * (the same property lets font rasterizers fill self-intersecting glyph outlines), so no
 * separate "which side is outer" test is needed.
 *
 * Each closed contour becomes an outer ring (+halfWidth) plus an inner ring (-halfWidth,
 * reversed so its winding is opposite the outer's) -- exactly the outer+hole shape
 * [resolveFillGroups] already detects for icon holes like Heroicons' clock/camera glyphs, so a
 * closed stroke (e.g. a circle) becomes a correct annulus for free.
 */
fun UiPath.strokeToFillPath(stroke: UiStroke): UiPath {
    val contours = flattenContours(curveSteps = 16, arcStepDegrees = 15f)
    // [UiStroke.width] is in the PATH's own coordinate space, not dp: an icon reaches here
    // through [UiImageVector.fitTo], which scales the path into device pixels and scales the
    // stroke width by the same factor. Converting again with `toPx()` applied density twice, so
    // every outline glyph came out proportionally heavier the higher the display scale -- at 2x
    // roughly double the weight Heroicons draws, at 3x the shapes closed into blobs.
    val halfWidth = stroke.width.value / 2f
    if (contours.isEmpty() || halfWidth <= 0f) return UiPath(fillRule = UiFillRule.NonZero, commands = emptyList())

    val commands = ArrayList<UiPathCommand>()
    fun emitRing(ring: List<UiPoint>) {
        if (ring.size < 3) return
        commands += UiPathCommand.MoveTo(ring[0].x, ring[0].y)
        for (i in 1 until ring.size) commands += UiPathCommand.LineTo(ring[i].x, ring[i].y)
        commands += UiPathCommand.Close
    }

    contours.forEach { contour ->
        if (contour.closed) {
            emitRing(offsetClosedRing(contour.points, halfWidth, stroke.join))
            emitRing(offsetClosedRing(contour.points, -halfWidth, stroke.join).asReversed())
        } else {
            emitRing(offsetOpenRing(contour.points, halfWidth, stroke))
        }
    }
    return UiPath(fillRule = UiFillRule.NonZero, commands = commands)
}

private fun unitDir(a: UiPoint, b: UiPoint): UiPoint? {
    val dx = b.x - a.x
    val dy = b.y - a.y
    val length = hypot(dx, dy)
    return if (length <= 0f) null else UiPoint(dx / length, dy / length)
}

/** Rotates a unit direction 90 degrees and scales by [distance] -- the same convention
 * [tessellateStroke]'s per-segment normal uses (`nx = -dy*halfWidth, ny = dx*halfWidth`), so a
 * positive [distance] lands on the same side [tessellateStroke] calls its stroke's "start+n"
 * side. Negative [distance] lands on the opposite side without needing a separate code path. */
private fun offsetVector(dir: UiPoint, distance: Float): UiPoint = UiPoint(-dir.y * distance, dir.x * distance)

/** Arc of points (both endpoints included) at [radius] from [center], sweeping from offset
 * vector [from] to offset vector [to] the SHORT way around -- the vectors' own angle is all that
 * matters, not their magnitude, so callers can pass any same-length offset pair. */
private fun arcBetween(center: UiPoint, radius: Float, from: UiPoint, to: UiPoint): List<UiPoint> {
    val piF = PI.toFloat()
    val fromAngle = atan2(from.y, from.x)
    val toAngle = atan2(to.y, to.x)
    val twoPi = 2f * piF
    var delta = toAngle - fromAngle
    while (delta > piF) delta -= twoPi
    while (delta < -piF) delta += twoPi
    val steps = adaptiveArcSteps(delta * 180f / piF, radius, 15f)
    return (0..steps).map { step ->
        val t = step / steps.toFloat()
        val angle = fromAngle + delta * t
        UiPoint(center.x + cos(angle) * radius, center.y + sin(angle) * radius)
    }
}

/**
 * Cap arc for a segment end traveling in [dir]: sweeps from the LEFT offset of [dir], through the
 * tip straight ahead in [dir]'s own direction, to the RIGHT offset -- always exactly a half turn
 * relative to [dir]'s own angle. [arcBetween]'s shortest-way-between-two-vectors logic is
 * ambiguous for a cap (its two endpoints are always exact opposites, +/-180 degrees both "the
 * short way") and float rounding on the antipodal angle difference can pick either winding --
 * this computes the sweep directly off [dir] instead of the endpoint vectors, so it is never
 * ambiguous. An end cap passes [dir] = the segment's own forward direction; a start cap passes
 * the reverse of the first segment's direction (the ring is walking backward there).
 */
private fun capSweep(round: Boolean, center: UiPoint, halfWidth: Float, dir: UiPoint): List<UiPoint> {
    if (!round) {
        val left = offsetVector(dir, halfWidth)
        val right = offsetVector(UiPoint(-dir.x, -dir.y), halfWidth)
        return listOf(UiPoint(center.x + left.x, center.y + left.y), UiPoint(center.x + right.x, center.y + right.y))
    }
    val piF = PI.toFloat()
    val fromAngle = atan2(dir.y, dir.x) + piF / 2f
    val steps = adaptiveArcSteps(180f, halfWidth, 15f)
    return (0..steps).map { step ->
        val angle = fromAngle - piF * (step / steps.toFloat())
        UiPoint(center.x + cos(angle) * halfWidth, center.y + sin(angle) * halfWidth)
    }
}

/**
 * One side (or, for a butt/square cap, one flat cut) of a stroke ring's tip/join: rounds it when
 * [round] asks for that, otherwise emits the two raw offset points -- a flat cut across the join
 * (a bevel), the same corner [tessellateStroke] leaves today, kept as the non-round fallback.
 */
private fun ringCorner(round: Boolean, center: UiPoint, halfWidth: Float, fromDir: UiPoint, toDir: UiPoint): List<UiPoint> {
    val from = offsetVector(fromDir, halfWidth)
    val to = offsetVector(toDir, halfWidth)
    return if (round) {
        arcBetween(center, halfWidth, from, to)
    } else {
        listOf(UiPoint(center.x + from.x, center.y + from.y), UiPoint(center.x + to.x, center.y + to.y))
    }
}

/** Builds the single self-intersecting ring described in [strokeToFillPath]'s doc comment for
 * one OPEN flattened contour. Assumes no zero-length segments -- guaranteed by
 * [UiPath.flattenContours], which already dedupes consecutive identical points. */
private fun offsetOpenRing(points: List<UiPoint>, halfWidth: Float, stroke: UiStroke): List<UiPoint> {
    if (points.size < 2) return emptyList()
    val dirs = ArrayList<UiPoint>(points.size - 1)
    for (i in 0 until points.size - 1) dirs += unitDir(points[i], points[i + 1]) ?: return emptyList()

    var pts = points
    if (stroke.cap == UiStrokeCap.Square) {
        val extendedFirst = UiPoint(pts.first().x - dirs.first().x * halfWidth, pts.first().y - dirs.first().y * halfWidth)
        val extendedLast = UiPoint(pts.last().x + dirs.last().x * halfWidth, pts.last().y + dirs.last().y * halfWidth)
        pts = listOf(extendedFirst) + pts.subList(1, pts.size - 1) + listOf(extendedLast)
    }
    val round = stroke.join == UiStrokeJoin.Round
    val roundCap = stroke.cap == UiStrokeCap.Round

    val ring = ArrayList<UiPoint>()
    // Left side (+halfWidth), first point to last.
    ring += UiPoint(pts.first().x + offsetVector(dirs.first(), halfWidth).x, pts.first().y + offsetVector(dirs.first(), halfWidth).y)
    for (i in 1 until dirs.size) ring += ringCorner(round, pts[i], halfWidth, dirs[i - 1], dirs[i])
    // End tip: left offset -> right offset, bulging away from the polyline in dirs.last()'s
    // own direction (see capSweep's doc comment for why this can't reuse the generic
    // shortest-arc-between-two-vectors helper).
    ring += capSweep(roundCap, pts.last(), halfWidth, dirs.last())
    // Right side (-halfWidth, expressed as +halfWidth on the reversed direction), last point back to first.
    for (i in dirs.size - 2 downTo 0) {
        ring += ringCorner(round, pts[i + 1], halfWidth, UiPoint(-dirs[i + 1].x, -dirs[i + 1].y), UiPoint(-dirs[i].x, -dirs[i].y))
    }
    // Start tip: right offset -> left offset, bulging away in the reverse of dirs.first() --
    // closes the ring back to its first point.
    ring += capSweep(roundCap, pts.first(), halfWidth, UiPoint(-dirs.first().x, -dirs.first().y))
    return ring
}

/** Builds one offset ring (outer when [distance] > 0, inner when < 0) for one CLOSED flattened
 * contour, rounding every vertex when [join] asks for that. Drops a trailing point that
 * coincides with the first: a source path that explicitly draws all the way back to its start
 * before `close()` (e.g. a full circle traced by arcs, like Heroicons' clock/camera glyphs) ends
 * [UiPath.flattenContours]' point list with that same point twice, and [unitDir] on the
 * resulting zero-length wraparound segment would otherwise reject the whole contour. */
private fun offsetClosedRing(rawPoints: List<UiPoint>, distance: Float, join: UiStrokeJoin): List<UiPoint> {
    val points = if (rawPoints.size >= 2 && unitDir(rawPoints.last(), rawPoints.first()) == null) {
        rawPoints.dropLast(1)
    } else {
        rawPoints
    }
    val n = points.size
    if (n < 3) return emptyList()
    val dirs = (0 until n).map { i -> unitDir(points[i], points[(i + 1) % n]) ?: return emptyList() }
    val round = join == UiStrokeJoin.Round
    val ring = ArrayList<UiPoint>()
    for (i in 0 until n) {
        val fromDir = dirs[(i - 1 + n) % n]
        val toDir = dirs[i]
        ring += ringCorner(round, points[i], abs(distance), scaleDir(fromDir, distance), scaleDir(toDir, distance))
    }
    return ring
}

/** [ringCorner] takes unit directions and multiplies by `halfWidth` (always positive) inside
 * [offsetVector]; a negative (inner) [offsetClosedRing] distance needs the SIGN folded into the
 * direction instead, which this does without changing [ringCorner]'s "always offset by a
 * positive half-width" contract. */
private fun scaleDir(dir: UiPoint, distance: Float): UiPoint = if (distance >= 0f) dir else UiPoint(-dir.x, -dir.y)

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

/**
 * Whether [inner] sits inside this contour, judged from a point genuinely interior to [inner]
 * rather than from one of its vertices.
 *
 * Probing a vertex is what a first cut does, and it fails on real icon data: Heroicons'
 * `arrow-down-on-square-stack` has two subpaths whose first vertices are the *same* point, so a
 * vertex probe lands exactly on both boundaries, where the winding number is undefined -- the
 * arrow was then mis-classified against the square it cuts out of, and the glyph rendered as a
 * bare hook. Edge midpoints nudged along the edge normal give points off both boundaries;
 * several are tried because any one can still land on a coincident edge where contours touch.
 */
private val NORMAL_SIDES = floatArrayOf(-1f, 1f)

private fun UiPathContour.containsContour(inner: UiPathContour): Boolean {
    val nudge = 0.01f
    var tried = 0
    for (index in inner.points.indices) {
        val a = inner.points[index]
        val b = inner.points[(index + 1) % inner.points.size]
        var dx = b.x - a.x
        var dy = b.y - a.y
        val length = hypot(dx, dy)
        if (length <= 0f) continue
        dx /= length
        dy /= length
        val midX = (a.x + b.x) / 2f
        val midY = (a.y + b.y) / 2f
        // Try both normals: this contour's winding direction is not known here.
        for (side in NORMAL_SIDES) {
            val px = midX + dy * nudge * side
            val py = midY - dx * nudge * side
            // Only trust a probe that is unambiguously inside `inner` itself.
            if (inner.windingContribution(px, py) == 0) continue
            return windingContribution(px, py) != 0
        }
        tried += 1
        if (tried >= 32) break
    }
    return false
}

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

/**
 * [roundedRectanglePath] with a radius per corner. Each is clamped to half the shorter side on
 * its own, so one large corner cannot eat the edge its neighbour needs; a zero radius emits the
 * corner point.
 */
private fun roundedCornersPath(
    bounds: UiBounds,
    spec: UiShapeSpec.RoundedCorners,
    fillRule: UiFillRule,
): UiPath {
    val limit = min(bounds.width, bounds.height) / 2f
    val topLeft = spec.topLeft.toPx().coerceIn(0f, limit)
    val topRight = spec.topRight.toPx().coerceIn(0f, limit)
    val bottomRight = spec.bottomRight.toPx().coerceIn(0f, limit)
    val bottomLeft = spec.bottomLeft.toPx().coerceIn(0f, limit)

    val left = bounds.x
    val top = bounds.y
    val right = bounds.x + bounds.width
    val bottom = bounds.y + bounds.height

    return uiPath(fillRule) {
        moveTo(left + topLeft, top)
        lineTo(right - topRight, top)
        if (topRight > 0f) arcTo(right - 2f * topRight, top, right, top + 2f * topRight, -90f, 90f)
        lineTo(right, bottom - bottomRight)
        if (bottomRight > 0f) {
            arcTo(right - 2f * bottomRight, bottom - 2f * bottomRight, right, bottom, 0f, 90f)
        }
        lineTo(left + bottomLeft, bottom)
        if (bottomLeft > 0f) arcTo(left, bottom - 2f * bottomLeft, left + 2f * bottomLeft, bottom, 90f, 90f)
        lineTo(left, top + topLeft)
        if (topLeft > 0f) arcTo(left, top, left + 2f * topLeft, top + 2f * topLeft, 180f, 90f)
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
