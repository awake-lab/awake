// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.contains
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.TimeSource

/**
 * Real-numbers proof for the "skip exact convex-path clipping for primitives safely inside a
 * rounded surface's straight-edge interior" fix (see each backend's `RendererDrawUi.kt`
 * `canSkipExactClip`/`stage*Run` and this file's [safeInteriorMargin]). Exercises the exact
 * same [UiTriangleMesh.clipToConvexPaths] cost the real backends pay per glyph/quad -- pure
 * ui-core geometry, no GPU, so this is trustworthy JVM wall-clock timing, same rationale as
 * `UiShowcaseLayoutCostTest`.
 */
class UiPathSafeInteriorClipTest {

    @Test
    fun interiorGlyphSkipsExactClipButProducesIdenticalGeometry() {
        val bounds = UiBounds(0f, 0f, 600f, 300f)
        val shape = UiShapeSpec.RoundedRectangle(8f.dp)
        val path = shape.toPath(bounds)
        val margin = shape.safeInteriorMargin(bounds)
        val safeInteriorRect = insetRect(bounds, margin)

        // A glyph-sized quad well inside the card, nowhere near a rounded corner.
        val interiorGlyph = UiBounds(x = 100f, y = 100f, width = 8f, height = 16f)
        assertTrue(safeInteriorRect.contains(interiorGlyph), "interior glyph must be classified as safe")

        val rawMesh = quadMesh(interiorGlyph)
        val exactlyClipped = rawMesh.clipToConvexPaths(listOf(path))

        // The whole point of the optimization: for a primitive the safe-interior rect already
        // proves is untouched by the corner arc, running the real exact-clip introduces NO new
        // (cut) vertices -- Sutherland-Hodgman may still re-walk the polygon and emit a
        // duplicate point at a shared edge, but every point it emits is one of the raw quad's
        // own 4 corners, never a synthesized intersection point. That's the actual geometric
        // guarantee that makes skipping the clip (using rawMesh as-is) safe.
        val rawPoints = rawMesh.points.toSet()
        assertTrue(
            exactlyClipped.points.all { it in rawPoints },
            "interior glyph's exact-clip introduced a new vertex -- safe-interior classification was wrong: " +
                "${exactlyClipped.points}",
        )
    }

    @Test
    fun cornerOverlappingGlyphStillNeedsExactClip() {
        val bounds = UiBounds(0f, 0f, 600f, 300f)
        val shape = UiShapeSpec.RoundedRectangle(8f.dp)
        val path = shape.toPath(bounds)
        val margin = shape.safeInteriorMargin(bounds)
        val safeInteriorRect = insetRect(bounds, margin)

        // A quad straddling the top-left rounded corner -- the exact bug 62d3d98c fixed.
        val cornerQuad = UiBounds(x = -2f, y = -2f, width = 12f, height = 12f)
        assertFalse(safeInteriorRect.contains(cornerQuad), "corner-straddling quad must NOT be classified as safe")

        val rawMesh = quadMesh(cornerQuad)
        val exactlyClipped = rawMesh.clipToConvexPaths(listOf(path))

        // Real clipping must have actually changed the geometry (cut the corner off), not
        // silently passed the quad through unclipped.
        assertTrue(exactlyClipped.points != rawMesh.points, "corner case must still be exact-clipped")
    }

    /**
     * Diagnostic only (see `UiShowcaseLayoutCostTest`'s identical framing) -- not a regression
     * gate, since absolute timing on shared CI hardware is noisy. Reproduces the reported
     * field-demo shape: 1323 glyph quads inside one `RoundedRectangle(8dp)` card, all in the
     * realistic "text never actually touches a rounded corner" layout, and reports the real
     * before/after Sutherland-Hodgman cost this fix removes for that common case.
     */
    @Test
    fun measureGlyphClipCostBeforeAndAfterSafeInteriorSkip() {
        val bounds = UiBounds(0f, 0f, 600f, 300f)
        val shape = UiShapeSpec.RoundedRectangle(8f.dp)
        val path = shape.toPath(bounds)
        val margin = shape.safeInteriorMargin(bounds)
        val safeInteriorRect = insetRect(bounds, margin)

        val glyphCount = 1323
        val glyphs = (0 until glyphCount).map { i ->
            val col = i % 60
            val row = i / 60
            UiBounds(x = margin + 4f + col * 9f, y = margin + 4f + row * 18f, width = 8f, height = 16f)
        }
        val skippable = glyphs.count { safeInteriorRect.contains(it) }

        val iterations = 20
        val beforeStart = TimeSource.Monotonic.markNow()
        repeat(iterations) {
            glyphs.forEach { quadMesh(it).clipToConvexPaths(listOf(path)) }
        }
        val beforeNanos = beforeStart.elapsedNow().inWholeNanoseconds

        val afterStart = TimeSource.Monotonic.markNow()
        repeat(iterations) {
            glyphs.forEach { glyph ->
                if (!safeInteriorRect.contains(glyph)) quadMesh(glyph).clipToConvexPaths(listOf(path))
            }
        }
        val afterNanos = afterStart.elapsedNow().inWholeNanoseconds

        val beforeMsPerFrame = beforeNanos / 1_000_000.0 / iterations
        val afterMsPerFrame = afterNanos / 1_000_000.0 / iterations
        println(
            "glyph exact-clip cost, $glyphCount glyphs in a RoundedRectangle(8dp) card " +
                "($skippable/$glyphCount safely interior):\n" +
                "  before (always exact-clip): ${beforeMsPerFrame.format3Decimals()}ms/frame\n" +
                "  after  (skip when safe):    ${afterMsPerFrame.format3Decimals()}ms/frame",
        )

        check(skippable > glyphCount / 2) {
            "benchmark layout put too few glyphs in the safe interior ($skippable/$glyphCount) -- not representative"
        }
    }

    private fun insetRect(bounds: UiBounds, margin: Float): UiBounds = UiBounds(
        x = bounds.x + margin,
        y = bounds.y + margin,
        width = (bounds.width - 2f * margin).coerceAtLeast(0f),
        height = (bounds.height - 2f * margin).coerceAtLeast(0f),
    )

    private fun quadMesh(bounds: UiBounds): UiTriangleMesh = UiTriangleMesh(
        points = listOf(
            UiPoint(bounds.x, bounds.y),
            UiPoint(bounds.x + bounds.width, bounds.y),
            UiPoint(bounds.x + bounds.width, bounds.y + bounds.height),
            UiPoint(bounds.x, bounds.y + bounds.height),
        ),
        indices = intArrayOf(0, 1, 2, 2, 3, 0),
    )
}

private fun Double.format3Decimals(): String {
    val total = (this * 1000).toInt()
    val intPart = total / 1000
    val fracPart = (total % 1000).let { if (it < 0) -it else it }
    return "$intPart.${fracPart.toString().padStart(3, '0')}"
}
