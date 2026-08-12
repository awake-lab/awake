// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("TooManyFunctions")

package io.github.ronjunevaldoz.awake.ui.designsystem.components


import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnTextFieldVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.visuals
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnSpacing

import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment

import io.github.ronjunevaldoz.awake.ui.headless.BoxScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceVisuals
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.combobox
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.rangeSlider
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.select
import io.github.ronjunevaldoz.awake.ui.headless.slider
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.textField
import io.github.ronjunevaldoz.awake.ui.headless.textarea



internal fun fieldVisuals(values: UiThemeValues, variant: ShadcnTextFieldVariant): SurfaceVisuals =
    variant.visuals(values)


/** SelectContent rows are menu items, so they intentionally do not reuse trigger chrome. */
private fun selectOptionVisuals(values: UiThemeValues): SurfaceVisuals = SurfaceVisuals(
    rest = SurfaceStyle(
        background = values.colors.popover,
        foreground = values.colors.popoverForeground,
        contentPadding = UiInsets(horizontal = 8f.dp, vertical = 6f.dp),
        textSize = values.typography.label,
    ),
    hovered = SurfaceStyle(
        background = values.colors.accent,
        foreground = values.colors.accentForeground,
    ),
    pressed = SurfaceStyle(
        background = values.colors.accent,
        foreground = values.colors.accentForeground,
    ),
    disabled = SurfaceStyle(foreground = values.colors.mutedForeground),
)

fun UiScope.shadcnSelect(
    id: String,
    options: List<String>,
    selectedIndex: Int? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "",
): Int? = select(
    id = id,
    options = options,
    selectedIndex = selectedIndex,
    modifier = modifier,
    placeholder = placeholder,
    visuals = fieldVisuals(themeValues, ShadcnTextFieldVariant.Default),
    selectedVisuals = SurfaceStyle(
        background = themeValues.colors.accent,
        foreground = themeValues.colors.accentForeground,
        cornerRadius = themeValues.shapes.md,
    ),
    optionVisuals = selectOptionVisuals(themeValues),
    enabled = enabled,
)



fun UiScope.shadcnCombobox(
    id: String,
    options: List<String>,
    selectedIndex: Int? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "Select framework...",
    filterPlaceholder: String = "Search framework...",
    emptyLabel: String = "No framework found.",
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
    optionVisuals = selectOptionVisuals(themeValues),
    filterVisuals = fieldVisuals(themeValues, ShadcnTextFieldVariant.Ghost),
)

fun UiScope.shadcnInput(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    variant: ShadcnTextFieldVariant = ShadcnTextFieldVariant.Default,
    enabled: Boolean = true,
    isError: Boolean = false,
    leadingIcon: (BoxScope.() -> Unit)? = null,
    trailingIcon: (BoxScope.() -> Unit)? = null,
    visualTransformation: (String) -> String = { it },
): String = textField(
    id = id,
    value = value,
    placeholder = placeholder,
    modifier = modifier,
    visuals = fieldVisuals(themeValues, variant),
    enabled = enabled,
    isError = isError,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    visualTransformation = visualTransformation,
)

fun UiScope.shadcnTextarea(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    variant: ShadcnTextFieldVariant = ShadcnTextFieldVariant.Default,
    enabled: Boolean = true,
    isError: Boolean = false,
    minLines: Int = 3,
): String = textarea(
    id = id,
    value = value,
    placeholder = placeholder,
    modifier = modifier,
    visuals = fieldVisuals(themeValues, variant),
    enabled = enabled,
    isError = isError,
    minLines = minLines,
)

fun UiScope.shadcnSlider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    label: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showKnob: Boolean = true,
): Float = slider(
    id = id,
    min = min,
    max = max,
    value = value,
    label = label,
    modifier = modifier,
    visuals = SurfaceVisuals(
        rest = SurfaceStyle(
            background = themeValues.colors.muted,
            foreground = themeValues.colors.primary,
            cornerRadius = themeValues.shapes.full,
        ),
    ),
    enabled = enabled,
    showKnob = showKnob,
)

fun UiScope.shadcnRangeSlider(
    id: String,
    min: Float,
    max: Float,
    valueStart: Float,
    valueEnd: Float,
    label: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
): Pair<Float, Float> = rangeSlider(
    id = id,
    min = min,
    max = max,
    valueStart = valueStart,
    valueEnd = valueEnd,
    label = label,
    modifier = modifier,
    visuals = SurfaceVisuals(
        rest = SurfaceStyle(
            background = themeValues.colors.muted,
            foreground = themeValues.colors.primary,
            cornerRadius = themeValues.shapes.full,
        ),
    ),
    enabled = enabled,
)


fun UiScope.shadcnInputGroup(
    id: String,
    value: String,
    placeholder: String = "",
    prefixText: String? = null,
    suffixText: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
): String {
    var result = value
    surface(
        id = "$id.group",
        modifier = modifier.fillMaxWidth(),
        style = SurfaceStyle(
            background = themeValues.colors.background,
            border = SurfaceBorder(1f.dp, themeValues.colors.input),
            cornerRadius = themeValues.shapes.md,
        ),
    ) {
        row(verticalAlignment = UiAlignment.Vertical.Center) {
            prefixText?.let { prefix ->
                surface(
                    id = "$id.prefix",
                    modifier = Modifier.padding(start = 12f.dp, end = 12f.dp),
                    style = SurfaceStyle(foreground = themeValues.colors.mutedForeground),
                ) {
                    text(label = prefix, visuals = SurfaceStyle(textSize = themeValues.typography.label))
                }
            }
            result = shadcnInput(
                id = id,
                value = value,
                placeholder = placeholder,
                enabled = enabled,
                variant = ShadcnTextFieldVariant.Ghost,
            )
            suffixText?.let { suffix ->
                surface(
                    id = "$id.suffix",
                    modifier = Modifier.padding(start = 12f.dp, end = 12f.dp),
                    style = SurfaceStyle(foreground = themeValues.colors.mutedForeground),
                ) {
                    text(label = suffix, visuals = SurfaceStyle(textSize = themeValues.typography.label))
                }
            }
        }
    }
    return result
}



