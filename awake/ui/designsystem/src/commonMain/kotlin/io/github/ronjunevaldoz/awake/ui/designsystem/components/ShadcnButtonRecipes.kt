// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.font.FontWeight
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

import io.github.ronjunevaldoz.awake.ui.designsystem.styles.visuals

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
    visuals = variant.visuals(themeValues, size),
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
    visuals = variant.visuals(themeValues, size),
    enabled = enabled,
    content = content,
).also { if (it) onClick?.invoke() }

