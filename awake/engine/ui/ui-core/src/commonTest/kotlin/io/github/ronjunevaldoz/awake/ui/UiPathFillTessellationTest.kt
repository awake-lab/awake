// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
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
