// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.tailwindgenerator

/**
 * Vendored, not fetched -- Tailwind's default spacing scale is stable across versions (rem-based,
 * hasn't changed shape since Tailwind's earliest releases) and this project already vendors other
 * external specs the same way (MoltenVK, Jolt) rather than reaching the network at build time.
 * Regenerate [Tw] by re-running this module's `main()` if the vendored scale below ever needs an
 * entry added.
 *
 * One scale, not two: Tailwind's `h-*`/`w-*`/`p-*`/`gap-*`/etc. utilities all read the SAME
 * numbered spacing scale (`h-9` and `p-9` are both `9 -> 2.25rem`), just applied to different CSS
 * properties -- there's no separate "height scale". Radius is deliberately NOT vendored here: in
 * this codebase it's theme-relative (`ShadcnStylePreset.baseRadius` with per-preset multipliers),
 * not a fixed Tailwind constant, so a static `Tw.Radius` would conflict with the existing, correct
 * theme-driven radius system rather than complement it.
 *
 * Key is the Tailwind scale step's own name (`"0.5"`, `"1"`, `"1.5"`, ...); value is that step's
 * real pixel value at the standard 16px root (`1rem = 16px`), which this codebase's `Dp` already
 * treats as 1:1 with dp (see `skills/awake-shadcn-styling/SKILL.md`'s own worked conversions).
 */
@Suppress("MagicNumber") // the whole point of this table is to BE Tailwind's literal scale values
val tailwindSpacingScalePx: List<Pair<String, Float>> = listOf(
    "0" to 0f,
    "px" to 1f,
    "0.5" to 2f,
    "1" to 4f,
    "1.5" to 6f,
    "2" to 8f,
    "2.5" to 10f,
    "3" to 12f,
    "3.5" to 14f,
    "4" to 16f,
    "5" to 20f,
    "6" to 24f,
    "7" to 28f,
    "8" to 32f,
    "9" to 36f,
    "10" to 40f,
    "11" to 44f,
    "12" to 48f,
    "14" to 56f,
    "16" to 64f,
    "20" to 80f,
    "24" to 96f,
    "28" to 112f,
    "32" to 128f,
    "36" to 144f,
    "40" to 160f,
    "44" to 176f,
    "48" to 192f,
    "52" to 208f,
    "56" to 224f,
    "60" to 240f,
    "64" to 256f,
    "72" to 288f,
    "80" to 320f,
    "96" to 384f,
)
