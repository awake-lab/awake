// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnRadiusScale
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Locks [ShadcnRadiusScale.fromBase] to Tailwind v4 / new-york-v4's real, MULTIPLICATIVE
 * derivation, copied from the pinned reference app's own `index.css`:
 *
 * ```css
 * --radius-sm: calc(var(--radius) * 0.6);
 * --radius-md: calc(var(--radius) * 0.8);
 * --radius-lg: var(--radius);
 * --radius-xl: calc(var(--radius) * 1.4);
 * ```
 *
 * This exists because the previous implementation was ADDITIVE (`base-4`/`base-2`/`base+4`)
 * and carried a comment asserting shadcn's scale "is additive, not multiplicative" -- factually
 * contradicted by the CSS above. It went unnoticed because additive and multiplicative agree
 * EXACTLY at Vega's base of 10dp (6/8/10/14), and Vega is the preset every parity comparison
 * and screenshot baseline uses. Every other preset silently drifted (Nova's 5dp base produced
 * 1/3/9 instead of the real 3/4/7).
 *
 * The Vega case below is therefore the regression's blind spot, and the non-Vega cases are what
 * actually catch a revert to additive -- keep both.
 */
class ShadcnRadiusScaleTest {

    private fun assertDp(expected: Float, actual: io.github.ronjunevaldoz.awake.ui.api.Dp, label: String) {
        assertTrue(
            abs(expected - actual.value) < 0.001f,
            "$label: expected ${expected}dp, got ${actual.value}dp",
        )
    }

    @Test
    fun vegaBaseMatchesRealShadcnRadiusLadder() {
        // --radius: 0.625rem = 10px, the value the pinned reference app actually renders with.
        val scale = ShadcnRadiusScale.fromBase(10f.dp)
        assertDp(6f, scale.sm, "sm (radius * 0.6)")
        assertDp(8f, scale.md, "md (radius * 0.8)")
        assertDp(10f, scale.lg, "lg (radius)")
        assertDp(14f, scale.xl, "xl (radius * 1.4)")
    }

    @Test
    fun ladderStaysMultiplicativeAtOtherBases() {
        // The case additive got wrong: at base 5, additive gives 1/3/9, multiplicative 3/4/7.
        val scale = ShadcnRadiusScale.fromBase(5f.dp)
        assertDp(3f, scale.sm, "sm (radius * 0.6)")
        assertDp(4f, scale.md, "md (radius * 0.8)")
        assertDp(5f, scale.lg, "lg (radius)")
        assertDp(7f, scale.xl, "xl (radius * 1.4)")
    }

    @Test
    fun zeroBaseCollapsesEntireLadderToZero() {
        // A 0dp-base preset (Lyra) must not produce negative radii -- and unlike the additive
        // formula, which needed an explicit coerceAtLeast(0f) to avoid base-4 = -4, the
        // multiplicative one reaches 0 naturally. Asserted so a future "simplify" that drops
        // the clamp is still provably safe.
        val scale = ShadcnRadiusScale.fromBase(0f.dp)
        assertDp(0f, scale.xs, "xs")
        assertDp(0f, scale.sm, "sm")
        assertDp(0f, scale.md, "md")
        assertDp(0f, scale.lg, "lg")
        assertDp(0f, scale.xl, "xl")
    }
}
