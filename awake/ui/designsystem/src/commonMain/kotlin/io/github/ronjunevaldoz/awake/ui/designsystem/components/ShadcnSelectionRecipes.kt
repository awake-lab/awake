// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceVisuals
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.checkbox
import io.github.ronjunevaldoz.awake.ui.headless.switch
import io.github.ronjunevaldoz.awake.ui.headless.toggle

fun UiScope.shadcnCheckbox(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: Modifier = Modifier,
    indeterminate: Boolean = false,
    enabled: Boolean = true,
): Boolean = checkbox(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier,
    boxSize = 16f.dp,
    indeterminate = indeterminate,
    enabled = enabled,
    visuals = SurfaceVisuals(
        rest = SurfaceStyle(
            background = if (checked || indeterminate) themeValues.colors.primary else themeValues.colors.background,
            foreground = if (checked || indeterminate) themeValues.colors.primaryForeground else themeValues.colors.foreground,
            border = SurfaceBorder(
                width = 1f.dp,
                color = if (checked || indeterminate) themeValues.colors.primary else themeValues.colors.input,
            ),
            // shadcn Checkbox uses rounded-[4px], independent of the theme radius scale.
            cornerRadius = 4f.dp,
        ),
        disabled = SurfaceStyle(foreground = themeValues.colors.mutedForeground),
    ),
)

fun UiScope.shadcnSwitch(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
): Boolean = switch(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier,
    enabled = enabled,
    visuals = SurfaceVisuals(
        rest = SurfaceStyle(
            background = if (checked) themeValues.colors.primary else themeValues.colors.input,
            foreground = if (checked) themeValues.colors.primaryForeground else themeValues.colors.foreground,
            cornerRadius = themeValues.shapes.full,
        ),
        disabled = SurfaceStyle(foreground = themeValues.colors.mutedForeground),
    ),
)

fun UiScope.shadcnToggle(
    id: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit = {},
    label: String? = null,
): Boolean = toggle(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier,
    enabled = enabled,
    onCheckedChange = onCheckedChange,
    visuals = SurfaceVisuals(
        rest = SurfaceStyle(
            background = if (checked) themeValues.colors.accent else themeValues.colors.background,
            foreground = themeValues.colors.foreground,
            cornerRadius = themeValues.shapes.md,
            contentPadding = UiInsets(12f.dp, 10f.dp),
        ),
    ),
)
