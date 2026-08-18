// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnTextStyle
import io.github.ronjunevaldoz.awake.ui.font.FontWeight
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiModifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.headless.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.tailwind.Tw

/**
 * Official shadcn/ui typography variants mapped to Tailwind v4 font sizes.
 */
enum class ShadcnTextStyle {
    H1, // 4xl font-extrabold
    H2, // 3xl font-semibold
    H3, // 2xl font-semibold
    H4, // xl font-semibold
    P, // base
    Lead, // xl text-muted-foreground
    Large, // lg font-semibold
    Small, // sm font-medium
    Muted, // sm text-muted-foreground
    Blockquote, // italic, border-l-2, pl-4
    Code, // mono, rounded, bg-muted, px-1.5 py-0.5
    Body, // Awake semantic body used when a recipe needs the installed theme's body size
    Title, // Awake semantic title used by overlays and cards
    Caption, // Awake semantic caption used by component metadata and supporting copy
}

/** Semantic foreground roles supplied by the active Core theme value. */
enum class ShadcnTextTone { Default, Muted, Destructive }

/** Explicit emphasis without exposing a per-call visual [Style] escape hatch. */
enum class ShadcnTextEmphasis { Inherit, Medium }

/**
 * Unified typography entry point for all shadcn/ui text variants.
 */
fun UiScope.shadcnText(
    label: String,
    style: ShadcnTextStyle = ShadcnTextStyle.P,
    modifier: UiModifier = Modifier,
    centered: Boolean = false,
    tone: ShadcnTextTone = ShadcnTextTone.Default,
    emphasis: ShadcnTextEmphasis = ShadcnTextEmphasis.Inherit,
    maxLines: Int = Int.MAX_VALUE,
    wrap: UiTextWrap = UiTextWrap.Word,
    overflow: UiTextOverflow = UiTextOverflow.Ellipsis,
): UiBounds {
    val (defaultSize, defaultWeight, isMutedDefault) = when (style) {
        ShadcnTextStyle.H1 -> Triple(Tw.Text.`4xl`, FontWeight.ExtraBold, false)
        ShadcnTextStyle.H2 -> Triple(Tw.Text.`3xl`, FontWeight.SemiBold, false)
        ShadcnTextStyle.H3 -> Triple(Tw.Text.`2xl`, FontWeight.SemiBold, false)
        ShadcnTextStyle.H4 -> Triple(Tw.Text.xl, FontWeight.SemiBold, false)
        ShadcnTextStyle.P -> Triple(Tw.Text.base, FontWeight.Normal, false)
        ShadcnTextStyle.Lead -> Triple(Tw.Text.xl, FontWeight.Normal, true)
        ShadcnTextStyle.Large -> Triple(Tw.Text.lg, FontWeight.SemiBold, false)
        ShadcnTextStyle.Small -> Triple(Tw.Text.sm, FontWeight.Medium, false)
        ShadcnTextStyle.Muted -> Triple(Tw.Text.sm, FontWeight.Normal, true)
        ShadcnTextStyle.Blockquote -> Triple(Tw.Text.base, FontWeight.Normal, false)
        ShadcnTextStyle.Code -> Triple(Tw.Text.sm, FontWeight.SemiBold, false)
        ShadcnTextStyle.Body -> Triple(themeValues.typography.body, FontWeight.Normal, false)
        ShadcnTextStyle.Title -> Triple(themeValues.typography.title, FontWeight.SemiBold, false)
        ShadcnTextStyle.Caption -> Triple(themeValues.typography.caption, FontWeight.Normal, false)
    }

    val effectiveTone = when {
        tone != ShadcnTextTone.Default -> tone
        isMutedDefault -> ShadcnTextTone.Muted
        else -> ShadcnTextTone.Default
    }
    val foreground = when (effectiveTone) {
        ShadcnTextTone.Default -> themeValues.colors.foreground
        ShadcnTextTone.Muted -> themeValues.colors.mutedForeground
        ShadcnTextTone.Destructive -> themeValues.colors.destructive
    }

    val defaultStyle = shadcnTextStyle(
        foreground = foreground,
        size = defaultSize,
        weight = if (emphasis == ShadcnTextEmphasis.Medium) FontWeight.Medium else defaultWeight,
    )

    val effectiveModifier = if (style == ShadcnTextStyle.Blockquote) {
        modifier.padding(start = Tw.Spacing.s4, top = 0f.dp, end = 0f.dp, bottom = 0f.dp)
    } else {
        modifier
    }

    return text(
        label = label,
        modifier = effectiveModifier,
        centered = centered,
        style = defaultStyle,
        maxLines = maxLines,
        wrap = wrap,
        overflow = overflow,
    )
}

// Convenient shorthand delegates for official shadcn typography variants:

fun UiScope.shadcnH1(label: String, modifier: UiModifier = Modifier): UiBounds =
    shadcnText(label = label, style = ShadcnTextStyle.H1, modifier = modifier)

fun UiScope.shadcnH2(label: String, modifier: UiModifier = Modifier): UiBounds =
    shadcnText(label = label, style = ShadcnTextStyle.H2, modifier = modifier)

fun UiScope.shadcnH3(label: String, modifier: UiModifier = Modifier): UiBounds =
    shadcnText(label = label, style = ShadcnTextStyle.H3, modifier = modifier)

fun UiScope.shadcnH4(label: String, modifier: UiModifier = Modifier): UiBounds =
    shadcnText(label = label, style = ShadcnTextStyle.H4, modifier = modifier)

fun UiScope.shadcnLead(label: String, modifier: UiModifier = Modifier): UiBounds =
    shadcnText(label = label, style = ShadcnTextStyle.Lead, modifier = modifier)

fun UiScope.shadcnLarge(label: String, modifier: UiModifier = Modifier): UiBounds =
    shadcnText(label = label, style = ShadcnTextStyle.Large, modifier = modifier)

fun UiScope.shadcnSmall(label: String, modifier: UiModifier = Modifier): UiBounds =
    shadcnText(label = label, style = ShadcnTextStyle.Small, modifier = modifier)

fun UiScope.shadcnMuted(
    label: String,
    modifier: UiModifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
): UiBounds = shadcnText(label = label, style = ShadcnTextStyle.Muted, modifier = modifier, maxLines = maxLines)

fun UiScope.shadcnBlockquote(label: String, modifier: UiModifier = Modifier): UiBounds =
    shadcnText(label = label, style = ShadcnTextStyle.Blockquote, modifier = modifier)

fun UiScope.shadcnCode(label: String, modifier: UiModifier = Modifier): UiBounds =
    shadcnText(label = label, style = ShadcnTextStyle.Code, modifier = modifier)

fun UiScope.shadcnSectionTitle(
    title: String,
    description: String? = null,
    modifier: UiModifier = Modifier,
    muted: Boolean = false,
): UiBounds = column(modifier = modifier) {
    shadcnText(label = title, style = ShadcnTextStyle.H3, tone = if (muted) ShadcnTextTone.Muted else ShadcnTextTone.Default)
    if (description != null) {
        shadcnText(label = description, style = ShadcnTextStyle.Muted)
    }
}

fun UiScope.shadcnTextLines(
    lines: Iterable<String>,
    modifier: UiModifier = Modifier,
): UiBounds = column(modifier = modifier) {
    lines.forEach { line ->
        shadcnText(label = line, style = ShadcnTextStyle.Muted)
    }
}
