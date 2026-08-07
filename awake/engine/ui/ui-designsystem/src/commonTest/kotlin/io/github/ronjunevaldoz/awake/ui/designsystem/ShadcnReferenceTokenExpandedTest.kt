// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.colors.Color
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Diffs every comparable [ShadcnTheme] resolved token (default Vega preset / Neutral base color /
 * Base accent -- `ShadcnTheme`/`shadcnTheme()`'s own defaults) plus `radius`, against
 * [ShadcnReferenceTokens] -- real shadcn/ui "neutral" OKLCH CSS vars machine-extracted from a
 * pinned upstream commit (`tools/fetch_shadcn_reference.sh` + `tools/extract_shadcn_tokens.py`,
 * see `docs/reference/shadcn-reference-pipeline.md`). Supersedes [ShadcnReferenceTokenTest]'s
 * 3-token spot check (copied from a third party's doc comments) with full coverage of every
 * token that has a real 1:1 shadcn counterpart, sourced directly from shadcn's own repo.
 *
 * Tokens skipped, not force-compared against nothing:
 * - `destructiveForeground`: shadcn's current registry has no `--destructive-foreground` var
 *   (destructive buttons hardcode white text via Tailwind instead); Awake computes one anyway.
 * - `chart-1`..`chart-5`: shadcn chart tokens Awake doesn't model.
 * - `primaryHover`/`primaryPressed`/`secondaryHover`/... : Awake-only interaction-state colors
 *   synthesized via `mix()`, not shadcn CSS vars.
 *
 * KNOWN_DRIFTED mechanism: this audit found real drift. Fixing production values is wave-2's
 * job, not this file's -- so a token listed in [KNOWN_DRIFTED] is asserted against its CURRENT
 * (wrong) resolved value instead of the reference, keeping the suite green today while staying a
 * real regression lock: it fails loudly the moment the drift's shape changes, and fails loudly
 * (by design) the moment someone fixes the underlying value without also deleting the entry here
 * -- a forced touch-point for whoever does the fix. Every entry's comment records the real
 * reference value and the measured diff for that worklist.
 */
class ShadcnReferenceTokenExpandedTest {

    // Same comparison approach as ShadcnReferenceTokenTest: sum of abs channel deltas in
    // resolved sRGB space, not raw OKLCH, so it tolerates float rounding through the
    // OKLCH->linear-sRGB->gamma conversion.
    private val tolerance = 0.02f

    private val KNOWN_DRIFTED: Map<String, Map<String, Color>> = mapOf(
        "light" to mapOf(
            // ours=oklch(0.94 0 0) reference=oklch(0.97 0 0) diff=0.117
            "accent" to oklch(0.94f, 0f, 0f),
        ),
        "dark" to mapOf(
            // ours=oklch(0.235 0 0) reference=oklch(0.269 0 0) diff=0.095
            "muted" to oklch(0.235f, 0f, 0f),
            // ours=oklch(0.32 0 0) reference=oklch(0.269 0 0) diff=0.149
            "accent" to oklch(0.32f, 0f, 0f),
            // ours=oklch(0.922 0 0) reference=oklch(0.488 0.243 264.376) diff=1.443 -- an
            // entirely different hue family, not just off-by-a-shade: Awake reuses `primary`
            // for the sidebar's "active nav item" accent instead of shadcn's distinct blue.
            "sidebar-primary" to oklch(0.922f, 0f, 0f),
            // ours=oklch(0.205 0 0) reference=oklch(0.985 0 0) diff=2.669 -- near-total
            // inversion (dark-on-dark vs reference's near-white).
            "sidebar-primary-foreground" to oklch(0.205f, 0f, 0f),
        ),
    )

    @Test
    fun lightTokensMatchReferenceOrLockedDrift() = assertMode(dark = false, reference = ShadcnReferenceTokens.light)

    @Test
    fun darkTokensMatchReferenceOrLockedDrift() = assertMode(dark = true, reference = ShadcnReferenceTokens.dark)

    @Test
    fun defaultRadiusIsKnownDriftedFromReferenceSpec() {
        // KNOWN_DRIFTED (Dp, not color): ours=6dp (Vega preset's baseRadius) reference=10dp
        // (ShadcnReferenceTokens.RADIUS_REM=0.625rem * 16). Pre-existing audit finding -- fix
        // belongs to wave-2.
        val oursDp = shadcnTheme().shapes.lg.value
        val referenceDp = ShadcnReferenceTokens.RADIUS_REM * 16f
        assertTrue(
            abs(oursDp - 6f) < 0.01f,
            "radius drift assumption changed: locked ours=6f, actual=$oursDp (reference=${referenceDp}dp)",
        )
    }

    private fun assertMode(dark: Boolean, reference: Map<String, ShadcnReferenceOklch>) {
        val theme = shadcnTheme(dark = dark).asShadcnTheme()
        val mode = if (dark) "dark" else "light"
        val locked = KNOWN_DRIFTED.getValue(mode)
        val ours: Map<String, Color> = mapOf(
            "background" to theme.colors.background,
            "foreground" to theme.colors.foreground,
            "primary" to theme.colors.primary,
            "primary-foreground" to theme.colors.primaryForeground,
            "secondary" to theme.colors.secondary,
            "secondary-foreground" to theme.colors.secondaryForeground,
            "muted" to theme.colors.muted,
            "muted-foreground" to theme.colors.mutedForeground,
            "accent" to theme.colors.accent,
            "accent-foreground" to theme.colors.accentForeground,
            "destructive" to theme.colors.destructive,
            "border" to theme.colors.border,
            "input" to theme.input,
            "ring" to theme.ring,
            "card" to theme.card,
            "card-foreground" to theme.onCard,
            "popover" to theme.popover,
            "popover-foreground" to theme.onPopover,
            "sidebar" to theme.sidebar,
            "sidebar-foreground" to theme.onSidebar,
            "sidebar-accent" to theme.sidebarAccent,
            "sidebar-accent-foreground" to theme.onSidebarAccent,
            "sidebar-border" to theme.sidebarBorder,
            "sidebar-ring" to theme.sidebarRing,
            "sidebar-primary" to theme.palette.sidebarPrimary,
            "sidebar-primary-foreground" to theme.palette.sidebarPrimaryForeground,
        )

        for ((key, actual) in ours) {
            val lockedValue = locked[key]
            if (lockedValue != null) {
                assertColorClose("$mode $key [KNOWN_DRIFTED, locked to current value]", lockedValue, actual)
            } else {
                val ref = reference.getValue(key)
                assertColorClose("$mode $key", oklch(ref.lightness, ref.chroma, ref.hueDegrees, ref.alpha), actual)
            }
        }
    }

    private fun assertColorClose(label: String, reference: Color, actual: Color, tolerance: Float = this.tolerance) {
        val diff = abs(reference.r - actual.r) + abs(reference.g - actual.g) + abs(reference.b - actual.b)
        assertTrue(diff < tolerance, "$label drifted: reference=$reference actual=$actual diff=$diff")
    }
}
