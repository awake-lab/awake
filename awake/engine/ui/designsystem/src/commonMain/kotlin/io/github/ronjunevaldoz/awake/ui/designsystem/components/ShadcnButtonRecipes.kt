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
import io.github.ronjunevaldoz.awake.ui.headless.heightIn

fun UiScope.shadcnButton(
    id: String,
    label: String,
    modifier: Modifier = Modifier,
    variant: ShadcnButtonVariant = ShadcnButtonVariant.Primary,
    size: ShadcnButtonSize = ShadcnButtonSize.Md,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
): Boolean = button(
    id = id,
    label = label,
    modifier = modifier.heightIn(min = size.heightDp),
    visuals = shadcnButtonVisuals(themeValues, variant),
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
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
): Boolean = button(
    id = id,
    label = label,
    modifier = modifier.heightIn(min = size.heightDp),
    visuals = shadcnButtonVisuals(themeValues, variant),
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
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
): Boolean = button(
    id = id,
    label = label,
    modifier = modifier.heightIn(min = size.heightDp),
    visuals = shadcnButtonVisuals(themeValues, variant),
    enabled = enabled,
).also { if (it) onClick?.invoke() }

private fun shadcnButtonVisuals(theme: UiThemeValues, variant: ShadcnButtonVariant): SurfaceVisuals {
    val colors = theme.colors
    val rest = when (variant) {
        ShadcnButtonVariant.Primary -> SurfaceStyle(colors.primary, colors.primaryForeground, cornerRadius = theme.shapes.md, textSize = theme.typography.label)
        ShadcnButtonVariant.Secondary -> SurfaceStyle(colors.secondary, colors.secondaryForeground, cornerRadius = theme.shapes.md, textSize = theme.typography.label)
        ShadcnButtonVariant.Outline -> SurfaceStyle(colors.background, colors.foreground, SurfaceBorder(1f.dp, colors.border), theme.shapes.md, textSize = theme.typography.label)
        ShadcnButtonVariant.Ghost -> SurfaceStyle(Color.Transparent, colors.foreground, cornerRadius = theme.shapes.md, textSize = theme.typography.label)
        ShadcnButtonVariant.Danger -> SurfaceStyle(colors.destructive, colors.destructiveForeground, cornerRadius = theme.shapes.md, textSize = theme.typography.label)
        ShadcnButtonVariant.Link -> SurfaceStyle(Color.Transparent, colors.primary, cornerRadius = theme.shapes.xs, textSize = theme.typography.label)
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
