// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.testing.ui.rasterize
import io.github.ronjunevaldoz.awake.ui.api.sp
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.font.UiFonts
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.text
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.testSnapshot
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Compares our text rendering against the same TTF rendered by Chromium
 * (`tools/capture_font_reference.py`, references in `docs/reference/font-previews/`).
 *
 * Exists because atlas metrics alone could not answer whether a glyph sitting a pixel low was
 * faithful to the typeface or introduced by us. The control says introduced by us: real Roboto
 * puts every flat and round glyph on one row at 12, 14 and 16px, while our atlas splits them by
 * a pixel at 12 and 14. The drift is recorded below rather than asserted away, so it cannot
 * widen unnoticed and disappears from the map when the atlas is fixed.
 */
@io.github.ronjunevaldoz.awake.testing.ui.UiLowLevelTest("Measures rendered font baseline pixels directly")
class FontBaselineFidelityTest {

    private data class Sample(val id: String, val text: String, val sizePx: Float)

    private val samples = listOf(
        Sample("roundvsflat-12", "iliaeco", 12f),
        Sample("roundvsflat-14", "iliaeco", 14f),
        Sample("roundvsflat-16", "iliaeco", 16f),
        Sample("email-12", "Email", 12f),
    )

    /** Baseline spread we render, against a reference spread of 0 everywhere. Every entry is a
     * defect; the goal is an empty map, and entries may only shrink.
     *
     * History: the map once held {roundvsflat-14: 1, roundvsflat-16: 1, email-12: 1}, but those
     * were not real -- `ourSpread` called `rasterize()` without `font =`, measuring the
     * null-font placeholder rect instead of glyph sampling. Measured honestly (font passed,
     * coverage from alpha over a transparent background, half-coverage ink threshold) after
     * glyph quads were fixed to cover their full UV sample rect, all four samples split by
     * exactly one pixel: the round glyphs' designed baseline overshoot (~0.01em, sub-pixel)
     * can quantize their ink bottom one row below the flat glyphs'. Chromium's hinter snaps
     * that overshoot away; we render unhinted. */
    // Empty since Roboto 3.015: every sample now lands every glyph on one baseline, so the
    // allowance the older 2011 atlas needed is gone. The test only permits shrinking this map,
    // and this is it shrinking to nothing -- if a future font change needs an entry back, that is
    // a regression to explain rather than a number to record.
    private val knownBaselineDrift = emptyMap<String, Int>()

    private fun inkBottomsPerGlyph(luma: (Int, Int) -> Int, width: Int, height: Int): List<Int> {
        val lit = (0 until width).map { x -> (0 until height).any { y -> luma(x, y) > 40 } }
        val runs = mutableListOf<Pair<Int, Int>>()
        var start: Int? = null
        lit.forEachIndexed { x, on ->
            if (on && start == null) start = x
            if (!on && start != null) {
                runs += start to x
                start = null
            }
        }
        start?.let { runs += it to width }
        return runs.map { (a, b) ->
            (0 until height).last { y -> (a until b).any { x -> luma(x, y) > 40 } }
        }
    }

    private fun ourSpread(sample: Sample): Int {
        val w = 640
        val h = 96
        val ui = UiContext()
        ui.beginFrame(w.toFloat(), h.toFloat(), testSnapshot())
        ui.createAbsolute(x = 16f, y = 48f)
            .text(sample.text, style = Style { textSize(sample.sizePx.sp) })
        // The font MUST be passed: without it the rasterizer draws its null-font placeholder
        // rect for every glyph, and this gate silently measures placeholder geometry instead of
        // glyph sampling (see docs/tasks/2026-08-10-glyph-scale-regression.md). Coverage lives
        // in the alpha channel -- drawGlyph writes full RGB into any pixel with nonzero
        // coverage, so thresholding red would count the entire AA skirt. The background must be
        // TRANSPARENT: an opaque background fills alpha with 255, every pixel then passes the
        // ink threshold, the whole canvas collapses into one full-width run and the spread is 0
        // no matter what was drawn -- the gate passes vacuously.
        val pixels = ui.endFrame().rasterize(
            w,
            h,
            background = Color(0f, 0f, 0f, 0f),
            font = UiFonts.default(),
        )
        val bottoms = inkBottomsPerGlyph({ x, y -> pixels[(y * w + x) * 4 + 3].toInt() and 0xFF }, w, h)
        assertTrue(
            bottoms.size > 1,
            "${sample.id}: probe found ${bottoms.size} ink run(s) for ${sample.text.length} glyphs; " +
                "a single run means the measurement degenerated (all-lit canvas or merged glyphs) " +
                "and the spread below would be meaningless",
        )
        return bottoms.max() - bottoms.min()
    }

    private fun referenceSpread(sample: Sample): Int {
        val file = listOf(
            File("../../../docs/reference/font-previews/${sample.id}.png"),
            File("../../../../docs/reference/font-previews/${sample.id}.png"),
        ).firstOrNull { it.exists() }
            ?: File("../../../docs/reference/font-previews/${sample.id}.png")
        assertTrue(
            file.exists(),
            "missing reference ${file.path}; run tools/capture_font_reference.py",
        )
        val image = ImageIO.read(file)
        val bottoms = inkBottomsPerGlyph(
            { x, y -> (image.getRGB(x, y) shr 16) and 0xFF },
            image.width,
            image.height,
        )
        return bottoms.max() - bottoms.min()
    }

    @Test
    fun baselineSpreadMatchesRealRobotoOrIsRecordedAsDrift() {
        samples.forEach { sample ->
            val reference = referenceSpread(sample)
            val ours = ourSpread(sample)
            val allowed = reference + (knownBaselineDrift[sample.id] ?: 0)
            assertEquals(
                allowed,
                ours,
                "${sample.id}: real Roboto puts every glyph within $reference px of one baseline, " +
                    "we render a spread of $ours. Update knownBaselineDrift only to shrink it.",
            )
        }
    }

    @Test
    fun realRobotoItselfHasNoBaselineSpread() {
        // Guards the control: if a capture regressed, the comparison above would silently
        // start grading against a moving reference.
        samples.forEach { sample ->
            assertEquals(0, referenceSpread(sample), "reference ${sample.id} is not flush")
        }
    }
}
