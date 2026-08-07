// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.colors.Color
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Diffs [ShadcnTheme]'s resolved colors against the *real* shadcn reference -- not our
 * own memory of what shadcn looks like, and not a hand-written "vibes" comparison (see the
 * old showcase "Reference Comparison" page, which was exactly that: a bullet list of
 * impressions next to our own render, never actually checked against anything).
 *
 * The numbers below are copied verbatim from `ronjunevaldoz/shadcn-compose`'s own
 * `ShadcnColors.kt` (fetched 2026-07-18, `main` branch) -- a real, independently maintained
 * Compose implementation of shadcn/ui by the same author, whose color values carry their own
 * doc-comment citations back to `ui.shadcn.com/docs/theming`'s published OKLCH CSS variables
 * (e.g. "primary/onSecondary match real shadcn's oklch(0.205 0 0)", "Verified against oklch
 * (0.577 0.245 27.325)"). That's the actual ground truth this project has always claimed to
 * target but never mechanically checked against -- this test is what turns "we think this
 * matches shadcn" into "a test proves it matches shadcn, or fails with the exact numbers that
 * don't."
 *
 * Only tokens shadcn-compose's own comments explicitly verified against the real CSS are
 * checked here (primary, destructive, ring) -- extending this file with more tokens as they
 * get similarly verified upstream is exactly the intended use.
 */
class ShadcnReferenceTokenTest {

    // Same tolerance class as this project's existing pixel-baseline tests: float rounding
    // through OKLCH->linear-sRGB->gamma conversion accumulates a little error even for two
    // mathematically identical OKLCH triples, so an exact `==` would be too brittle.
    private val colorTolerance = 0.02f

    @Test
    fun defaultDarkPrimaryMatchesRealShadcnOklch() {
        // shadcn-compose ShadcnColors.kt, ShadcnDarkColors.primary = TwColors.neutral200,
        // doc comment: "verified against ui.shadcn.com/docs/theming's dark oklch(0.922 0 0)".
        val reference = oklch(0.922f, 0f, 0f)
        val actual = shadcnTheme(dark = true).colors.primary
        assertColorClose("dark primary", reference, actual)
    }

    @Test
    fun defaultLightPrimaryMatchesRealShadcnOklch() {
        // shadcn-compose ShadcnColors.kt, ShadcnLightColors.primary = TwColors.neutral900,
        // doc comment: "verified byte-for-byte ... match real shadcn's oklch(0.205 0 0)".
        val reference = oklch(0.205f, 0f, 0f)
        val actual = shadcnTheme(dark = false).colors.primary
        assertColorClose("light primary", reference, actual)
    }

    @Test
    fun defaultDarkDestructiveMatchesRealShadcnOklch() {
        // shadcn-compose ShadcnColors.kt, ShadcnDarkColors.destructive = TwColors.red400,
        // doc comment: "Verified against dark oklch(0.704 0.191 22.216)".
        val reference = oklch(0.704f, 0.191f, 22.216f)
        val actual = shadcnTheme(dark = true).colors.destructive
        assertColorClose("dark destructive", reference, actual)
    }

    @Test
    fun defaultLightDestructiveMatchesRealShadcnOklch() {
        // shadcn-compose ShadcnColors.kt, ShadcnLightColors.destructive = TwColors.red600,
        // doc comment: "Verified against oklch(0.577 0.245 27.325)".
        val reference = oklch(0.577f, 0.245f, 27.325f)
        val actual = shadcnTheme(dark = false).colors.destructive
        assertColorClose("light destructive", reference, actual)
    }

    @Test
    fun focusRingStaysAMidGrayLikeRealShadcnRingToken() {
        // shadcn-compose ShadcnColors.kt: "shadcn's 'ring' token is a distinct mid-gray
        // (oklch(0.705 0.015 286.067) = zinc400), not primary -- it's a focus indicator
        // color, not a brand color" (light); dark uses zinc500 = oklch(0.552 0.016 285.938).
        // Wider tolerance than the primary/destructive checks above: Awake's "Neutral" base
        // color family deliberately zeroes chroma (see ShadcnBaseColor.Neutral), so
        // this can never be an exact RGB match -- the point of this test is that the token
        // stays a genuine mid-gray (not primary's near-black/near-white), which a wide but
        // bounded tolerance still catches a regression on.
        val darkReference = oklch(0.552f, 0.016f, 285.938f)
        val lightReference = oklch(0.705f, 0.015f, 286.067f)
        assertColorClose("dark ring", darkReference, shadcnTheme(dark = true).asShadcnTheme().ring, tolerance = 0.08f)
        assertColorClose("light ring", lightReference, shadcnTheme(dark = false).asShadcnTheme().ring, tolerance = 0.08f)
    }

    private fun assertColorClose(label: String, reference: Color, actual: Color, tolerance: Float = colorTolerance) {
        val diff = abs(reference.r - actual.r) + abs(reference.g - actual.g) + abs(reference.b - actual.b)
        assertTrue(
            diff < tolerance,
            "$label drifted from the real shadcn reference: reference=$reference actual=$actual diff=$diff",
        )
    }
}
