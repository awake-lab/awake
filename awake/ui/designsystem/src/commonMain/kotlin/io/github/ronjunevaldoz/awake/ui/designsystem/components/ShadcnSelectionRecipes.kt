// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.checkbox
import io.github.ronjunevaldoz.awake.ui.headless.switch
import io.github.ronjunevaldoz.awake.ui.headless.toggle
import io.github.ronjunevaldoz.awake.ui.style.Style

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
    style = Style {
        background(if (checked || indeterminate) themeValues.colors.primary else themeValues.colors.background)
        foreground(if (checked || indeterminate) themeValues.colors.primaryForeground else themeValues.colors.foreground)
        border(1f.dp, if (checked || indeterminate) themeValues.colors.primary else themeValues.colors.input)
            // shadcn Checkbox uses rounded-[4px], independent of the theme radius scale.
        shape(4f.dp)
        disabled { foreground(themeValues.colors.mutedForeground) }
    },
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
    style = Style {
        background(if (checked) themeValues.colors.primary else themeValues.colors.input)
        foreground(if (checked) themeValues.colors.primaryForeground else themeValues.colors.foreground)
        shape(themeValues.shapes.full)
        disabled { foreground(themeValues.colors.mutedForeground) }
    },
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
    style = Style {
        background(if (checked) themeValues.colors.accent else themeValues.colors.background)
        foreground(themeValues.colors.foreground)
        shape(themeValues.shapes.md)
        contentPadding(horizontal = 12f.dp, vertical = 10f.dp)
        // Reference toggle hover is muted; the on state keeps accent while hovered.
        hovered {
            background(if (checked) themeValues.colors.accent else themeValues.colors.muted)
            foreground(if (checked) themeValues.colors.foreground else themeValues.colors.mutedForeground)
        }
    },
)
