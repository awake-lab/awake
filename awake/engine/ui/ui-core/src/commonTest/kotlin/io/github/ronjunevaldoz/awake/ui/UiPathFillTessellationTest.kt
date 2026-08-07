// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Locks the fill-tessellation contract for concave contours: a centroid fan is only valid for
 * convex polygons -- for a chevron-shaped (concave) contour the centroid sits outside the
 * polygon, in the notch, and a fan from it overfills the notch (the shipped "icons not crisp"
 * bug). Concave contours must ear-clip instead.
 */
class UiPathFillTessellationTest {

    /** The chevron-down ribbon as a minimal concave hexagon: one reflex vertex at (8, 8.8). */
    private fun chevronPath(): UiPath = uiPath {
        moveTo(4f, 4.8f)
        lineTo(8f, 8.8f)
        lineTo(12f, 4.8f)
        lineTo(13.2f, 6f)
        lineTo(8f, 11.2f)
        lineTo(2.8f, 6f)
        close()
    }

    @Test
    fun concaveChevronFillDoesNotOverfillTheNotch() {
        val mesh = chevronPath().tessellateFill()
        assertTrue(mesh.indices.size >= 3, "expected a non-empty mesh")
        // (8, 7) sits in the chevron's notch -- inside the old centroid fan, outside the glyph.
        assertFalse(
            trianglesCover(mesh.points, mesh.indices, 8f, 7f),
            "notch point (8, 7) must not be covered -- centroid-fan overfill regressed",
        )
        // (8, 10) sits inside the ribbon below the reflex vertex.
        assertTrue(
            trianglesCover(mesh.points, mesh.indices, 8f, 10f),
            "interior point (8, 10) must be covered",
        )
    }

    @Test
    fun concaveFillAreaMatchesPolygonArea() {
        val path = chevronPath()
        val polygon = path.flattenContours().single().points
        val expected = abs(shoelaceArea(polygon))
        val mesh = path.tessellateFill()
        var actual = 0f
        var at = 0
        while (at + 3 <= mesh.indices.size) {
            val a = mesh.points[mesh.indices[at]]
            val b = mesh.points[mesh.indices[at + 1]]
            val c = mesh.points[mesh.indices[at + 2]]
            actual += abs((b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)) / 2f
            at += 3
        }
        // A centroid fan on this shape sums to MORE than the polygon's area (the notch gets
        // covered too); an exact triangulation matches it.
        assertTrue(
            abs(actual - expected) < 0.01f,
            "triangulated area $actual must equal polygon area $expected",
        )
    }

    @Test
    fun convexContourKeepsCentroidFan() {
        val square = uiPath {
            moveTo(0f, 0f)
            lineTo(10f, 0f)
            lineTo(10f, 10f)
            lineTo(0f, 10f)
            close()
        }
        val mesh = square.tessellateFill()
        // Centroid fan = 1 centroid vertex + the polygon's own points; ear clipping would have
        // no extra vertex. The fan is deliberate for convex shapes (see tessellateFill's
        // sliver-triangle comment) -- this locks the fast path.
        assertEquals(square.flattenContours().single().points.size + 1, mesh.points.size)
    }

    @Test
    fun degenerateContourStillProducesAMeshWithoutCrashing() {
        val collinear = uiPath {
            moveTo(0f, 0f)
            lineTo(5f, 0f)
            lineTo(10f, 0f)
            close()
        }
        // Zero-area input: ear clipping bails, centroid-fan fallback still returns something.
        collinear.tessellateFill()
    }

    /** Outer 20x20 square with a 10x10 inner square, both wound the same way (how SVG evenodd
     * sources -- e.g. Heroicons clock/camera -- author holes). */
    private fun ringPath(fillRule: UiFillRule, reverseInner: Boolean = false): UiPath = uiPath(fillRule) {
        moveTo(0f, 0f)
        lineTo(20f, 0f)
        lineTo(20f, 20f)
        lineTo(0f, 20f)
        close()
        if (reverseInner) {
            moveTo(5f, 5f)
            lineTo(5f, 15f)
            lineTo(15f, 15f)
            lineTo(15f, 5f)
            close()
        } else {
            moveTo(5f, 5f)
            lineTo(15f, 5f)
            lineTo(15f, 15f)
            lineTo(5f, 15f)
            close()
        }
    }

    @Test
    fun evenOddRingLeavesTheHoleEmpty() {
        val mesh = ringPath(UiFillRule.EvenOdd).tessellateFill()
        assertFalse(
            trianglesCover(mesh.points, mesh.indices, 10f, 10f),
            "hole center (10, 10) must not be covered",
        )
        assertTrue(trianglesCover(mesh.points, mesh.indices, 2f, 10f), "ring interior must be covered")
        assertTrue(trianglesCover(mesh.points, mesh.indices, 10f, 2f), "ring interior must be covered")
        var area = 0f
        var at = 0
        while (at + 3 <= mesh.indices.size) {
            val a = mesh.points[mesh.indices[at]]
            val b = mesh.points[mesh.indices[at + 1]]
            val c = mesh.points[mesh.indices[at + 2]]
            area += abs((b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)) / 2f
            at += 3
        }
        assertTrue(abs(area - 300f) < 0.01f, "ring area must be 400 - 100 = 300, was $area")
    }

    @Test
    fun nonZeroOppositeWindingRingLeavesTheHoleEmpty() {
        val mesh = ringPath(UiFillRule.NonZero, reverseInner = true).tessellateFill()
        assertFalse(
            trianglesCover(mesh.points, mesh.indices, 10f, 10f),
            "hole center (10, 10) must not be covered",
        )
        assertTrue(trianglesCover(mesh.points, mesh.indices, 2f, 10f), "ring interior must be covered")
    }

    @Test
    fun nonZeroSameWindingNestedContourStillFills() {
        // Per the winding rule a same-winding nested contour is NOT a hole.
        val mesh = ringPath(UiFillRule.NonZero).tessellateFill()
        assertTrue(
            trianglesCover(mesh.points, mesh.indices, 10f, 10f),
            "same-winding nested contour must keep filling under NonZero",
        )
    }

    @Test
    fun evenOddRingAaInteriorDoesNotCoverHole() {
        val color = Color.White
        val mesh = ringPath(UiFillRule.EvenOdd).tessellateFillAa(color)
        val interiorPoints = ArrayList<UiPoint>()
        val interiorIndices = ArrayList<Int>()
        var at = 0
        while (at + 3 <= mesh.indices.size) {
            val ia = mesh.indices[at]
            val ib = mesh.indices[at + 1]
            val ic = mesh.indices[at + 2]
            if (listOf(ia, ib, ic).all { mesh.vertices[it].color.a == color.a }) {
                val base = interiorPoints.size
                interiorPoints += mesh.vertices[ia].position
                interiorPoints += mesh.vertices[ib].position
                interiorPoints += mesh.vertices[ic].position
                interiorIndices += base
                interiorIndices += base + 1
                interiorIndices += base + 2
            }
            at += 3
        }
        assertFalse(
            trianglesCover(interiorPoints, interiorIndices.toIntArray(), 10f, 10f),
            "AA interior must not cover the hole center",
        )
        assertTrue(
            trianglesCover(interiorPoints, interiorIndices.toIntArray(), 2f, 10f),
            "AA interior must cover the ring",
        )
    }

    @Test
    fun concaveChevronAaInteriorDoesNotCoverNotch() {
        val color = Color.White
        val mesh = chevronPath().tessellateFillAa(color)
        // Interior triangles are the all-opaque ones (fringe quads carry transparent vertices).
        val interiorPoints = ArrayList<UiPoint>()
        val interiorIndices = ArrayList<Int>()
        var at = 0
        while (at + 3 <= mesh.indices.size) {
            val ia = mesh.indices[at]
            val ib = mesh.indices[at + 1]
            val ic = mesh.indices[at + 2]
            if (listOf(ia, ib, ic).all { mesh.vertices[it].color.a == color.a }) {
                val base = interiorPoints.size
                interiorPoints += mesh.vertices[ia].position
                interiorPoints += mesh.vertices[ib].position
                interiorPoints += mesh.vertices[ic].position
                interiorIndices += base
                interiorIndices += base + 1
                interiorIndices += base + 2
            }
            at += 3
        }
        assertTrue(interiorIndices.isNotEmpty(), "expected opaque interior triangles")
        assertFalse(
            trianglesCover(interiorPoints, interiorIndices.toIntArray(), 8f, 7f),
            "AA interior must not cover the notch point (8, 7)",
        )
        assertTrue(
            trianglesCover(interiorPoints, interiorIndices.toIntArray(), 8f, 10f),
            "AA interior must cover the ribbon point (8, 10)",
        )
    }

    /**
     * Locks the "symmetric fringe" fix: the old fringe offset only outward from the true
     * boundary (100% opaque AT the edge, fading to 0 further out), which rendered every filled
     * path about half the fringe width bolder than its geometry. The fringe must now straddle
     * the true boundary -- opaque interior starts only at the INSET edge (fringePx/2 inside the
     * true edge), not at the true edge itself.
     */
    @Test
    fun aaFringeCentersOnTrueBoundaryForALargeSquare() {
        val color = Color.White
        val fringe = 1f
        val halfFringe = fringe / 2f
        val square = uiPath {
            moveTo(0f, 0f)
            lineTo(100f, 0f)
            lineTo(100f, 100f)
            lineTo(0f, 100f)
            close()
        }
        val mesh = square.tessellateFillAa(color, fringePx = fringe)
        val (opaquePoints, opaqueIndices) = opaqueTriangles(mesh, color.a)

        // 0.1px inside the true left edge (x=0) but still outside the inset edge (x=0.5): must
        // NOT be opaque -- the old outward-only fringe was fully opaque all the way to x=0.
        assertFalse(
            trianglesCover(opaquePoints, opaqueIndices, halfFringe - 0.4f, 50f),
            "opaque interior must not reach 0.1px inside the true edge -- fringe is not centered",
        )
        // Comfortably past the inset edge: must be opaque.
        assertTrue(
            trianglesCover(opaquePoints, opaqueIndices, 10f, 50f),
            "opaque interior must cover a point well inside the inset boundary",
        )

        // The fringe band itself must straddle the true boundary (x=0), not sit entirely
        // outside it.
        val allPoints = mesh.vertices.map { it.position }
        assertTrue(
            trianglesCover(allPoints, mesh.indices, 0f, 50f),
            "fringe band must span the true boundary (x=0)",
        )
    }

    /** A 400px circle should facet far less than a fixed-step-count flattener would (visibly
     * faceted at the old fixed 15deg/step), and a 12px circle shouldn't burn the same segment
     * count on a curve where it's imperceptible -- see UiPath.adaptiveArcSteps. */
    @Test
    fun adaptiveFlatteningScalesArcStepsWithCircleSize() {
        val bigCircle = UiShapeSpec.Circle.toPath(UiBounds(0f, 0f, 400f, 400f))
        val bigSegments = bigCircle.flattenContours().single().points.size
        assertTrue(bigSegments >= 40, "400px circle should flatten to >=40 segments, was $bigSegments")

        val smallCircle = UiShapeSpec.Circle.toPath(UiBounds(0f, 0f, 12f, 12f))
        val smallSegments = smallCircle.flattenContours().single().points.size
        assertTrue(smallSegments <= 24, "12px circle should flatten to <=24 segments, was $smallSegments")
    }
}

/** Sub-mesh of only the fully-opaque (alpha == [alpha]) triangles -- the AA interior, excluding
 * fringe triangles which carry at least one transparent vertex. */
private fun opaqueTriangles(mesh: UiColoredTriangleMesh, alpha: Float): Pair<List<UiPoint>, IntArray> {
    val points = ArrayList<UiPoint>()
    val indices = ArrayList<Int>()
    var at = 0
    while (at + 3 <= mesh.indices.size) {
        val ia = mesh.indices[at]
        val ib = mesh.indices[at + 1]
        val ic = mesh.indices[at + 2]
        if (listOf(ia, ib, ic).all { mesh.vertices[it].color.a == alpha }) {
            val base = points.size
            points += mesh.vertices[ia].position
            points += mesh.vertices[ib].position
            points += mesh.vertices[ic].position
            indices += base
            indices += base + 1
            indices += base + 2
        }
        at += 3
    }
    return points to indices.toIntArray()
}

private fun trianglesCover(points: List<UiPoint>, indices: IntArray, x: Float, y: Float): Boolean {
    var at = 0
    while (at + 3 <= indices.size) {
        val a = points[indices[at]]
        val b = points[indices[at + 1]]
        val c = points[indices[at + 2]]
        val d1 = (b.x - a.x) * (y - a.y) - (b.y - a.y) * (x - a.x)
        val d2 = (c.x - b.x) * (y - b.y) - (c.y - b.y) * (x - b.x)
        val d3 = (a.x - c.x) * (y - c.y) - (a.y - c.y) * (x - c.x)
        val hasNegative = d1 < 0f || d2 < 0f || d3 < 0f
        val hasPositive = d1 > 0f || d2 > 0f || d3 > 0f
        if (!(hasNegative && hasPositive)) return true
        at += 3
    }
    return false
}

private fun shoelaceArea(points: List<UiPoint>): Float {
    var sum = 0f
    for (i in points.indices) {
        val a = points[i]
        val b = points[(i + 1) % points.size]
        sum += a.x * b.y - b.x * a.y
    }
    return sum / 2f
}
