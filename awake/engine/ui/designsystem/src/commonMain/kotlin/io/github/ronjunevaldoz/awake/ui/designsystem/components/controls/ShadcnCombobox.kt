// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.controls

import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnTextFieldVariant
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceVisuals
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.combobox

/** Shadcn Combobox behavior recipe; Headless owns selection and popup state. */
fun UiScope.shadcnCombobox(
    id: String,
    options: List<String>,
    selectedIndex: Int? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "",
    filterPlaceholder: String = "Search...",
    emptyLabel: String = "No results found.",
): Int? = combobox(
    id = id,
    options = options,
    selectedIndex = selectedIndex,
    modifier = modifier,
    enabled = enabled,
    placeholder = placeholder,
    filterPlaceholder = filterPlaceholder,
    emptyLabel = emptyLabel,
    visuals = fieldVisuals(themeValues, ShadcnTextFieldVariant.Default),
    selectedVisuals = SurfaceStyle(
        background = themeValues.colors.accent,
        foreground = themeValues.colors.accentForeground,
        cornerRadius = themeValues.shapes.md,
    ),
    optionVisuals = SurfaceVisuals(
        rest = SurfaceStyle(
            background = themeValues.colors.popover,
            foreground = themeValues.colors.popoverForeground,
            textSize = themeValues.typography.label,
        ),
        hovered = SurfaceStyle(
            background = themeValues.colors.accent,
            foreground = themeValues.colors.accentForeground,
        ),
        pressed = SurfaceStyle(
            background = themeValues.colors.accent,
            foreground = themeValues.colors.accentForeground,
        ),
        disabled = SurfaceStyle(foreground = themeValues.colors.mutedForeground),
    ),
    filterVisuals = fieldVisuals(themeValues, ShadcnTextFieldVariant.Ghost),
)

/** Generic-value overload matching the usual controlled-component call shape. */
fun <T> UiScope.shadcnCombobox(
    id: String,
    value: T?,
    options: List<T>,
    onValueChange: (T) -> Unit,
    label: (T) -> String = { it.toString() },
    placeholder: String = "",
    filterPlaceholder: String = "Search...",
    emptyLabel: String = "No results found.",
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val selectedIndex = value?.let { options.indexOf(it).takeIf { index -> index >= 0 } }
    shadcnCombobox(
        id = id,
        options = options.map(label),
        selectedIndex = selectedIndex,
        modifier = modifier,
        enabled = enabled,
        placeholder = placeholder,
        filterPlaceholder = filterPlaceholder,
        emptyLabel = emptyLabel,
    )?.let { index -> options.getOrNull(index)?.let(onValueChange) }
}
