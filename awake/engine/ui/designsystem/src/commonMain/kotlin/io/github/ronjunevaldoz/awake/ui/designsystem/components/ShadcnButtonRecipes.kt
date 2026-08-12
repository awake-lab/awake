// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.RowScope
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceVisuals
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.button
import io.github.ronjunevaldoz.awake.ui.headless.heightOrDefault
import io.github.ronjunevaldoz.awake.ui.api.theme.FontWeight

fun UiScope.shadcnButton(
    id: String,
    label: String,
    modifier: Modifier = Modifier,
    variant: ShadcnButtonVariant = ShadcnButtonVariant.Primary,
    size: ShadcnButtonSize = ShadcnButtonSize.Md,
    centered: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
): Boolean = button(
    id = id,
    label = label,
    // button.tsx sizes are fixed (`h-9`, `h-8`, ...), not minimum heights. Using heightIn here
    // left Headless's 40dp natural fallback in place, so the 36dp default button was clipped by
    // the parity canvas and its glyphs appeared cut.
    modifier = modifier.heightOrDefault(size.heightDp),
    visuals = shadcnButtonVisuals(themeValues, variant, size),
    centered = centered,
    enabled = enabled,
).also {
    if (it) {
        onClick?.invoke()
    }
}

fun ColumnScope.shadcnButton(
    id: String,
    label: String,
    modifier: Modifier = Modifier,
    variant: ShadcnButtonVariant = ShadcnButtonVariant.Primary,
    size: ShadcnButtonSize = ShadcnButtonSize.Md,
    centered: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
): Boolean = button(
    id = id,
    label = label,
    modifier = modifier.heightOrDefault(size.heightDp),
    visuals = shadcnButtonVisuals(themeValues, variant, size),
    centered = centered,
    enabled = enabled,
).also {
    if (it) {
        onClick?.invoke()
    }
}

fun RowScope.shadcnButton(
    id: String,
    label: String,
    modifier: Modifier = Modifier,
    variant: ShadcnButtonVariant = ShadcnButtonVariant.Primary,
    size: ShadcnButtonSize = ShadcnButtonSize.Md,
    centered: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
): Boolean = button(
    id = id,
    label = label,
    modifier = modifier.heightOrDefault(size.heightDp),
    visuals = shadcnButtonVisuals(themeValues, variant, size),
    centered = centered,
    enabled = enabled,
).also { if (it) onClick?.invoke() }

private fun shadcnButtonVisuals(
    theme: UiThemeValues,
    variant: ShadcnButtonVariant,
    size: ShadcnButtonSize,
): SurfaceVisuals {
    val colors = theme.colors
    val rest = when (variant) {
        // shadcn's button source uses `text-sm` (14px in the pinned reference). In the
        // default Vega preset that is the body tier; using the label tier here made every
        // button's measured text narrower than the reference before padding was even applied.
        ShadcnButtonVariant.Primary -> SurfaceStyle(colors.primary, colors.primaryForeground, cornerRadius = theme.shapes.md, contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(horizontal = size.paddingX, vertical = 0f.dp), textSize = theme.typography.body, fontWeight = FontWeight.Medium)
        ShadcnButtonVariant.Secondary -> SurfaceStyle(colors.secondary, colors.secondaryForeground, cornerRadius = theme.shapes.md, contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(horizontal = size.paddingX, vertical = 0f.dp), textSize = theme.typography.body, fontWeight = FontWeight.Medium)
        // shadcn Button outline uses `border-input`; `border` is reserved for layout chrome.
        ShadcnButtonVariant.Outline -> SurfaceStyle(colors.background, colors.foreground, SurfaceBorder(1f.dp, colors.input), theme.shapes.md, io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(horizontal = size.paddingX, vertical = 0f.dp), textSize = theme.typography.body, fontWeight = FontWeight.Medium)
        ShadcnButtonVariant.Ghost -> SurfaceStyle(Color.Transparent, colors.foreground, cornerRadius = theme.shapes.md, contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(horizontal = size.paddingX, vertical = 0f.dp), textSize = theme.typography.body, fontWeight = FontWeight.Medium)
        // button.tsx uses `text-white` for destructive, independent of the generated palette's
        // destructive-foreground token.
        ShadcnButtonVariant.Danger -> SurfaceStyle(colors.destructive, Color.White, cornerRadius = theme.shapes.md, contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(horizontal = size.paddingX, vertical = 0f.dp), textSize = theme.typography.body, fontWeight = FontWeight.Medium)
        ShadcnButtonVariant.Link -> SurfaceStyle(Color.Transparent, colors.primary, cornerRadius = theme.shapes.xs, contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(horizontal = size.paddingX, vertical = 0f.dp), textSize = theme.typography.body, fontWeight = FontWeight.Medium)
    }
    return SurfaceVisuals(
        rest = rest,
        hovered = when (variant) {
            ShadcnButtonVariant.Outline -> SurfaceStyle(colors.secondary, colors.secondaryForeground)
            ShadcnButtonVariant.Ghost -> SurfaceStyle(colors.accent, colors.accentForeground)
            else -> null
        },
        pressed = when (variant) {
            ShadcnButtonVariant.Outline, ShadcnButtonVariant.Ghost -> SurfaceStyle(colors.accent, colors.accentForeground)
            else -> null
        },
        disabled = SurfaceStyle(foreground = colors.mutedForeground),
    )
}
