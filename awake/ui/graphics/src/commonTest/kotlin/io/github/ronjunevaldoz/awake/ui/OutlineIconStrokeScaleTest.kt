// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * An outline glyph must keep the same stroke weight relative to its own size at every display
 * scale.
 *
 * It did not. [UiImageVector.fitTo] scales a glyph's stroke width into pixel space along with its
 * path, and [strokeToFillPath] then converted that value with `toPx()` a second time -- so every
 * outline icon came out proportionally heavier the higher the scale: roughly double at 2x, and by
 * 3x the shapes closed into solid blobs. Reported as the icons not looking like Heroicons.
 *
 * Measured as outline area over the glyph's own drawn area, which is scale-independent by
 * construction: a correctly scaled stroke covers the same fraction of itself at any size.
 */
class OutlineIconStrokeScaleTest {

    @AfterTest
    fun resetDensity() {
        UiDensity.scale = 1f
    }

    @Test
    fun strokeWeightStaysProportionalAcrossDisplayScales() {
        val coverage = listOf(1f, 2f, 3f).map { scale ->
            UiDensity.scale = scale
            val slotSize = ICON_SIZE.value * scale
            val slot = UiBounds(0f, 0f, slotSize, slotSize)
            val fitted = squareGlyph().fitTo(slot).single()
            val stroke = requireNotNull(fitted.stroke)
            val outlineArea = fitted.path.strokeToFillPath(stroke).tessellateFill().area()
            outlineArea / (slotSize * slotSize)
        }

        coverage.forEach { assertTrue(it > 0.01f, "the glyph must render, coverage was $it") }
        val spread = coverage.max() - coverage.min()
        assertTrue(
            spread < 0.05f,
            "stroke weight must not grow with display scale: coverage per scale was $coverage",
        )
    }

    /** A closed square traced in a 24-unit viewport with Heroicons' own 1.5 stroke width. */
    private fun squareGlyph(): UiImageVector = uiImageVector(
        defaultWidth = ICON_SIZE,
        defaultHeight = ICON_SIZE,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ) {
        path(stroke = UiStroke(width = 1.5f.dp, cap = UiStrokeCap.Round, join = UiStrokeJoin.Round)) {
            moveTo(4f, 4f)
            lineTo(20f, 4f)
            lineTo(20f, 20f)
            lineTo(4f, 20f)
            close()
        }
    }

    private fun UiTriangleMesh.area(): Float {
        var total = 0f
        var index = 0
        while (index + 2 < indices.size) {
            val a = points[indices[index]]
            val b = points[indices[index + 1]]
            val c = points[indices[index + 2]]
            total += kotlin.math.abs((b.x - a.x) * (c.y - a.y) - (c.x - a.x) * (b.y - a.y)) / 2f
            index += 3
        }
        return total
    }

    private companion object {
        val ICON_SIZE: Dp = 24f.dp
    }
}
