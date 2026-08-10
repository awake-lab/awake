// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnAvatarSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.tailwind.Tw
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The spec table: every shadcn-derived constant we can reference, asserted against the real
 * Tailwind class it implements.
 *
 * **Why this exists.** Every other gate in this module compares us to our own past self --
 * screenshots, layout signatures, and the parity baseline all answer "did this change?", never
 * "is this right?". That is how ten value bugs accumulated undetected through 2026-08, including
 * a radius formula whose own verification test asserted the wrong rule and a Tabs padding whose
 * own code comment cited a class the source doesn't use. Auditing by hand found them one wave at
 * a time and never converged; this table converts that recurring cost into a permanent gate.
 *
 * **How to extend it.** Add a row. The `tailwindClass` string is documentation *and* the
 * derivation: read it off the real component source
 * (`third_party/shadcn-ui-ref/apps/v4/registry/new-york-v4/ui/<component>.tsx`, or
 * `raw.githubusercontent.com/shadcn-ui/ui/main/...`), never off how it looks in a preview. See
 * `skills/awake-shadcn-styling/SKILL.md` for reading Tailwind (`step * 4 = px`, `px` = 1px,
 * half-steps, and why `max-w-*` is a bound rather than a size).
 *
 * **What it deliberately does NOT cover.** Values hardcoded inside a component function body
 * (e.g. a `spacer(Modifier.height(8f.dp))` mid-layout) aren't referenceable from a test without
 * rendering. Those stay the job of `*FidelityTest`. The fix is to promote such values to named
 * constants -- at which point they belong here. Coverage of this table is therefore also a
 * measure of how much of the module speaks in named tokens.
 */
class ShadcnSpecAssertionTest {

    private data class SpecRow(
        val component: String,
        val role: String,
        val tailwindClass: String,
        val expected: Dp,
        val actual: Dp,
    )

    /** Vega is the only preset mapped to real shadcn -- the other seven are Awake-original
     * density variants with no upstream spec, so asserting them against Tailwind is meaningless
     * (see ShadcnStylePreset's own doc comment). */
    private val vega = shadcnTheme(preset = ShadcnStylePreset.Vega)
    private val metrics = ShadcnStylePreset.Vega.metrics

    private fun rows(): List<SpecRow> = listOf(
        // --- Button: `h-8` / `h-9` / `h-10` / `size-9` (button.tsx, buttonVariants.size) ---
        SpecRow("Button", "sm height", "h-8", Tw.Spacing.s8, ShadcnButtonSize.Sm.heightDp),
        SpecRow("Button", "default height", "h-9", Tw.Spacing.s9, ShadcnButtonSize.Md.heightDp),
        SpecRow("Button", "lg height", "h-10", Tw.Spacing.s10, ShadcnButtonSize.Lg.heightDp),
        SpecRow("Button", "icon size", "size-9", Tw.Spacing.s9, ShadcnButtonSize.Icon.heightDp),
        SpecRow("Button", "xs height", "h-6", Tw.Spacing.s6, ShadcnButtonSize.Xs.heightDp),
        SpecRow("Button", "xs padding-x", "px-2", Tw.Spacing.s2, ShadcnButtonSize.Xs.paddingX),
        SpecRow("Button", "sm padding-x", "px-3", Tw.Spacing.s3, ShadcnButtonSize.Sm.paddingX),
        SpecRow("Button", "default padding-x", "px-4", Tw.Spacing.s4, ShadcnButtonSize.Md.paddingX),
        SpecRow("Button", "lg padding-x", "px-6", Tw.Spacing.s6, ShadcnButtonSize.Lg.paddingX),

        // --- Avatar: `size-6` / `size-8` / `size-10` (avatar.tsx) ---
        SpecRow("Avatar", "sm box", "size-6", Tw.Spacing.s6, ShadcnAvatarSize.Sm.boxSize),
        SpecRow("Avatar", "default box", "size-8", Tw.Spacing.s8, ShadcnAvatarSize.Default.boxSize),
        SpecRow("Avatar", "lg box", "size-10", Tw.Spacing.s10, ShadcnAvatarSize.Lg.boxSize),

        // --- Surface insets (ShadcnMetrics, Vega) ---
        SpecRow("Card/Dialog", "panel padding", "p-6", Tw.Spacing.s6, metrics.panelPadding),
        SpecRow("Popover", "panel padding", "p-4", Tw.Spacing.s4, metrics.surfacePadding),
        SpecRow("Input/Select", "trigger padding-x", "px-3", Tw.Spacing.s3, metrics.fieldPaddingX),
        SpecRow("SelectTrigger", "padding-y", "py-2", Tw.Spacing.s2, metrics.fieldPaddingY),
        SpecRow("Input", "padding-y", "py-1", Tw.Spacing.s1, metrics.inputPaddingY),
        SpecRow("Badge", "padding-x", "px-2", Tw.Spacing.s2, metrics.badgePaddingX),
        SpecRow("Badge", "padding-y", "py-0.5", Tw.Spacing.s0_5, metrics.badgePaddingY),

        // --- Radius ladder (Tailwind v4 derives these MULTIPLICATIVELY from --radius: 0.625rem;
        // see ShadcnRadiusScaleTest for the formula itself) ---
        SpecRow("Theme", "radius lg (--radius)", "0.625rem", 10f.dpOf(), vega.shapes.lg),
    )

    @Test
    fun everyReferenceableConstantMatchesItsRealTailwindClass() {
        val failures = rows().filter { abs(it.expected.value - it.actual.value) >= 0.001f }
        assertTrue(
            failures.isEmpty(),
            buildString {
                appendLine("${failures.size} constant(s) do not match their real shadcn/ui class:")
                failures.forEach {
                    appendLine(
                        "  ${it.component} / ${it.role}: `${it.tailwindClass}` is " +
                            "${it.expected.value}dp, but we use ${it.actual.value}dp",
                    )
                }
                appendLine("Verify against third_party/shadcn-ui-ref before changing either side.")
            },
        )
    }

    /** Guards the table itself: a row whose expected value is silently zero (a typo'd `Tw`
     * constant, say) would pass the assertion above against another zero. */
    @Test
    fun specTableIsNotVacuous() {
        val rows = rows()
        assertTrue(rows.size >= 15, "spec table shrank unexpectedly: ${rows.size} rows")
        assertTrue(
            rows.none { it.expected.value <= 0f },
            "row(s) with a non-positive expected value: " +
                rows.filter { it.expected.value <= 0f }.map { "${it.component}/${it.role}" },
        )
    }
}

private fun Float.dpOf(): Dp = io.github.ronjunevaldoz.awake.ui.Dp(this)
