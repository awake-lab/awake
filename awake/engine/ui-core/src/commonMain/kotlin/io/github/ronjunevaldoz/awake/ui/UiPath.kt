// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

enum class UiFillRule {
    NonZero,
    EvenOdd
}

enum class UiStrokeCap {
    Butt,
    Round,
    Square
}

enum class UiStrokeJoin {
    Miter,
    Round,
    Bevel
}

data class UiStroke(
    val width: Dp = 1f.dp,
    val cap: UiStrokeCap = UiStrokeCap.Butt,
    val join: UiStrokeJoin = UiStrokeJoin.Miter
)

data class UiPoint(
    val x: Float,
    val y: Float
)

data class UiPathContour(
    val points: List<UiPoint>,
    val closed: Boolean
)

data class UiTriangleMesh(
    val points: List<UiPoint>,
    val indices: IntArray
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
        val y: Float
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
        val sweepDegrees: Float
    ) : UiPathCommand

    data object Close : UiPathCommand
}

data class UiPath(
    val fillRule: UiFillRule = UiFillRule.NonZero,
    val commands: List<UiPathCommand>
) {
    companion object {
        fun build(fillRule: UiFillRule = UiFillRule.NonZero, block: UiPathBuilder.() -> Unit): UiPath =
            uiPath(fillRule, block)
    }
}

class UiPathBuilder internal constructor(
    private val fillRule: UiFillRule
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

fun UiShapeSpec.toPath(bounds: UiSlot, fillRule: UiFillRule = UiFillRule.NonZero): UiPath = when (this) {
    UiShapeSpec.Rectangle -> rectanglePath(bounds, fillRule)
    is UiShapeSpec.RoundedRectangle -> roundedRectanglePath(bounds, radius, fillRule)
    UiShapeSpec.Circle -> circlePath(bounds, fillRule)
    UiShapeSpec.Pill -> pillPath(bounds, fillRule)
    is UiShapeSpec.CutCorner -> cutCornerPath(bounds, size, fillRule)
}

fun UiPath.bounds(): UiSlot {
    if (commands.isEmpty()) return UiSlot(0f, 0f, 0f, 0f)

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
        return UiSlot(0f, 0f, 0f, 0f)
    }
    return UiSlot(minX, minY, (maxX - minX).coerceAtLeast(0f), (maxY - minY).coerceAtLeast(0f))
}

fun UiPath.flattenContours(
    curveSteps: Int = 8,
    arcStepDegrees: Float = 15f
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
                            y = oneMinusT * oneMinusT * start.y + 2f * oneMinusT * t * command.cy + t * t * command.y
                        )
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
                                t * t * t * command.y
                        )
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
                            y = centerY + sin(angleRadians) * radiusY
                        )
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

fun UiPath.tessellateFill(): UiTriangleMesh {
    val contours = flattenContours()
    if (contours.isEmpty()) return UiTriangleMesh(emptyList(), IntArray(0))

    val points = ArrayList<UiPoint>()
    val indices = ArrayList<Int>()
    contours.forEach { contour ->
        val polygon = contour.points
        if (polygon.size < 3) return@forEach
        val base = points.size
        points += polygon
        for (i in 1 until polygon.lastIndex) {
            indices += base
            indices += base + i
            indices += base + i + 1
        }
    }
    return UiTriangleMesh(points, indices.toIntArray())
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

fun UiTriangleMesh.clipToConvexPaths(paths: List<UiPath>): UiTriangleMesh {
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
            points[indices[index + 2]]
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
        if (sign == 0) sign = currentSign
        else if (sign != currentSign) return false
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

private fun rectanglePath(bounds: UiSlot, fillRule: UiFillRule): UiPath = uiPath(fillRule) {
    moveTo(bounds.x, bounds.y)
    lineTo(bounds.x + bounds.width, bounds.y)
    lineTo(bounds.x + bounds.width, bounds.y + bounds.height)
    lineTo(bounds.x, bounds.y + bounds.height)
    close()
}

private fun roundedRectanglePath(bounds: UiSlot, radius: Dp, fillRule: UiFillRule): UiPath {
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

private fun circlePath(bounds: UiSlot, fillRule: UiFillRule): UiPath {
    val diameter = min(bounds.width, bounds.height)
    val insetX = (bounds.width - diameter) / 2f
    val insetY = (bounds.height - diameter) / 2f
    return roundedRectanglePath(
        bounds = UiSlot(bounds.x + insetX, bounds.y + insetY, diameter, diameter),
        radius = (diameter / 2f).px,
        fillRule = fillRule
    )
}

private fun pillPath(bounds: UiSlot, fillRule: UiFillRule): UiPath {
    val radiusPx = min(bounds.width, bounds.height) / 2f
    return roundedRectanglePath(bounds, radiusPx.px, fillRule)
}

private fun cutCornerPath(bounds: UiSlot, size: Dp, fillRule: UiFillRule): UiPath {
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
