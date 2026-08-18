// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.dp
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Locks the outline-icon stroke-to-fill conversion (`strokeToFillPath` in UiPath.kt) against a
 * synthetic L-shaped 3-point open polyline, round join + round cap: closed ring, area matches
 * the Minkowski-dilation formula for a round-jointed, round-capped stroke, plus a PNG for a
 * by-eye check at `build/ui-core-probes/l-shape-stroke.png`.
 */
class StrokeToFillPathProbe {
    @Test
    fun lShapedStrokeIsClosedAndHasExpectedArea() {
        val width = 2f
        val radius = width / 2f
        val path = UiPath.build {
            moveTo(0f, 0f)
            lineTo(10f, 0f)
            lineTo(10f, 10f)
        }
        val stroke = UiStroke(width = width.dp, cap = UiStrokeCap.Round, join = UiStrokeJoin.Round)
        val outline = path.strokeToFillPath(stroke)

        // Closed: every subpath ends in Close, and there is exactly one subpath (one open contour).
        assertEquals(1, outline.commands.count { it == UiPathCommand.Close })
        assertTrue(outline.commands.first() is UiPathCommand.MoveTo)
        assertTrue(outline.commands.last() == UiPathCommand.Close)

        // Minkowski dilation of the 2-segment, 90-degree polyline by a disk of `radius`:
        // 2*r*totalLength (the two segment rectangles) + r^2*turnAngle/2 (the one round join's
        // wedge) + 2*(pi*r^2/2) (the two round caps' half-disks).
        val totalLength = 20f
        val turnAngle = (PI / 2).toFloat()
        val expectedArea = 2 * radius * totalLength + radius * radius * turnAngle / 2f + 2 * (PI.toFloat() * radius * radius / 2f)

        val mesh = outline.tessellateFill()
        val actualArea = mesh.signedTriangleAreaSum()
        writeProbePng(outline, File("build/ui-core-probes/l-shape-stroke.png"))
        // Chord-flattened arcs always slightly UNDER-estimate a true circular sector's area, so
        // some shortfall is expected -- 5% tolerance is generous for radius=1 (a small icon-scale
        // stroke); real icon strokes at 24dp radii will be far closer to the analytic figure.
        assertTrue(
            kotlin.math.abs(actualArea - expectedArea) < expectedArea * 0.05f,
            "expected area ~$expectedArea, got $actualArea",
        )
    }

    /**
     * Regression for a real bug: a CLOSED contour whose source path explicitly draws all the way
     * back to its start point before calling `close()` (e.g. a full circle traced by arcs --
     * Heroicons' clock/camera outline glyphs both do this) makes [UiPath.flattenContours] end the
     * point list with that same point twice. `offsetClosedRing`'s wraparound segment was then
     * zero-length, and it bailed out to an empty ring for the WHOLE contour -- clock's face
     * circle and both of camera's closed shapes rendered as nothing at all.
     */
    @Test
    fun closedContourWithExplicitReturnToStartRendersAsAHollowBand() {
        val halfWidth = 1f
        val path = UiPath.build {
            moveTo(0f, 0f)
            lineTo(10f, 0f)
            lineTo(10f, 10f)
            lineTo(0f, 10f)
            lineTo(0f, 0f) // explicit return to the start point, as a traced-arc circle would be
            close()
        }
        val stroke = UiStroke(width = (halfWidth * 2).dp, cap = UiStrokeCap.Butt, join = UiStrokeJoin.Round)
        val outline = path.strokeToFillPath(stroke)

        assertEquals(2, outline.commands.count { it == UiPathCommand.Close }, "outer + inner ring")
        assertTrue(outline.containsPoint(0f, 5f), "the stroke band itself should be filled")
        assertTrue(!outline.containsPoint(5f, 5f), "the hollow interior should stay empty")
        assertTrue(!outline.containsPoint(-5f, -5f), "well outside the shape should stay empty")

        writeProbePng(outline, File("build/ui-core-probes/closed-square-stroke.png"))
    }

    private fun UiTriangleMesh.signedTriangleAreaSum(): Float {
        var area = 0f
        var i = 0
        while (i + 2 < indices.size) {
            val a = points[indices[i]]
            val b = points[indices[i + 1]]
            val c = points[indices[i + 2]]
            area += kotlin.math.abs((b.x - a.x) * (c.y - a.y) - (c.x - a.x) * (b.y - a.y)) / 2f
            i += 3
        }
        return area
    }

    private fun writeProbePng(path: UiPath, out: File) {
        val size = 40
        val scale = 2f
        val margin = 4f
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        for (py in 0 until size) {
            for (px in 0 until size) {
                val x = (px - margin) / scale
                val y = (py - margin) / scale
                val inside = path.containsPoint(x, y)
                image.setRGB(px, py, if (inside) 0xFF000000.toInt() or 0x2563EB else 0x00000000)
            }
        }
        out.parentFile?.mkdirs()
        ImageIO.write(image, "png", out)
    }
}
