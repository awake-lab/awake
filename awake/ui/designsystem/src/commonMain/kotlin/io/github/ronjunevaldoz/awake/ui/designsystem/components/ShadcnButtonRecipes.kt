// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.theme.FontWeight
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.AbsoluteScope
import io.github.ronjunevaldoz.awake.ui.headless.BoxScope
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.RowScope
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceVisuals
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.button
import io.github.ronjunevaldoz.awake.ui.headless.heightOrDefault

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
    modifier = modifier.heightOrDefault(size.heightDp),
    visuals = shadcnButtonVisuals(themeValues, variant, size),
    centered = centered,
    enabled = enabled,
).also { if (it) onClick?.invoke() }

fun UiScope.shadcnButton(
    id: String,
    modifier: Modifier = Modifier,
    variant: ShadcnButtonVariant = ShadcnButtonVariant.Primary,
    size: ShadcnButtonSize = ShadcnButtonSize.Md,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: BoxScope.(slot: UiBounds) -> Unit,
): Boolean = button(
    id = id,
    modifier = modifier.heightOrDefault(size.heightDp),
    visuals = shadcnButtonVisuals(themeValues, variant, size),
    enabled = enabled,
    content = content,
).also { if (it) onClick?.invoke() }

private fun shadcnButtonVisuals(
    theme: UiThemeValues,
    variant: ShadcnButtonVariant,
    size: ShadcnButtonSize,
): SurfaceVisuals {
    val colors = theme.colors
    val rest = when (variant) {
        ShadcnButtonVariant.Primary -> SurfaceStyle(colors.primary, colors.primaryForeground, cornerRadius = theme.shapes.md, contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(horizontal = size.paddingX, vertical = 0f.dp), textSize = theme.typography.body, fontWeight = FontWeight.Medium)
        ShadcnButtonVariant.Secondary -> SurfaceStyle(colors.secondary, colors.secondaryForeground, cornerRadius = theme.shapes.md, contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(horizontal = size.paddingX, vertical = 0f.dp), textSize = theme.typography.body, fontWeight = FontWeight.Medium)
        ShadcnButtonVariant.Outline -> SurfaceStyle(colors.background, colors.foreground, SurfaceBorder(1f.dp, colors.input), theme.shapes.md, io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(horizontal = size.paddingX, vertical = 0f.dp), textSize = theme.typography.body, fontWeight = FontWeight.Medium)
        ShadcnButtonVariant.Ghost -> SurfaceStyle(Color.Transparent, colors.foreground, cornerRadius = theme.shapes.md, contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(horizontal = size.paddingX, vertical = 0f.dp), textSize = theme.typography.body, fontWeight = FontWeight.Medium)
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
