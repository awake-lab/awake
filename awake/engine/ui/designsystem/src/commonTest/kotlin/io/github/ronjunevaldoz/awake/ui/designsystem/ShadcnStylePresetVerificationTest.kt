// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Verifies [ShadcnStylePreset] on the terms it actually has -- these 8 density/radius bundles
 * are Awake-original, with NO upstream shadcn counterpart (see `ShadcnStylePreset`'s own doc
 * comment), so there is no real reference to diff most of them against. What CAN be checked
 * mechanically without inventing a fake reference:
 *
 * - the radius scale's additive rule (`ShadcnRadiusScale.fromBase`) holds for every preset, not
 *   just the one ([ShadcnReferenceTokenExpandedTest] already spot-checked with real Vega numbers.
 * - each preset's own metrics are internally sane (positive, sensibly ordered relative to each
 *   other) and no two presets silently collapse into the same bundle.
 * - every (preset x base color x mode) combination resolves a complete theme without throwing --
 *   a cheap way to catch a whole class of "this preset's config crashes with base color X"
 *   regressions.
 * - [ShadcnStylePreset.Vega] is the one preset with a real correctness obligation: its
 *   `baseRadius` must equal shadcn's own `--radius` (0.625rem = 10dp), pinned directly against
 *   [ShadcnReferenceTokens] rather than assumed.
 *
 * Suspicious-but-not-fixed finding from this audit (reported per this pipeline's non-goals, not
 * corrected here): [ShadcnStylePreset.Maia] and [ShadcnStylePreset.Luma] have byte-identical
 * [ShadcnMetrics] (18dp/22dp/14dp/9dp/14dp/6dp/4.5dp) -- they only differ in `baseRadius` (10dp
 * vs 12dp) and `ringAlphaMultiplier` (1f vs 0.75f). That may be an intentional "same density,
 * different roundness/ring" pairing, or it may be an unintentional copy-paste -- worth a design
 * call from whoever owns preset values, not assumed here either way.
 */
class ShadcnStylePresetVerificationTest {

    /** This test previously asserted an ADDITIVE ladder (base-6/-4/-2/+4) and therefore actively
     * protected the bug it was meant to catch: real Tailwind v4 derives radii MULTIPLICATIVELY
     * (`calc(var(--radius) * 0.6/0.8/1.4)`, see the pinned reference app's own index.css).
     * The two agree exactly at Vega's base of 10dp, which is the only preset the parity
     * screenshots exercise -- so every other preset drifted silently. Corrected 2026-08-10;
     * see ShadcnRadiusScaleTest for the formula-level lock. */
    @Test
    fun radiusScaleFollowsTheMultiplicativeRuleForEveryPreset() {
        for (preset in ShadcnStylePreset.values()) {
            val base = preset.baseRadius.value
            val shapes = shadcnTheme(preset = preset).shapes
            val expectedXs = (base * 0.4f).coerceAtLeast(0f)
            val expectedSm = (base * 0.6f).coerceAtLeast(0f)
            val expectedMd = (base * 0.8f).coerceAtLeast(0f)
            val expectedXl = (base * 1.4f).coerceAtLeast(0f)

            assertEquals(expectedXs, shapes.xs.value, "${preset.label}: xs should be base * 0.4")
            assertEquals(expectedSm, shapes.sm.value, "${preset.label}: sm should be base * 0.6 (--radius-sm)")
            assertEquals(expectedMd, shapes.md.value, "${preset.label}: md should be base * 0.8 (--radius-md)")
            assertEquals(base, shapes.lg.value, "${preset.label}: lg should equal baseRadius exactly (--radius-lg)")
            assertEquals(expectedXl, shapes.xl.value, "${preset.label}: xl should be base * 1.4 (--radius-xl)")
        }
    }

    @Test
    fun everyPresetsMetricsArePositiveAndSensiblyOrdered() {
        for (preset in ShadcnStylePreset.values()) {
            val m = preset.metrics
            val label = preset.label
            for ((name, dp) in listOf(
                "panelPadding" to m.panelPadding,
                "surfacePadding" to m.surfacePadding,
                "fieldPaddingX" to m.fieldPaddingX,
                "fieldPaddingY" to m.fieldPaddingY,
                "badgePaddingX" to m.badgePaddingX,
                "badgePaddingY" to m.badgePaddingY,
                "inputPaddingY" to m.inputPaddingY,
            )) {
                assertTrue(dp.value > 0f, "$label: $name should be positive, was ${dp.value}")
            }
            assertTrue(
                m.fieldPaddingX.value > m.fieldPaddingY.value,
                "$label: fieldPaddingX (${m.fieldPaddingX.value}) should exceed fieldPaddingY (${m.fieldPaddingY.value})",
            )
            assertTrue(
                m.badgePaddingX.value > m.badgePaddingY.value,
                "$label: badgePaddingX (${m.badgePaddingX.value}) should exceed badgePaddingY (${m.badgePaddingY.value})",
            )
            assertTrue(
                m.fieldPaddingY.value >= m.inputPaddingY.value,
                "$label: fieldPaddingY (${m.fieldPaddingY.value}) should be >= inputPaddingY (${m.inputPaddingY.value})",
            )
        }
    }

    @Test
    fun noTwoPresetsShareAFullIdentity() {
        // Full identity = baseRadius + metrics + ringAlphaMultiplier all equal. Metrics alone
        // colliding (Maia/Luma, see class doc) is not an identity collision by itself.
        val identities = ShadcnStylePreset.values().map { preset ->
            Triple(preset.baseRadius, preset.metrics, preset.ringAlphaMultiplier)
        }
        assertEquals(
            identities.size,
            identities.toSet().size,
            "two ShadcnStylePreset entries are fully identical (baseRadius + metrics + ringAlphaMultiplier) " +
                "-- one of them is very likely a copy-paste that never got its own values",
        )
    }

    @Test
    fun everyPresetResolvesACompleteThemeAcrossEveryBaseColorAndMode() {
        var combinations = 0
        for (preset in ShadcnStylePreset.values()) {
            for (baseColor in ShadcnBaseColor.values()) {
                for (dark in listOf(false, true)) {
                    combinations++
                    val theme = shadcnTheme(preset = preset, baseColor = baseColor, dark = dark)
                    // Resolution itself not throwing is most of the value here; a few cheap
                    // well-formedness checks on top catch a corrupted-config class of regression.
                    assertEquals(preset.baseRadius.value, theme.shapes.lg.value, "${preset.label}/${baseColor.label}/dark=$dark: lg radius mismatch")
                    for (channel in listOf(theme.colors.background.a, theme.colors.foreground.a, theme.colors.primary.a)) {
                        assertTrue(channel in 0f..1f, "${preset.label}/${baseColor.label}/dark=$dark: alpha channel out of range: $channel")
                    }
                }
            }
        }
        assertEquals(
            ShadcnStylePreset.values().size * ShadcnBaseColor.values().size * 2,
            combinations,
            "expected preset x baseColor x mode combination count to match 8 x 7 x 2",
        )
    }

    @Test
    fun vegaBaseRadiusPinsToTheRealShadcnDefaultRadius() {
        // The one preset with a correctness obligation to the pinned reference -- every other
        // preset is Awake-original and has none (see class doc).
        val oursDp = ShadcnStylePreset.Vega.baseRadius.value
        val referenceDp = ShadcnReferenceTokens.RADIUS_REM * 16f
        assertTrue(
            abs(oursDp - referenceDp) < 0.01f,
            "Vega.baseRadius drifted from real shadcn --radius: ours=$oursDp reference=$referenceDp",
        )
    }
}
