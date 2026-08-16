// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.colors.Color
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Diffs every comparable resolved token for EVERY [ShadcnBaseColor] Awake ships (not just
 * `Neutral`), in both light and dark, against [ShadcnReferenceTokens] -- real shadcn/ui OKLCH
 * CSS vars machine-extracted from a pinned upstream commit for all 7 base-color theme names
 * (`tools/fetch_shadcn_reference.sh` + `tools/extract_shadcn_tokens.py`, see
 * `docs/reference/shadcn-reference-pipeline.md`). Supersedes this file's earlier Neutral-only
 * coverage, which was the only base color anyone had ever actually checked.
 *
 * Tokens skipped, not force-compared against nothing:
 * - `destructiveForeground`: shadcn's current registry has no `--destructive-foreground` var
 *   (destructive buttons hardcode white text via Tailwind instead); Awake computes one anyway.
 * - `chart-1`..`chart-5`: shadcn chart tokens Awake doesn't model.
 * - `primaryHover`/`primaryPressed`/`secondaryHover`/... : Awake-only interaction-state colors
 *   synthesized via `mix()`, not shadcn CSS vars.
 *
 * knownDrifted mechanism: this audit found real drift. A token listed in [knownDrifted]
 * (keyed `"<baseColor>.<light|dark>"`, e.g. `"stone.dark"`) is asserted against its CURRENT
 * (wrong) resolved value instead of the reference, keeping the suite green while staying a real
 * regression lock: it fails loudly the moment the drift's shape changes, and fails loudly (by
 * design) the moment someone fixes the underlying value without also deleting the entry here --
 * a forced touch-point for whoever does the fix.
 *
 * `Neutral` (hue=0, chroma=0 -- see `ShadcnBaseColor.Neutral`) has zero drift in either mode: the
 * wave-2a value-fix pass already brought it in line with the pinned reference, and it is the only
 * base color anyone had verified before this pass. The other six base colors (`Stone`, `Zinc`,
 * `Mauve`, `Olive`, `Mist`, `Taupe`) had never been checked before this audit, and every one of
 * them drifts on a consistent, structural set of tokens: `primary`/`secondary`/`muted`/`accent`
 * (plus their `-foreground` pairs), `ring`, `card`/`popover`/`sidebar`, and the `sidebar-*`
 * mirrors of the above. Root cause (reported, not fixed here per this pipeline's non-goals):
 * `ShadcnTheme.kt`'s `createPalette` derives every token for a base color from ONE fixed
 * `hueDegrees`/`chroma` pair (`ShadcnBaseColor.hueDegrees`/`.chroma`) scaled by fixed multipliers
 * per token role, whereas real shadcn's registry hand-tunes a slightly different hue/chroma per
 * token within the same base color (e.g. real shadcn `stone`'s `foreground` is
 * `oklch(0.147 0.004 49.25)` while its `primary` is `oklch(0.216 0.006 56.043)` -- different hue
 * AND chroma, not a shared pair). A single-hue/chroma-per-base-color model structurally cannot
 * reproduce that without becoming a per-token lookup table -- see the drift table in this
 * class's knownDrifted entries below and the task report that introduced them for the full
 * before/after numbers.
 */
class ShadcnReferenceTokenExpandedTest {

    // Same comparison approach as before: sum of abs channel deltas in resolved sRGB space, not
    // raw OKLCH, so it tolerates float rounding through the OKLCH->linear-sRGB->gamma conversion.
    private val tolerance = 0.02f

    private val knownDrifted: Map<String, Map<String, Color>> = mapOf(
        "stone.light" to mapOf(
            "primary" to Color(0.095243f, 0.089069f, 0.087457f, 1.0f), // reference=oklch(0.216, 0.006, 56.043)
            "secondary-foreground" to Color(0.094821f, 0.089204f, 0.087737f, 1.0f), // reference=oklch(0.216, 0.006, 56.043)
            "muted-foreground" to Color(0.455974f, 0.450174f, 0.448662f, 1.0f), // reference=oklch(0.553, 0.013, 58.071)
            "accent-foreground" to Color(0.094397f, 0.089338f, 0.088017f, 1.0f), // reference=oklch(0.216, 0.006, 56.043)
            "ring" to Color(0.638432f, 0.627659f, 0.62485f, 1.0f), // reference=oklch(0.709, 0.01, 56.259)
            "sidebar-accent-foreground" to Color(0.094821f, 0.089204f, 0.087737f, 1.0f), // reference=oklch(0.216, 0.006, 56.043)
            "sidebar-ring" to Color(0.638432f, 0.627659f, 0.62485f, 1.0f), // reference=oklch(0.709, 0.01, 56.259)
            "sidebar-primary" to Color(0.095243f, 0.089069f, 0.087457f, 1.0f), // reference=oklch(0.216, 0.006, 56.043)
        ),
        "stone.dark" to mapOf(
            "primary-foreground" to Color(0.093972f, 0.089471f, 0.088297f, 1.0f), // reference=oklch(0.216, 0.006, 56.043)
            "muted-foreground" to Color(0.633719f, 0.629094f, 0.627887f, 1.0f), // reference=oklch(0.709, 0.01, 56.259)
            "ring" to Color(0.461214f, 0.448563f, 0.445262f, 1.0f), // reference=oklch(0.553, 0.013, 58.071)
            "card" to Color(0.094397f, 0.089338f, 0.088017f, 1.0f), // reference=oklch(0.216, 0.006, 56.043)
            "popover" to Color(0.095243f, 0.089069f, 0.087457f, 1.0f), // reference=oklch(0.216, 0.006, 56.043)
            "sidebar" to Color(0.093972f, 0.089471f, 0.088297f, 1.0f), // reference=oklch(0.216, 0.006, 56.043)
            "sidebar-ring" to Color(0.461214f, 0.448563f, 0.445262f, 1.0f), // reference=oklch(0.553, 0.013, 58.071)
        ),
        "zinc.light" to mapOf(
            "muted-foreground" to Color(0.450947f, 0.451091f, 0.456029f, 1.0f), // reference=oklch(0.552, 0.016, 285.938)
            "ring" to Color(0.629102f, 0.629364f, 0.638541f, 1.0f), // reference=oklch(0.705, 0.015, 286.067)
            "sidebar-ring" to Color(0.629102f, 0.629364f, 0.638541f, 1.0f), // reference=oklch(0.705, 0.015, 286.067)
        ),
        "zinc.dark" to mapOf(
            "muted-foreground" to Color(0.629707f, 0.629823f, 0.633759f, 1.0f), // reference=oklch(0.705, 0.015, 286.067)
            "ring" to Color(0.450275f, 0.450571f, 0.461363f, 1.0f), // reference=oklch(0.552, 0.016, 285.938)
            "sidebar-ring" to Color(0.450275f, 0.450571f, 0.461363f, 1.0f), // reference=oklch(0.552, 0.016, 285.938)
        ),
        "mauve.light" to mapOf(
            "primary" to Color(0.093365f, 0.088633f, 0.095655f, 1.0f), // reference=oklch(0.212, 0.019, 322.12)
            "secondary" to Color(0.963652f, 0.958587f, 0.966126f, 1.0f), // reference=oklch(0.96, 0.003, 325.6)
            "secondary-foreground" to Color(0.093109f, 0.088808f, 0.095192f, 1.0f), // reference=oklch(0.212, 0.019, 322.12)
            "muted" to Color(0.96212f, 0.959588f, 0.963358f, 1.0f), // reference=oklch(0.96, 0.003, 325.6)
            "muted-foreground" to Color(0.454183f, 0.449775f, 0.456334f, 1.0f), // reference=oklch(0.542, 0.034, 322.5)
            "accent" to Color(0.964034f, 0.958336f, 0.966817f, 1.0f), // reference=oklch(0.96, 0.003, 325.6)
            "accent-foreground" to Color(0.092852f, 0.088982f, 0.094729f, 1.0f), // reference=oklch(0.212, 0.019, 322.12)
            "ring" to Color(0.635112f, 0.626915f, 0.639106f, 1.0f), // reference=oklch(0.711, 0.019, 323.02)
            "sidebar-accent" to Color(0.963652f, 0.958587f, 0.966126f, 1.0f), // reference=oklch(0.96, 0.003, 325.6)
            "sidebar-accent-foreground" to Color(0.093109f, 0.088808f, 0.095192f, 1.0f), // reference=oklch(0.212, 0.019, 322.12)
            "sidebar-ring" to Color(0.635112f, 0.626915f, 0.639106f, 1.0f), // reference=oklch(0.711, 0.019, 323.02)
            "sidebar-primary" to Color(0.093365f, 0.088633f, 0.095655f, 1.0f), // reference=oklch(0.212, 0.019, 322.12)
        ),
        "mauve.dark" to mapOf(
            "primary-foreground" to Color(0.092596f, 0.089156f, 0.094265f, 1.0f), // reference=oklch(0.212, 0.019, 322.12)
            "secondary" to Color(0.156922f, 0.144224f, 0.163008f, 1.0f), // reference=oklch(0.263, 0.024, 320.12)
            "muted" to Color(0.154206f, 0.146143f, 0.158099f, 1.0f), // reference=oklch(0.263, 0.024, 320.12)
            "muted-foreground" to Color(0.632288f, 0.628776f, 0.634003f, 1.0f), // reference=oklch(0.711, 0.019, 323.02)
            "accent" to Color(0.156246f, 0.144709f, 0.161785f, 1.0f), // reference=oklch(0.263, 0.024, 320.12)
            "ring" to Color(0.457333f, 0.447682f, 0.462024f, 1.0f), // reference=oklch(0.542, 0.034, 322.5)
            "card" to Color(0.092852f, 0.088982f, 0.094729f, 1.0f), // reference=oklch(0.212, 0.019, 322.12)
            "popover" to Color(0.093365f, 0.088633f, 0.095655f, 1.0f), // reference=oklch(0.212, 0.019, 322.12)
            "sidebar" to Color(0.092596f, 0.089156f, 0.094265f, 1.0f), // reference=oklch(0.212, 0.019, 322.12)
            "sidebar-accent" to Color(0.156922f, 0.144224f, 0.163008f, 1.0f), // reference=oklch(0.263, 0.024, 320.12)
            "sidebar-ring" to Color(0.457333f, 0.447682f, 0.462024f, 1.0f), // reference=oklch(0.542, 0.034, 322.5)
        ),
        "olive.light" to mapOf(
            "primary" to Color(0.088513f, 0.092002f, 0.085473f, 1.0f), // reference=oklch(0.228, 0.013, 107.4)
            "secondary" to Color(0.958433f, 0.962193f, 0.955205f, 1.0f), // reference=oklch(0.966, 0.005, 106.5)
            "secondary-foreground" to Color(0.088697f, 0.09187f, 0.085936f, 1.0f), // reference=oklch(0.228, 0.013, 107.4)
            "muted" to Color(0.95951f, 0.961391f, 0.957897f, 1.0f), // reference=oklch(0.966, 0.005, 106.5)
            "muted-foreground" to Color(0.449644f, 0.452914f, 0.446832f, 1.0f), // reference=oklch(0.58, 0.031, 107.3)
            "accent-foreground" to Color(0.08888f, 0.091738f, 0.086399f, 1.0f), // reference=oklch(0.228, 0.013, 107.4)
            "border" to Color(0.896831f, 0.899153f, 0.894841f, 1.0f), // reference=oklch(0.93, 0.007, 106.5)
            "input" to Color(0.896565f, 0.899351f, 0.894176f, 1.0f), // reference=oklch(0.93, 0.007, 106.5)
            "ring" to Color(0.626676f, 0.632752f, 0.621443f, 1.0f), // reference=oklch(0.737, 0.021, 106.9)
            "sidebar-accent" to Color(0.958433f, 0.962193f, 0.955205f, 1.0f), // reference=oklch(0.966, 0.005, 106.5)
            "sidebar-accent-foreground" to Color(0.088697f, 0.09187f, 0.085936f, 1.0f), // reference=oklch(0.228, 0.013, 107.4)
            "sidebar-border" to Color(0.896831f, 0.899153f, 0.894841f, 1.0f), // reference=oklch(0.93, 0.007, 106.5)
            "sidebar-ring" to Color(0.626676f, 0.632752f, 0.621443f, 1.0f), // reference=oklch(0.737, 0.021, 106.9)
            "sidebar-primary" to Color(0.088513f, 0.092002f, 0.085473f, 1.0f), // reference=oklch(0.228, 0.013, 107.4)
        ),
        "olive.dark" to mapOf(
            "primary" to Color(0.897097f, 0.898955f, 0.895505f, 1.0f), // reference=oklch(0.93, 0.007, 106.5)
            "primary-foreground" to Color(0.089064f, 0.091605f, 0.08686f, 1.0f), // reference=oklch(0.228, 0.013, 107.4)
            "secondary" to Color(0.143975f, 0.153266f, 0.135745f, 1.0f), // reference=oklch(0.286, 0.016, 107.4)
            "muted" to Color(0.14595f, 0.151884f, 0.14076f, 1.0f), // reference=oklch(0.286, 0.016, 107.4)
            "muted-foreground" to Color(0.62867f, 0.631276f, 0.626432f, 1.0f), // reference=oklch(0.737, 0.021, 106.9)
            "accent" to Color(0.144469f, 0.152924f, 0.137005f, 1.0f), // reference=oklch(0.286, 0.016, 107.4)
            "ring" to Color(0.447413f, 0.454553f, 0.441238f, 1.0f), // reference=oklch(0.58, 0.031, 107.3)
            "card" to Color(0.08888f, 0.091738f, 0.086399f, 1.0f), // reference=oklch(0.228, 0.013, 107.4)
            "popover" to Color(0.088513f, 0.092002f, 0.085473f, 1.0f), // reference=oklch(0.228, 0.013, 107.4)
            "sidebar" to Color(0.089064f, 0.091605f, 0.08686f, 1.0f), // reference=oklch(0.228, 0.013, 107.4)
            "sidebar-accent" to Color(0.143975f, 0.153266f, 0.135745f, 1.0f), // reference=oklch(0.286, 0.016, 107.4)
            "sidebar-ring" to Color(0.447413f, 0.454553f, 0.441238f, 1.0f), // reference=oklch(0.58, 0.031, 107.3)
        ),
        "mist.light" to mapOf(
            "primary" to Color(0.084744f, 0.092038f, 0.095142f, 1.0f), // reference=oklch(0.218, 0.008, 223.9)
            "secondary" to Color(0.95449f, 0.96224f, 0.965561f, 1.0f), // reference=oklch(0.963, 0.002, 197.1)
            "secondary-foreground" to Color(0.08528f, 0.091904f, 0.094725f, 1.0f), // reference=oklch(0.218, 0.008, 223.9)
            "muted" to Color(0.957543f, 0.961415f, 0.963075f, 1.0f), // reference=oklch(0.963, 0.002, 197.1)
            "muted-foreground" to Color(0.446202f, 0.452954f, 0.455844f, 1.0f), // reference=oklch(0.56, 0.021, 213.5)
            "accent" to Color(0.953725f, 0.962446f, 0.966182f, 1.0f), // reference=oklch(0.963, 0.002, 197.1)
            "accent-foreground" to Color(0.085813f, 0.091769f, 0.094307f, 1.0f), // reference=oklch(0.218, 0.008, 223.9)
            "border" to Color(0.894402f, 0.899182f, 0.901231f, 1.0f), // reference=oklch(0.925, 0.005, 214.3)
            "input" to Color(0.893648f, 0.899386f, 0.901844f, 1.0f), // reference=oklch(0.925, 0.005, 214.3)
            "ring" to Color(0.620258f, 0.632824f, 0.638198f, 1.0f), // reference=oklch(0.723, 0.014, 214.4)
            "sidebar-accent" to Color(0.95449f, 0.96224f, 0.965561f, 1.0f), // reference=oklch(0.963, 0.002, 197.1)
            "sidebar-accent-foreground" to Color(0.08528f, 0.091904f, 0.094725f, 1.0f), // reference=oklch(0.218, 0.008, 223.9)
            "sidebar-border" to Color(0.894402f, 0.899182f, 0.901231f, 1.0f), // reference=oklch(0.925, 0.005, 214.3)
            "sidebar-ring" to Color(0.620258f, 0.632824f, 0.638198f, 1.0f), // reference=oklch(0.723, 0.014, 214.4)
            "sidebar-primary" to Color(0.084744f, 0.092038f, 0.095142f, 1.0f), // reference=oklch(0.218, 0.008, 223.9)
        ),
        "mist.dark" to mapOf(
            "primary" to Color(0.895154f, 0.898978f, 0.900617f, 1.0f), // reference=oklch(0.925, 0.005, 214.3)
            "primary-foreground" to Color(0.086345f, 0.091633f, 0.093888f, 1.0f), // reference=oklch(0.218, 0.008, 223.9)
            "muted" to Color(0.13949f, 0.151942f, 0.157231f, 1.0f), // reference=oklch(0.275, 0.011, 216.9)
            "muted-foreground" to Color(0.625936f, 0.631309f, 0.633611f, 1.0f), // reference=oklch(0.723, 0.014, 214.4)
            "ring" to Color(0.439812f, 0.454635f, 0.460963f, 1.0f), // reference=oklch(0.56, 0.021, 213.5)
            "card" to Color(0.085813f, 0.091769f, 0.094307f, 1.0f), // reference=oklch(0.218, 0.008, 223.9)
            "popover" to Color(0.084744f, 0.092038f, 0.095142f, 1.0f), // reference=oklch(0.218, 0.008, 223.9)
            "sidebar" to Color(0.086345f, 0.091633f, 0.093888f, 1.0f), // reference=oklch(0.218, 0.008, 223.9)
            "sidebar-ring" to Color(0.439812f, 0.454635f, 0.460963f, 1.0f), // reference=oklch(0.56, 0.021, 213.5)
        ),
        "taupe.light" to mapOf(
            "primary" to Color(0.09768f, 0.088155f, 0.086695f, 1.0f), // reference=oklch(0.214, 0.009, 43.1)
            "secondary" to Color(0.968427f, 0.95809f, 0.956492f, 1.0f), // reference=oklch(0.96, 0.002, 17.2)
            "secondary-foreground" to Color(0.097044f, 0.088375f, 0.087045f, 1.0f), // reference=oklch(0.214, 0.009, 43.1)
            "muted" to Color(0.964515f, 0.959341f, 0.95854f, 1.0f), // reference=oklch(0.96, 0.002, 17.2)
            "muted-foreground" to Color(0.45832f, 0.449341f, 0.447954f, 1.0f), // reference=oklch(0.547, 0.021, 43.1)
            "accent" to Color(0.969403f, 0.957777f, 0.95598f, 1.0f), // reference=oklch(0.96, 0.002, 17.2)
            "accent-foreground" to Color(0.096406f, 0.088594f, 0.087395f, 1.0f), // reference=oklch(0.214, 0.009, 43.1)
            "ring" to Color(0.642774f, 0.626106f, 0.623534f, 1.0f), // reference=oklch(0.714, 0.014, 41.2)
            "sidebar-accent" to Color(0.968427f, 0.95809f, 0.956492f, 1.0f), // reference=oklch(0.96, 0.002, 17.2)
            "sidebar-accent-foreground" to Color(0.097044f, 0.088375f, 0.087045f, 1.0f), // reference=oklch(0.214, 0.009, 43.1)
            "sidebar-ring" to Color(0.642774f, 0.626106f, 0.623534f, 1.0f), // reference=oklch(0.714, 0.014, 41.2)
            "sidebar-primary" to Color(0.09768f, 0.088155f, 0.086695f, 1.0f), // reference=oklch(0.214, 0.009, 43.1)
        ),
        "taupe.dark" to mapOf(
            "primary-foreground" to Color(0.095764f, 0.088813f, 0.087744f, 1.0f), // reference=oklch(0.214, 0.009, 43.1)
            "muted-foreground" to Color(0.635597f, 0.628432f, 0.627324f, 1.0f), // reference=oklch(0.714, 0.014, 41.2)
            "ring" to Color(0.466275f, 0.446721f, 0.443711f, 1.0f), // reference=oklch(0.547, 0.021, 43.1)
            "card" to Color(0.096406f, 0.088594f, 0.087395f, 1.0f), // reference=oklch(0.214, 0.009, 43.1)
            "popover" to Color(0.09768f, 0.088155f, 0.086695f, 1.0f), // reference=oklch(0.214, 0.009, 43.1)
            "sidebar" to Color(0.095764f, 0.088813f, 0.087744f, 1.0f), // reference=oklch(0.214, 0.009, 43.1)
            "sidebar-ring" to Color(0.466275f, 0.446721f, 0.443711f, 1.0f), // reference=oklch(0.547, 0.021, 43.1)
        ),
    )

    @Test
    fun lightTokensMatchReferenceOrLockedDriftForEveryBaseColor() {
        for (baseColor in ShadcnBaseColor.values()) assertBaseColorMode(baseColor, dark = false)
    }

    @Test
    fun darkTokensMatchReferenceOrLockedDriftForEveryBaseColor() {
        for (baseColor in ShadcnBaseColor.values()) assertBaseColorMode(baseColor, dark = true)
    }

    @Test
    fun defaultRadiusMatchesReferenceSpec() {
        // Vega is the only preset with a correctness obligation to the real shadcn reference --
        // see ShadcnStylePresetVerificationTest for the other 7 (Awake-original, no upstream
        // counterpart) presets' own structural/regression coverage.
        val oursDp = shadcnThemeValues().shapes.lg.value
        val referenceDp = ShadcnReferenceTokens.RADIUS_REM * 16f
        assertTrue(
            abs(oursDp - referenceDp) < 0.01f,
            "radius drifted from reference: ours=$oursDp reference=$referenceDp",
        )
    }

    private fun assertBaseColorMode(baseColor: ShadcnBaseColor, dark: Boolean) {
        val refKey = baseColor.name.lowercase()
        val mode = if (dark) "dark" else "light"
        val referenceTokens = ShadcnReferenceTokens.BY_BASE_COLOR[refKey]
            ?: error(
                "no ShadcnReferenceTokens entry for base color \"$refKey\" -- " +
                    "tools/extract_shadcn_tokens.py's THEME_NAMES and ShadcnBaseColor have drifted apart",
            )
        val reference = if (dark) referenceTokens.dark else referenceTokens.light
        val theme = shadcnThemeValues(baseColor = baseColor, dark = dark)
        val locked = knownDrifted["$refKey.$mode"] ?: emptyMap()
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
                assertColorClose("$refKey $mode $key [knownDrifted, locked to current value]", lockedValue, actual)
            } else {
                val ref = reference.getValue(key)
                assertColorClose("$refKey $mode $key", oklch(ref.lightness, ref.chroma, ref.hueDegrees, ref.alpha), actual)
            }
        }
    }

    private fun assertColorClose(label: String, reference: Color, actual: Color, tolerance: Float = this.tolerance) {
        val diff = abs(reference.r - actual.r) + abs(reference.g - actual.g) + abs(reference.b - actual.b)
        assertTrue(diff < tolerance, "$label drifted: reference=$reference actual=$actual diff=$diff")
    }
}
