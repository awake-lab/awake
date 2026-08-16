// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.sp
import io.github.ronjunevaldoz.awake.ui.api.theme.UiColorTokens
import io.github.ronjunevaldoz.awake.ui.api.theme.UiShapeTokens
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.api.theme.UiTypography
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnMetrics
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnPalette
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnRadiusScale
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnSpacing
import io.github.ronjunevaldoz.awake.ui.theme.UiComponentStylesProvider

/**
 * A neutral-first, shadcn-inspired design-system theme that lives OUTSIDE the engine core.
 * It proves Awake's public UI API is enough to host a branded layer without modifying
 * `ui-core` or `ui-headless`.
 *
 * The default singleton still exists for authored samples, while [shadcnTheme] lets
 * callers select a runtime style preset, base palette family, and accent override.
 */
object ShadcnTheme : ShadcnResolvedTheme by shadcnThemeData()

enum class ShadcnStylePreset(val label: String, internal val baseRadius: Dp, internal val metrics: ShadcnMetrics, internal val ringAlphaMultiplier: Float) {
    // Vega is the real-shadcn-mapped preset (new-york-v4, --radius: 0.625rem = 10dp, card/dialog
    // p-6 = 24dp, popover p-4 = 16dp, badge px-2 py-0.5 = 8/2dp) -- every other preset below is
    // an Awake-original density variant with no upstream shadcn equivalent, so their numbers are
    // deliberately untouched by the wave-2a value-fix pass that brought Vega in line with the
    // pinned reference.
    Vega(
        label = "Vega",
        baseRadius = 10f.dp,
        metrics = ShadcnMetrics(24f.dp, 16f.dp, 12f.dp, 8f.dp, 8f.dp, 2f.dp, 4f.dp),
        ringAlphaMultiplier = 1f,
    ),
    Nova(
        label = "Nova",
        baseRadius = 5f.dp,
        metrics = ShadcnMetrics(14f.dp, 18f.dp, 10f.dp, 7f.dp, 10f.dp, 4f.dp, 3.5f.dp),
        ringAlphaMultiplier = 1f,
    ),
    Maia(
        label = "Maia",
        baseRadius = 10f.dp,
        metrics = ShadcnMetrics(18f.dp, 22f.dp, 14f.dp, 9f.dp, 14f.dp, 6f.dp, 4.5f.dp),
        ringAlphaMultiplier = 1f,
    ),
    Lyra(
        label = "Lyra",
        baseRadius = 0f.dp,
        metrics = ShadcnMetrics(15f.dp, 18f.dp, 11f.dp, 7f.dp, 11f.dp, 4f.dp, 3.5f.dp),
        ringAlphaMultiplier = 0.9f,
    ),
    Mira(
        label = "Mira",
        baseRadius = 4f.dp,
        metrics = ShadcnMetrics(12f.dp, 15f.dp, 10f.dp, 6f.dp, 9f.dp, 4f.dp, 3f.dp),
        ringAlphaMultiplier = 0.85f,
    ),
    Luma(
        label = "Luma",
        baseRadius = 12f.dp,
        metrics = ShadcnMetrics(18f.dp, 22f.dp, 14f.dp, 9f.dp, 14f.dp, 6f.dp, 4.5f.dp),
        ringAlphaMultiplier = 0.75f,
    ),
    Sera(
        label = "Sera",
        baseRadius = 7f.dp,
        metrics = ShadcnMetrics(16f.dp, 20f.dp, 13f.dp, 8f.dp, 12f.dp, 5f.dp, 4f.dp),
        ringAlphaMultiplier = 0.85f,
    ),
    Rhea(
        label = "Rhea",
        baseRadius = 8f.dp,
        metrics = ShadcnMetrics(14f.dp, 18f.dp, 11f.dp, 7f.dp, 10f.dp, 4f.dp, 3.5f.dp),
        ringAlphaMultiplier = 0.85f,
    ),
}

enum class ShadcnBaseColor(val label: String, internal val hueDegrees: Float, internal val chroma: Float) {
    Neutral("Neutral", 0f, 0f),
    Stone("Stone", 35f, 0.012f),
    Zinc("Zinc", 285f, 0.012f),
    Mauve("Mauve", 315f, 0.018f),
    Olive("Olive", 130f, 0.016f),
    Mist("Mist", 225f, 0.015f),
    Taupe("Taupe", 28f, 0.018f),
}

enum class ShadcnAccent(val label: String, internal val darkPrimary: Color?, internal val darkOnPrimary: Color?, internal val lightPrimary: Color?, internal val lightOnPrimary: Color?) {
    Base("Base", null, null, null, null),
    Amber("Amber", hex(0xF59E0B), hex(0x0F172A), hex(0xD97706), Color.White),
    Blue("Blue", hex(0x3B82F6), hex(0x0F172A), hex(0x2563EB), Color.White),
    Cyan("Cyan", hex(0x06B6D4), hex(0x0F172A), hex(0x0891B2), Color.White),
    Emerald("Emerald", hex(0x10B981), hex(0x0F172A), hex(0x059669), Color.White),
    Fuchsia("Fuchsia", hex(0xD8B4FE), hex(0x0F172A), hex(0xC084FC), Color.White),
    Green("Green", hex(0x22C55E), hex(0x0F172A), hex(0x16A34A), Color.White),
    Indigo("Indigo", hex(0x6366F1), hex(0x0F172A), hex(0x4F46E5), Color.White),
    Lime("Lime", hex(0xA3E635), hex(0x0F172A), hex(0x84CC16), hex(0x0F172A)),
    Orange("Orange", hex(0xF97316), hex(0x0F172A), hex(0xEA580C), Color.White),
    Pink("Pink", hex(0xEC4899), hex(0x0F172A), hex(0xDB2777), Color.White),
    Purple("Purple", hex(0xA855F7), hex(0x0F172A), hex(0x9333EA), Color.White),
    Red("Red", hex(0xEF4444), hex(0x0F172A), hex(0xDC2626), Color.White),
    Rose("Rose", hex(0xF43F5E), hex(0x0F172A), hex(0xE11D48), Color.White),
    Sky("Sky", hex(0x38BDF8), hex(0x0F172A), hex(0x0284C7), Color.White),
    Teal("Teal", hex(0x14B8A6), hex(0x0F172A), hex(0x0D9488), Color.White),
    Violet("Violet", hex(0x8B5CF6), hex(0x0F172A), hex(0x7C3AED), Color.White),
    Yellow("Yellow", hex(0xFACC15), hex(0x0F172A), hex(0xCA8A04), hex(0x0F172A)),
}

data class ShadcnThemeConfig(
    val preset: ShadcnStylePreset = ShadcnStylePreset.Vega,
    val baseColor: ShadcnBaseColor = ShadcnBaseColor.Neutral,
    val accent: ShadcnAccent = ShadcnAccent.Base,
    val dark: Boolean = true,
)

/** Builds a complete, runtime-free Shadcn theme value for [shadcnTheme]. */
fun shadcnThemeValues(
    preset: ShadcnStylePreset = ShadcnStylePreset.Vega,
    baseColor: ShadcnBaseColor = ShadcnBaseColor.Neutral,
    accent: ShadcnAccent = ShadcnAccent.Base,
    dark: Boolean = true,
): ShadcnThemeValues {
    val core = shadcnThemeData(
        ShadcnThemeConfig(
            preset = preset,
            baseColor = baseColor,
            accent = accent,
            dark = dark,
        ),
    )
    return ShadcnThemeValues(resolved = core)
}

interface ShadcnResolvedTheme : UiThemeValues {
    val config: ShadcnThemeConfig
    val palette: ShadcnPalette
    val radii: ShadcnRadiusScale
    val metrics: ShadcnMetrics
    val spacing: ShadcnSpacing get() = ShadcnSpacing
    override val typography: UiTypography
    override val shapes: UiShapeTokens get() = radii

    val card: Color get() = palette.card
    val onCard: Color get() = palette.cardForeground
    val popover: Color get() = palette.popover
    val onPopover: Color get() = palette.popoverForeground
    val sidebar: Color get() = palette.sidebar
    val onSidebar: Color get() = palette.sidebarForeground
    val sidebarAccent: Color get() = palette.sidebarAccent
    val onSidebarAccent: Color get() = palette.sidebarAccentForeground
    val sidebarBorder: Color get() = palette.sidebarBorder
    val sidebarRing: Color get() = palette.sidebarRing
    val input: Color get() = palette.input
    val ring: Color get() = palette.ring
}

private fun shadcnThemeData(config: ShadcnThemeConfig = ShadcnThemeConfig()): ShadcnResolvedTheme = ConfiguredShadcnTheme(config)

private class ConfiguredShadcnTheme(override val config: ShadcnThemeConfig) :
    ShadcnResolvedTheme,
    UiComponentStylesProvider {
    override val radii: ShadcnRadiusScale = ShadcnRadiusScale.fromBase(config.preset.baseRadius)
    override val metrics: ShadcnMetrics = config.preset.metrics
    override val palette: ShadcnPalette = createPalette(config)
    override val typography: UiTypography = createTypography(config)

    override val colors: UiColorTokens = object : UiColorTokens {
        override val background = palette.background
        override val foreground = palette.foreground
        override val card = palette.card
        override val cardForeground = palette.cardForeground
        override val popover = palette.popover
        override val popoverForeground = palette.popoverForeground
        override val primary = palette.primary
        override val primaryForeground = palette.primaryForeground
        override val secondary = palette.secondary
        override val secondaryForeground = palette.secondaryForeground
        override val muted = palette.muted
        override val mutedForeground = palette.mutedForeground
        override val accent = palette.accent
        override val accentForeground = palette.accentForeground
        override val destructive = palette.destructive
        override val destructiveForeground = palette.destructiveForeground
        override val border = palette.border
        override val input = palette.input
        override val ring = palette.ring
    }
    override val componentStyles = ShadcnComponentStyles(this)
}

private fun createTypography(config: ShadcnThemeConfig): UiTypography = when (config.preset) {
    ShadcnStylePreset.Vega -> UiTypography(caption = 11.sp, label = 13.sp, body = 14.sp, title = 18.sp, headline = 22.sp, display = 28.sp)
    ShadcnStylePreset.Nova -> UiTypography(caption = 11.sp, label = 12.sp, body = 13.sp, title = 17.sp, headline = 21.sp, display = 26.sp)
    ShadcnStylePreset.Maia -> UiTypography(caption = 12.sp, label = 14.sp, body = 15.sp, title = 20.sp, headline = 24.sp, display = 30.sp)
    ShadcnStylePreset.Lyra -> UiTypography(caption = 11.sp, label = 13.sp, body = 14.sp, title = 18.sp, headline = 22.sp, display = 26.sp)
    ShadcnStylePreset.Mira -> UiTypography(caption = 10.sp, label = 12.sp, body = 13.sp, title = 17.sp, headline = 20.sp, display = 24.sp)
    ShadcnStylePreset.Luma -> UiTypography(caption = 12.sp, label = 14.sp, body = 15.sp, title = 20.sp, headline = 24.sp, display = 30.sp)
    ShadcnStylePreset.Sera -> UiTypography(caption = 12.sp, label = 14.sp, body = 15.sp, title = 21.sp, headline = 25.sp, display = 30.sp)
    ShadcnStylePreset.Rhea -> UiTypography(caption = 11.sp, label = 13.sp, body = 14.sp, title = 18.sp, headline = 21.sp, display = 27.sp)
}

private fun createPalette(config: ShadcnThemeConfig): ShadcnPalette {
    val hue = config.baseColor.hueDegrees
    val chroma = config.baseColor.chroma
    val dark = config.dark

    // Keep the neutral light/dark values aligned with the pinned shadcn reference app's
    // `index.css`. Earlier Awake values used the dark foreground lightness for light mode,
    // which made a neutral light canvas #fefefe with #171717 text instead of white with black
    // text and caused every parity crop to drift before component styling was considered.
    val background = if (dark) oklch(0.145f, chroma * 0.18f, hue) else oklch(1f, chroma * 0.02f, hue)
    val foreground = if (dark) oklch(0.985f, chroma * 0.02f, hue) else oklch(0.145f, chroma * 0.18f, hue)
    val secondary = if (dark) oklch(0.269f, chroma * 0.55f, hue) else oklch(0.97f, chroma * 0.16f, hue)
    val secondaryForeground = if (dark) foreground else oklch(0.205f, chroma * 0.2f, hue)
    val muted = if (dark) oklch(0.269f, chroma * 0.35f, hue) else oklch(0.97f, chroma * 0.08f, hue)
    val mutedForeground = if (dark) oklch(0.708f, chroma * 0.12f, hue) else oklch(0.556f, chroma * 0.16f, hue)
    val accentSurface = if (dark) oklch(0.269f, chroma * 0.5f, hue) else oklch(0.97f, chroma * 0.18f, hue)
    val accentSurfaceForeground = if (dark) foreground else oklch(0.205f, chroma * 0.18f, hue)
    // Real shadcn's dark theme gives card/popover/sidebar the IDENTICAL lightness (0.205) --
    // they only read as different surfaces in the real app because of where they sit in the
    // DOM, not because of a color difference. `card`/`sidebar` here were drifting toward
    // `background`'s 0.145 (0.168/0.158, only 0.023/0.013 above it) instead of matching
    // `popover`'s already-correct 0.205, so in dark mode a card or the sidebar was barely
    // distinguishable from the page behind it -- the actual "wrong in dark mode" symptom this
    // was investigated for. Restored to the published value; `popover` was already right.
    val card = if (dark) oklch(0.205f, chroma * 0.18f, hue) else oklch(1f, chroma * 0.03f, hue)
    val cardForeground = foreground
    val popover = if (dark) oklch(0.205f, chroma * 0.22f, hue) else oklch(1f, chroma * 0.04f, hue)
    val popoverForeground = cardForeground
    val sidebar = if (dark) oklch(0.205f, chroma * 0.16f, hue) else oklch(0.985f, chroma * 0.03f, hue)
    val sidebarForeground = foreground
    val border = if (dark) oklch(1f, chroma * 0.1f, hue, alpha = 0.1f) else oklch(0.922f, chroma * 0.1f, hue)
    val input = if (dark) oklch(1f, chroma * 0.12f, hue, alpha = 0.15f) else oklch(0.922f, chroma * 0.12f, hue)
    val ringBase = if (dark) oklch(0.556f, chroma * 0.35f, hue) else oklch(0.708f, chroma * 0.28f, hue)

    val defaultPrimary = if (dark) oklch(0.922f, chroma * 0.08f, hue) else oklch(0.205f, chroma * 0.22f, hue)
    val defaultOnPrimary = if (dark) oklch(0.205f, chroma * 0.16f, hue) else oklch(0.985f, chroma * 0.02f, hue)

    val accentPrimary = if (dark) config.accent.darkPrimary else config.accent.lightPrimary
    val accentOnPrimary = if (dark) config.accent.darkOnPrimary else config.accent.lightOnPrimary
    val primary = accentPrimary ?: defaultPrimary
    val primaryForeground = accentOnPrimary ?: defaultOnPrimary
    val ring = (accentPrimary ?: ringBase).withAlpha(if (accentPrimary != null) 1f else config.preset.ringAlphaMultiplier)

    return ShadcnPalette(
        background = background,
        foreground = foreground,
        primary = primary,
        primaryForeground = primaryForeground,
        primaryHover = mix(primary, background, if (dark) 0.12f else 0.08f),
        primaryPressed = mix(primary, background, if (dark) 0.24f else 0.16f),
        secondary = secondary,
        secondaryForeground = secondaryForeground,
        secondaryHover = mix(secondary, foreground, if (dark) 0.08f else 0.02f),
        secondaryPressed = mix(secondary, foreground, if (dark) 0.16f else 0.05f),
        muted = muted,
        mutedForeground = mutedForeground,
        accent = accentSurface,
        accentForeground = accentSurfaceForeground,
        accentHover = mix(accentSurface, foreground, if (dark) 0.08f else 0.03f),
        accentPressed = mix(accentSurface, foreground, if (dark) 0.16f else 0.06f),
        destructive = if (dark) oklch(0.704f, 0.191f, 22.216f) else oklch(0.577f, 0.245f, 27.325f),
        destructiveForeground = if (dark) oklch(0.985f, 0f) else oklch(0.97f, 0.01f, 17f),
        destructiveHover = if (dark) {
            oklch(0.652f, 0.191f, 22.216f)
        } else {
            oklch(
                0.537f,
                0.245f,
                27.325f,
            )
        },
        destructivePressed = if (dark) {
            oklch(0.604f, 0.191f, 22.216f)
        } else {
            oklch(
                0.507f,
                0.245f,
                27.325f,
            )
        },
        border = border,
        ring = ring,
        input = input,
        card = card,
        cardForeground = cardForeground,
        popover = popover,
        popoverForeground = popoverForeground,
        sidebar = sidebar,
        sidebarForeground = sidebarForeground,
        // Real shadcn's dark sidebar-primary is a fixed distinct blue (oklch(0.488 0.243
        // 264.376)), not a reuse of the page's own `primary` -- verified identical across every
        // base color in the pinned themes.ts (neutral AND stone dark both emit this same value),
        // so it is not hue-derived like the rest of this palette. Light mode's sidebar-primary
        // genuinely does equal `primary` in the reference (both oklch(0.205 0 0) for neutral),
        // so only the dark branch needs its own literal here.
        sidebarPrimary = if (dark) oklch(0.488f, 0.243f, 264.376f) else primary,
        sidebarPrimaryForeground = if (dark) oklch(0.985f, 0f, 0f) else primaryForeground,
        sidebarAccent = secondary,
        sidebarAccentForeground = secondaryForeground,
        sidebarBorder = border,
        sidebarRing = ring,
    )
}

private fun mix(from: Color, to: Color, fraction: Float): Color {
    val t = fraction.coerceIn(0f, 1f)
    return Color(
        r = lerp(from.r, to.r, t),
        g = lerp(from.g, to.g, t),
        b = lerp(from.b, to.b, t),
        a = lerp(from.a, to.a, t),
    )
}

private fun lerp(start: Float, end: Float, fraction: Float): Float = start + (end - start) * fraction

internal fun hex(rgb: Int, alpha: Float = 1f): Color = Color(
    r = ((rgb shr 16) and 0xFF) / 255f,
    g = ((rgb shr 8) and 0xFF) / 255f,
    b = (rgb and 0xFF) / 255f,
    a = alpha,
)

/** shadcn v4's checkbox corner: a literal `rounded-[4px]`, not a step on the radius scale. */
