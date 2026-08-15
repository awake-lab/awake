// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.uishowcase.ui

import io.github.ronjunevaldoz.awake.testing.ui.renderAnnotatedUiPreviews
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Compares Awake's layout against shadcn's by NUMBER, not by pixel.
 *
 * The pixel oracle next door answers "do these two images look alike", which drags in the
 * rasterizer, the typeface, anti-aliasing and gamma -- three passes of this session went into
 * fixing faults in that instrument rather than in a component. It also answers badly: badge's real
 * defect is "about 4px of extra padding per pill", and pixels reported it as "36.92% mismatch,
 * maxDelta 255", which took a hand-cropped upscale to interpret.
 *
 * Both sides can state their geometry exactly. Awake has semantic bounds; the reference app has
 * getBoundingClientRect, which the capture already called and threw away after printing the size.
 * Now it writes them, keyed by a `data-parity-id` attribute matching the Awake semantic id, and
 * this compares the two directly. No tolerance games, no font dependency.
 *
 * Pixels keep the job they are good at -- colour, corner radius, borders, shadows.
 */
class ShadcnGeometryParityTest {

    @Serializable
    private data class Rect(val x: Double, val y: Double, val width: Double, val height: Double)

    @Serializable
    private data class ReferenceGeometry(
        val case: String,
        val theme: String,
        val root: Rect2,
        val nodes: Map<String, Rect>,
    )

    @Serializable
    private data class Rect2(val width: Double, val height: Double)

    @Test
    fun badgeGeometryMatchesShadcn() = assertBadgeGeometry("light", AwakeBadgeVariantsLightPreview)

    @Test
    fun badgeGeometryMatchesShadcnInDark() = assertBadgeGeometry("dark", AwakeBadgeVariantsDarkPreview)

    /**
     * A theme changes colour, not layout, so both themes must land on the same numbers.
     *
     * shadcn agrees with itself here to the last decimal -- its light and dark captures report
     * identical rects -- so any divergence on the Awake side is ours, and this catches the class of
     * bug where a dark-only token carries different padding or border width by accident.
     */
    @Test
    fun badgeGeometryIsIdenticalAcrossThemes() {
        val light = boundsById(AwakeBadgeVariantsLightPreview)
        val dark = boundsById(AwakeBadgeVariantsDarkPreview)
        assertEquals(light.keys, dark.keys, "the two themes emitted different nodes")
        val drift = light.filter { (id, l) ->
            val d = dark.getValue(id)
            abs(l.width - d.width) > 0.01f || abs(l.height - d.height) > 0.01f || abs(l.x - d.x) > 0.01f
        }
        assertTrue(
            drift.isEmpty(),
            "theme changed layout for ${drift.keys.sorted()}:\n" +
                drift.keys.sorted().joinToString("\n") { id ->
                    "  $id light=${light.getValue(id)} dark=${dark.getValue(id)}"
                },
        )
    }

    private fun boundsById(entry: io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewEntry) =
        renderAnnotatedUiPreviews(entry).single().semantics
            .filter { it.id != null }
            .associate { it.id!! to it.bounds }

    private fun assertBadgeGeometry(
        theme: String,
        entry: io.github.ronjunevaldoz.awake.testing.ui.AwakeUiPreviewEntry,
    ) {
        val reference = Json { ignoreUnknownKeys = true }.decodeFromString<ReferenceGeometry>(
            File("../../docs/reference/shadcn-previews-local/badge-variants_$theme.json").readText(),
        )
        val scene = renderAnnotatedUiPreviews(entry).single()
        val awake = scene.semantics.filter { it.id != null }.associateBy { it.id!! }

        val rows = reference.nodes.toSortedMap().map { (id, ref) ->
            val bounds = requireNotNull(awake[id]?.bounds) {
                "Awake emitted no semantic node '$id'. Present: ${awake.keys.sorted()}"
            }
            GeometryRow(
                id = id,
                awakeWidth = bounds.width.toDouble(),
                referenceWidth = ref.width,
                awakeX = bounds.x.toDouble(),
                referenceX = ref.x,
                awakeHeight = bounds.height.toDouble(),
                referenceHeight = ref.height,
            )
        }

        val table = rows.joinToString("\n") { r ->
            "  %-18s width %7.2f vs %7.2f (%+6.2f)   x %7.2f vs %7.2f (%+6.2f)   height %6.2f vs %6.2f".format(
                r.id, r.awakeWidth, r.referenceWidth, r.widthDelta,
                r.awakeX, r.referenceX, r.xDelta,
                r.awakeHeight, r.referenceHeight,
            )
        }
        println("badge geometry vs shadcn [$theme]:\n$table")

        val offenders = rows.filter { abs(it.widthDelta) > KNOWN_WIDTH_GAP_PX || abs(it.heightDelta) > TOLERANCE_PX }
        assertTrue(
            offenders.isEmpty(),
            "badge geometry diverges from shadcn beyond the recorded gap:\n$table\n\n" +
                "Every pill is wider than shadcn's by roughly the same amount, which is horizontal " +
                "content padding, not text measurement -- heights already agree exactly. " +
                "KNOWN_WIDTH_GAP_PX holds that gap so this test states it instead of hiding it; " +
                "shrink it as the padding is fixed, and it must never be raised.",
        )
    }

    private data class GeometryRow(
        val id: String,
        val awakeWidth: Double,
        val referenceWidth: Double,
        val awakeX: Double,
        val referenceX: Double,
        val awakeHeight: Double,
        val referenceHeight: Double,
    ) {
        val widthDelta: Double get() = awakeWidth - referenceWidth
        val xDelta: Double get() = awakeX - referenceX
        val heightDelta: Double get() = awakeHeight - referenceHeight
    }

    private companion object {
        /** Sub-pixel: both sides report fractional widths, so exact equality is not the bar. */
        const val TOLERANCE_PX = 1.0

        /**
         * What is left after the padding fix, measured rather than guessed.
         *
         * Was ~4.8px per pill when contentPadding used 2.5 grid units (10dp) against shadcn's
         * `px-2` (8px). At 2.0 units the widths land within 0.66..1.16px, and that remainder is
         * text advance, not padding: Awake reports integer widths while the DOM reports fractional
         * ones (56.84, 75.31, 79.34, 56.20), so a sub-pixel gap is expected and closing it would
         * need sub-pixel layout rather than a style change.
         *
         * Heights and gaps already agree exactly, which is what made the padding the only suspect.
         */
        const val KNOWN_WIDTH_GAP_PX = 1.5
    }
}
