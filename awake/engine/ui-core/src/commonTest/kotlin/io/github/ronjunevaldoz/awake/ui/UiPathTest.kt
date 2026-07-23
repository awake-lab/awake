// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UiPathTest {

    @Test
    fun builderPreservesCommandOrder() {
        val path = uiPath(UiFillRule.EvenOdd) {
            moveTo(1f, 2f)
            lineTo(3f, 4f)
            quadTo(5f, 6f, 7f, 8f)
            cubicTo(9f, 10f, 11f, 12f, 13f, 14f)
            arcTo(15f, 16f, 17f, 18f, 90f, 180f)
            close()
        }

        assertEquals(UiFillRule.EvenOdd, path.fillRule)
        assertEquals(
            listOf(
                UiPathCommand.MoveTo(1f, 2f),
                UiPathCommand.LineTo(3f, 4f),
                UiPathCommand.QuadTo(5f, 6f, 7f, 8f),
                UiPathCommand.CubicTo(9f, 10f, 11f, 12f, 13f, 14f),
                UiPathCommand.ArcTo(15f, 16f, 17f, 18f, 90f, 180f),
                UiPathCommand.Close
            ),
            path.commands
        )
    }

    @Test
    fun roundedRectangleExpandsToDeterministicArcSequence() {
        val path = UiShapeSpec.RoundedRectangle(8f.dp).toPath(UiSlot(0f, 0f, 40f, 20f))

        assertEquals(
            listOf(
                UiPathCommand.MoveTo(8f, 0f),
                UiPathCommand.LineTo(32f, 0f),
                UiPathCommand.ArcTo(24f, 0f, 40f, 16f, -90f, 90f),
                UiPathCommand.LineTo(40f, 12f),
                UiPathCommand.ArcTo(24f, 4f, 40f, 20f, 0f, 90f),
                UiPathCommand.LineTo(8f, 20f),
                UiPathCommand.ArcTo(0f, 4f, 16f, 20f, 90f, 90f),
                UiPathCommand.LineTo(0f, 8f),
                UiPathCommand.ArcTo(0f, 0f, 16f, 16f, 180f, 90f),
                UiPathCommand.Close
            ),
            path.commands
        )
    }

    @Test
    fun circleUsesCenteredSquareWithinNonSquareBounds() {
        val path = UiShapeSpec.Circle.toPath(UiSlot(0f, 0f, 40f, 20f))

        assertEquals(UiPathCommand.MoveTo(20f, 0f), path.commands.first())
        assertEquals(
            UiPathCommand.ArcTo(10f, 0f, 30f, 20f, -90f, 90f),
            path.commands[2],
            "circle should be centered using the largest inscribed square"
        )
    }

    @Test
    fun pillClampsToHalfOfShortestDimension() {
        val path = UiShapeSpec.Pill.toPath(UiSlot(0f, 0f, 60f, 20f))

        assertEquals(UiPathCommand.MoveTo(10f, 0f), path.commands.first())
        assertEquals(UiPathCommand.LineTo(50f, 0f), path.commands[1])
    }

    @Test
    fun customShapeSpecOverridesLegacyRadiusInStyleResolution() {
        val resolved = Style {
            shape(UiShape.sm)
            shape(UiShapeSpec.CutCorner(6f.dp))
        }.resolve()

        assertEquals(UiShapeSpec.CutCorner(6f.dp), resolved.shapeSpec)
        assertEquals(UiShape.none, resolved.shape)
    }

    @Test
    fun legacyRadiusClearsCustomShapeSpecWhenAppliedLast() {
        val resolved = Style {
            shape(UiShapeSpec.Circle)
            shape(12f.dp)
        }.resolve()

        assertEquals(12f.dp, resolved.shape)
        assertNull(resolved.shapeSpec)
    }

    @Test
    fun boundsCoversEntireExpandedPath() {
        val path = UiShapeSpec.CutCorner(6f.dp).toPath(UiSlot(10f, 20f, 40f, 30f))

        assertEquals(UiSlot(10f, 20f, 40f, 30f), path.bounds())
    }

    @Test
    fun transformScalesAndTranslatesPathCommands() {
        val path = uiPath {
            moveTo(1f, 2f)
            lineTo(3f, 4f)
            arcTo(0f, 1f, 4f, 5f, 0f, 90f)
        }.transform(scaleX = 2f, scaleY = 3f, translateX = 5f, translateY = 7f)

        assertEquals(
            listOf(
                UiPathCommand.MoveTo(7f, 13f),
                UiPathCommand.LineTo(11f, 19f),
                UiPathCommand.ArcTo(5f, 10f, 13f, 22f, 0f, 90f)
            ),
            path.commands
        )
    }

    @Test
    fun cutCornerFillTessellatesIntoTriangleFan() {
        val mesh = UiShapeSpec.CutCorner(6f.dp).toPath(UiSlot(0f, 0f, 40f, 20f)).tessellateFill()

        assertEquals(8, mesh.points.size)
        assertEquals(18, mesh.indices.size, "8-point convex polygon should tessellate to 6 triangles")
    }

    @Test
    fun roundedRectangleStrokeTessellatesIntoSegmentQuads() {
        val mesh = UiShapeSpec.RoundedRectangle(8f.dp).toPath(UiSlot(0f, 0f, 40f, 20f)).tessellateStroke(UiStroke(2f.dp))

        assertTrue(mesh.points.size >= 16, "rounded corners should flatten into multiple stroke segments")
        assertEquals(0, mesh.indices.size % 6, "stroke geometry should be emitted as quads split into 2 triangles each")
    }

    @Test
    fun cutCornerPathContainsInteriorButNotClippedCorner() {
        val path = UiShapeSpec.CutCorner(6f.dp).toPath(UiSlot(0f, 0f, 40f, 20f))

        assertTrue(path.containsPoint(20f, 10f))
        assertTrue(!path.containsPoint(1f, 1f), "the top-left clipped corner should sit outside the path")
    }

    @Test
    fun triangleMeshClipsAgainstConvexCutCornerPath() {
        val quadMesh = UiTriangleMesh(
            points = listOf(
                UiPoint(0f, 0f),
                UiPoint(40f, 0f),
                UiPoint(40f, 20f),
                UiPoint(0f, 20f)
            ),
            indices = intArrayOf(0, 1, 2, 2, 3, 0)
        )

        val clipped = quadMesh.clipToConvexPath(UiShapeSpec.CutCorner(6f.dp).toPath(
            UiSlot(
                0f,
                0f,
                40f,
                20f
            )
        ))

        assertTrue(clipped.points.size > 4, "clipping should introduce intersection vertices")
        assertTrue(clipped.points.none { it.x == 0f && it.y == 0f }, "the fully clipped corner vertex should be removed from the output mesh")
    }

    @Test
    fun texturedTriangleMeshClipsAgainstConvexCutCornerPathAndPreservesUvInterpolation() {
        val quadMesh = UiTexturedTriangleMesh(
            vertices = listOf(
                UiTexturedVertex(UiPoint(0f, 0f), u = 0f, v = 0f),
                UiTexturedVertex(UiPoint(40f, 0f), u = 1f, v = 0f),
                UiTexturedVertex(UiPoint(40f, 20f), u = 1f, v = 1f),
                UiTexturedVertex(UiPoint(0f, 20f), u = 0f, v = 1f)
            ),
            indices = intArrayOf(0, 1, 2, 2, 3, 0)
        )

        val clipped = quadMesh.clipToConvexPath(UiShapeSpec.CutCorner(6f.dp).toPath(
            UiSlot(
                0f,
                0f,
                40f,
                20f
            )
        ))

        assertTrue(clipped.vertices.size > 4, "clipping should introduce intersection vertices")
        assertTrue(clipped.vertices.none { it.position.x == 0f && it.position.y == 0f }, "the fully clipped corner vertex should be removed from the output mesh")

        val topEdgeVertex = clipped.vertices.firstOrNull { abs(it.position.x - 6f) < 0.001f && abs(it.position.y) < 0.001f }
        assertTrue(topEdgeVertex != null, "expected an interpolated top-edge clip vertex at x=6")
        assertTrue(abs(topEdgeVertex.u - 0.15f) < 0.001f, "u should interpolate along the top edge")
        assertTrue(abs(topEdgeVertex.v) < 0.001f, "v should stay pinned to the top edge")
    }

    @Test
    fun imageVectorFitsInsideTargetSlotAndKeepsAspectRatio() {
        val vector = uiImageVector(
            defaultWidth = 12f.dp,
            defaultHeight = 12f.dp,
            viewportWidth = 12f,
            viewportHeight = 12f
        ) {
            path {
                moveTo(0f, 0f)
                lineTo(12f, 0f)
                lineTo(12f, 12f)
                close()
            }
        }

        val fitted = vector.fitTo(UiSlot(10f, 20f, 24f, 12f)).single().path

        assertEquals(
            listOf(
                UiPathCommand.MoveTo(16f, 20f),
                UiPathCommand.LineTo(28f, 20f),
                UiPathCommand.LineTo(28f, 32f),
                UiPathCommand.Close
            ),
            fitted.commands
        )
    }
}
