// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiColorTokens
import io.github.ronjunevaldoz.awake.ui.UiComponentStyles
import io.github.ronjunevaldoz.awake.ui.UiTheme
import io.github.ronjunevaldoz.awake.ui.UiTypography
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.AwakeShadcnMetrics
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.AwakeShadcnPalette
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.AwakeShadcnRadiusScale
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.sp

/**
 * A neutral-first, shadcn-inspired design-system theme that lives OUTSIDE the engine core.
 * It proves Awake's public UI API is enough to host a branded layer without modifying
 * `ui-core` or `ui-widgets`.
 *
 * The default singleton still exists for authored samples, while [awakeShadcnTheme] lets
 * callers select a runtime style preset, base palette family, and accent override.
 */
object AwakeShadcnTheme : AwakeShadcnResolvedTheme by awakeShadcnThemeData()

enum class AwakeShadcnStylePreset(
    val label: String,
    internal val baseRadius: Dp,
    internal val metrics: AwakeShadcnMetrics,
    internal val ringAlphaMultiplier: Float
) {
    Vega(
        label = "Vega",
        baseRadius = 6f.dp,
        metrics = AwakeShadcnMetrics(16f.dp, 20f.dp, 12f.dp, 8f.dp, 12f.dp, 5f.dp),
        ringAlphaMultiplier = 1f
    ),
    Nova(
        label = "Nova",
        baseRadius = 5f.dp,
        metrics = AwakeShadcnMetrics(14f.dp, 18f.dp, 10f.dp, 7f.dp, 10f.dp, 4f.dp),
        ringAlphaMultiplier = 1f
    ),
    Maia(
        label = "Maia",
        baseRadius = 10f.dp,
        metrics = AwakeShadcnMetrics(18f.dp, 22f.dp, 14f.dp, 9f.dp, 14f.dp, 6f.dp),
        ringAlphaMultiplier = 1f
    ),
    Lyra(
        label = "Lyra",
        baseRadius = 0f.dp,
        metrics = AwakeShadcnMetrics(15f.dp, 18f.dp, 11f.dp, 7f.dp, 11f.dp, 4f.dp),
        ringAlphaMultiplier = 0.9f
    ),
    Mira(
        label = "Mira",
        baseRadius = 4f.dp,
        metrics = AwakeShadcnMetrics(12f.dp, 15f.dp, 10f.dp, 6f.dp, 9f.dp, 4f.dp),
        ringAlphaMultiplier = 0.85f
    ),
    Luma(
        label = "Luma",
        baseRadius = 12f.dp,
        metrics = AwakeShadcnMetrics(18f.dp, 22f.dp, 14f.dp, 9f.dp, 14f.dp, 6f.dp),
        ringAlphaMultiplier = 0.75f
    ),
    Sera(
        label = "Sera",
        baseRadius = 7f.dp,
        metrics = AwakeShadcnMetrics(16f.dp, 20f.dp, 13f.dp, 8f.dp, 12f.dp, 5f.dp),
        ringAlphaMultiplier = 0.85f
    ),
    Rhea(
        label = "Rhea",
        baseRadius = 8f.dp,
        metrics = AwakeShadcnMetrics(14f.dp, 18f.dp, 11f.dp, 7f.dp, 10f.dp, 4f.dp),
        ringAlphaMultiplier = 0.85f
    )
}

enum class AwakeShadcnBaseColor(
    val label: String,
    internal val hueDegrees: Float,
    internal val chroma: Float
) {
    Neutral("Neutral", 0f, 0f),
    Stone("Stone", 35f, 0.012f),
    Zinc("Zinc", 285f, 0.012f),
    Mauve("Mauve", 315f, 0.018f),
    Olive("Olive", 130f, 0.016f),
    Mist("Mist", 225f, 0.015f),
    Taupe("Taupe", 28f, 0.018f)
}

enum class AwakeShadcnAccent(
    val label: String,
    internal val darkPrimary: Color?,
    internal val darkOnPrimary: Color?,
    internal val lightPrimary: Color?,
    internal val lightOnPrimary: Color?
) {
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
    Yellow("Yellow", hex(0xFACC15), hex(0x0F172A), hex(0xCA8A04), hex(0x0F172A))
}

data class AwakeShadcnThemeConfig(
    val preset: AwakeShadcnStylePreset = AwakeShadcnStylePreset.Vega,
    val baseColor: AwakeShadcnBaseColor = AwakeShadcnBaseColor.Neutral,
    val accent: AwakeShadcnAccent = AwakeShadcnAccent.Base,
    val dark: Boolean = true
)

fun awakeShadcnTheme(
    preset: AwakeShadcnStylePreset = AwakeShadcnStylePreset.Vega,
    baseColor: AwakeShadcnBaseColor = AwakeShadcnBaseColor.Neutral,
    accent: AwakeShadcnAccent = AwakeShadcnAccent.Base,
    dark: Boolean = true
): UiTheme = awakeShadcnThemeData(
    AwakeShadcnThemeConfig(
        preset = preset,
        baseColor = baseColor,
        accent = accent,
        dark = dark
    )
)

internal interface AwakeShadcnResolvedTheme : UiTheme {
    val config: AwakeShadcnThemeConfig
    val palette: AwakeShadcnPalette
    val radii: AwakeShadcnRadiusScale
    val metrics: AwakeShadcnMetrics
    override val typography: UiTypography

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

internal fun UiTheme.asAwakeShadcnTheme(): AwakeShadcnResolvedTheme = this as? AwakeShadcnResolvedTheme ?: AwakeShadcnTheme

private fun awakeShadcnThemeData(
    config: AwakeShadcnThemeConfig = AwakeShadcnThemeConfig()
): AwakeShadcnResolvedTheme = ConfiguredAwakeShadcnTheme(config)

private class ConfiguredAwakeShadcnTheme(
    override val config: AwakeShadcnThemeConfig
) : AwakeShadcnResolvedTheme {
    override val radii: AwakeShadcnRadiusScale = AwakeShadcnRadiusScale.fromBase(config.preset.baseRadius)
    override val metrics: AwakeShadcnMetrics = config.preset.metrics
    override val palette: AwakeShadcnPalette = createPalette(config)
    override val typography: UiTypography = createTypography(config)

    override val tokens: UiColorTokens = object : UiColorTokens {
        override val background = palette.background
        override val foreground = palette.foreground
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
    }

    override val components: UiComponentStyles = object : UiComponentStyles {
        override val button: Style = Style {
            background(palette.secondary)
            foreground(palette.secondaryForeground)
            shape(radii.lg)
            textSize(typography.label)
        }
        override val toggle: Style = button
        override val checkbox: Style = Style {
            background(palette.background)
            foreground(palette.foreground)
            borderWidth(1f.dp)
            borderColor(palette.input)
            shape(radii.md)
            textSize(typography.label)
        }
        override val slider: Style = Style {
            background(palette.input)
            foreground(palette.foreground)
            borderWidth(1f.dp)
            borderColor(palette.input)
            shape(radii.full)
            textSize(typography.label)
        }
        override val dropdown: Style = Style {
            background(palette.background)
            foreground(palette.foreground)
            borderWidth(1f.dp)
            borderColor(palette.input)
            shape(radii.lg)
            contentPadding(metrics.fieldPaddingX, metrics.fieldPaddingY)
            textSize(typography.label)
        }
        override val panel: Style = Style {
            background(palette.card)
            foreground(palette.cardForeground)
            borderWidth(1f.dp)
            borderColor(palette.border)
            shape(radii.xl)
            contentPadding(metrics.panelPadding)
        }
        override val textField: Style = Style {
            background(palette.background)
            foreground(palette.foreground)
            borderWidth(1f.dp)
            borderColor(palette.input)
            shape(radii.md)
            contentPadding(metrics.fieldPaddingX, metrics.fieldPaddingY)
            textSize(typography.label)
            focused { borderColor(palette.ring) }
            disabled {
                background(palette.muted)
                foreground(palette.mutedForeground)
                borderColor(palette.input)
            }
        }
    }
}

private fun createTypography(config: AwakeShadcnThemeConfig): UiTypography = when (config.preset) {
    AwakeShadcnStylePreset.Vega -> UiTypography(caption = 11.sp, label = 13.sp, body = 14.sp, title = 18.sp, headline = 22.sp, display = 28.sp)
    AwakeShadcnStylePreset.Nova -> UiTypography(caption = 11.sp, label = 12.sp, body = 13.sp, title = 17.sp, headline = 21.sp, display = 26.sp)
    AwakeShadcnStylePreset.Maia -> UiTypography(caption = 12.sp, label = 14.sp, body = 15.sp, title = 20.sp, headline = 24.sp, display = 30.sp)
    AwakeShadcnStylePreset.Lyra -> UiTypography(caption = 11.sp, label = 13.sp, body = 14.sp, title = 18.sp, headline = 22.sp, display = 26.sp)
    AwakeShadcnStylePreset.Mira -> UiTypography(caption = 10.sp, label = 12.sp, body = 13.sp, title = 17.sp, headline = 20.sp, display = 24.sp)
    AwakeShadcnStylePreset.Luma -> UiTypography(caption = 12.sp, label = 14.sp, body = 15.sp, title = 20.sp, headline = 24.sp, display = 30.sp)
    AwakeShadcnStylePreset.Sera -> UiTypography(caption = 12.sp, label = 14.sp, body = 15.sp, title = 21.sp, headline = 25.sp, display = 30.sp)
    AwakeShadcnStylePreset.Rhea -> UiTypography(caption = 11.sp, label = 13.sp, body = 14.sp, title = 18.sp, headline = 21.sp, display = 27.sp)
}

private fun createPalette(config: AwakeShadcnThemeConfig): AwakeShadcnPalette {
    val hue = config.baseColor.hueDegrees
    val chroma = config.baseColor.chroma
    val dark = config.dark

    val background = if (dark) oklch(0.145f, chroma * 0.18f, hue) else oklch(1f, chroma * 0.02f, hue)
    val foreground = if (dark) oklch(0.985f, chroma * 0.02f, hue) else oklch(0.145f, chroma * 0.18f, hue)
    val secondary = if (dark) oklch(0.269f, chroma * 0.55f, hue) else oklch(0.97f, chroma * 0.16f, hue)
    val secondaryForeground = if (dark) foreground else oklch(0.205f, chroma * 0.2f, hue)
    val muted = if (dark) oklch(0.235f, chroma * 0.35f, hue) else oklch(0.97f, chroma * 0.08f, hue)
    val mutedForeground = if (dark) oklch(0.708f, chroma * 0.12f, hue) else oklch(0.556f, chroma * 0.16f, hue)
    val accentSurface = if (dark) oklch(0.32f, chroma * 0.5f, hue) else oklch(0.94f, chroma * 0.18f, hue)
    val accentSurfaceForeground = if (dark) foreground else oklch(0.205f, chroma * 0.18f, hue)
    val card = if (dark) oklch(0.168f, chroma * 0.18f, hue) else oklch(1f, chroma * 0.03f, hue)
    val cardForeground = foreground
    val popover = if (dark) oklch(0.205f, chroma * 0.22f, hue) else oklch(1f, chroma * 0.04f, hue)
    val popoverForeground = cardForeground
    val sidebar = if (dark) oklch(0.158f, chroma * 0.16f, hue) else oklch(0.988f, chroma * 0.03f, hue)
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

    return AwakeShadcnPalette(
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
        destructiveForeground = if (dark) oklch(0.985f, 0f) else Color.White,
        destructiveHover = if (dark) oklch(0.652f, 0.191f, 22.216f) else oklch(
            0.537f,
            0.245f,
            27.325f
        ),
        destructivePressed = if (dark) oklch(0.604f, 0.191f, 22.216f) else oklch(
            0.507f,
            0.245f,
            27.325f
        ),
        border = border,
        ring = ring,
        input = input,
        card = card,
        cardForeground = cardForeground,
        popover = popover,
        popoverForeground = popoverForeground,
        sidebar = sidebar,
        sidebarForeground = sidebarForeground,
        sidebarPrimary = primary,
        sidebarPrimaryForeground = primaryForeground,
        sidebarAccent = secondary,
        sidebarAccentForeground = secondaryForeground,
        sidebarBorder = border,
        sidebarRing = ring
    )
}

private fun mix(
    from: Color,
    to: Color,
    fraction: Float
): Color {
    val t = fraction.coerceIn(0f, 1f)
    return Color(
        r = lerp(from.r, to.r, t),
        g = lerp(from.g, to.g, t),
        b = lerp(from.b, to.b, t),
        a = lerp(from.a, to.a, t)
    )
}

private fun lerp(
    start: Float,
    end: Float,
    fraction: Float
): Float = start + (end - start) * fraction

internal fun hex(
    rgb: Int,
    alpha: Float = 1f
): Color = Color(
    r = ((rgb shr 16) and 0xFF) / 255f,
    g = ((rgb shr 8) and 0xFF) / 255f,
    b = (rgb and 0xFF) / 255f,
    a = alpha
)
