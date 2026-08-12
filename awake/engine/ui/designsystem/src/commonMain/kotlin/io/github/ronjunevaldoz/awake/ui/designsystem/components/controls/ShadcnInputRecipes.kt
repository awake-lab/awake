// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("TooManyFunctions")

package io.github.ronjunevaldoz.awake.ui.designsystem.components.controls

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnTextFieldVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnSpacing
import io.github.ronjunevaldoz.awake.ui.headless.BoxScope
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.RowScope
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceVisuals
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.rangeSlider
import io.github.ronjunevaldoz.awake.ui.headless.select
import io.github.ronjunevaldoz.awake.ui.headless.slider
import io.github.ronjunevaldoz.awake.ui.headless.textField
import io.github.ronjunevaldoz.awake.ui.headless.textarea

internal fun fieldVisuals(values: UiThemeValues, variant: ShadcnTextFieldVariant): SurfaceVisuals {
    val colors = values.colors
    val shapes = values.shapes
    val rest = when (variant) {
        ShadcnTextFieldVariant.Default -> SurfaceStyle(
            // shadcn Input is `bg-transparent`: the page surface shows through. Keeping the
            // neutral field surface transparent is also important for `disabled:opacity-50`;
            // painting an opaque white fill and an alpha-dimmed border as separate primitives
            // double-composites the border in our immediate-mode renderer.
            background = Color.Transparent,
            foreground = colors.foreground,
            border = SurfaceBorder(1f.dp, colors.input),
            cornerRadius = shapes.md,
            contentPadding = UiInsets(ShadcnSpacing.md, ShadcnSpacing.xs),
            textSize = values.typography.label,
        )
        ShadcnTextFieldVariant.Filled -> SurfaceStyle(
            background = colors.muted,
            foreground = colors.foreground,
            cornerRadius = shapes.md,
            contentPadding = UiInsets(ShadcnSpacing.md, ShadcnSpacing.xs),
            textSize = values.typography.label,
        )
        ShadcnTextFieldVariant.Ghost -> SurfaceStyle(
            background = Color.Transparent,
            foreground = colors.foreground,
            cornerRadius = shapes.md,
            contentPadding = UiInsets(ShadcnSpacing.md, ShadcnSpacing.xs),
            textSize = values.typography.label,
        )
    }
    return SurfaceVisuals(
        rest = rest,
        hovered = if (variant == ShadcnTextFieldVariant.Ghost) null else rest.copy(background = colors.card),
        pressed = if (variant == ShadcnTextFieldVariant.Ghost) null else rest.copy(background = colors.card),
        disabled = rest.copy(foreground = colors.mutedForeground),
    )
}

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

fun ColumnScope.shadcnSelect(
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

fun RowScope.shadcnSelect(
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

fun io.github.ronjunevaldoz.awake.ui.headless.ColumnScope.shadcnInput(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    variant: ShadcnTextFieldVariant = ShadcnTextFieldVariant.Default,
    enabled: Boolean = true,
    isError: Boolean = false,
    visualTransformation: (String) -> String = { it },
): String = textField(
    id = id,
    value = value,
    placeholder = placeholder,
    modifier = modifier,
    visuals = fieldVisuals(themeValues, variant),
    enabled = enabled,
    isError = isError,
    visualTransformation = visualTransformation,
)

/** Horizontal-scope overload: nested row content must bind the widget to the row cursor rather
 * than falling back to an outer ColumnScope receiver. */
fun RowScope.shadcnInput(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    variant: ShadcnTextFieldVariant = ShadcnTextFieldVariant.Default,
    enabled: Boolean = true,
    isError: Boolean = false,
    visualTransformation: (String) -> String = { it },
): String = textField(
    id = id,
    value = value,
    placeholder = placeholder,
    modifier = modifier,
    visuals = fieldVisuals(themeValues, variant),
    enabled = enabled,
    isError = isError,
    visualTransformation = visualTransformation,
)

fun io.github.ronjunevaldoz.awake.ui.headless.ColumnScope.shadcnTextarea(
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

fun io.github.ronjunevaldoz.awake.ui.headless.ColumnScope.shadcnSlider(
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

fun io.github.ronjunevaldoz.awake.ui.headless.ColumnScope.shadcnRangeSlider(
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
