// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("LongParameterList", "MagicNumber", "TooManyFunctions", "UnusedParameter")

package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnTextFieldVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.headless.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidthOrDefault
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.separator
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.weight
import kotlin.math.round

fun UiScope.shadcnField(
    id: String? = null,
    modifier: Modifier = Modifier,
    orientation: ShadcnFieldOrientation = ShadcnFieldOrientation.Vertical,
    content: ColumnScope.() -> Unit,
): UiBounds = if (orientation == ShadcnFieldOrientation.Vertical) {
    column(
        id = id,
        modifier = modifier.fillMaxWidthOrDefault(),
        verticalArrangement = Arrangement.spacedBy(12f.dp),
        content = { content() },
    )
} else {
    row(
        modifier = modifier.fillMaxWidthOrDefault(),
        horizontalArrangement = Arrangement.spacedBy(12f.dp),
        content = { column(verticalArrangement = Arrangement.spacedBy(12f.dp)) { content() } },
    )
}

fun UiScope.shadcnFieldLabel(
    text: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    disabled: Boolean = false,
): UiBounds = text(
    label = if (required) "$text *" else text,
    modifier = modifier,
    visuals = SurfaceStyle(
        foreground = if (disabled) themeValues.colors.mutedForeground else themeValues.colors.foreground,
        textSize = themeValues.typography.label,
    ),
)

fun UiScope.shadcnFieldDescription(text: String, modifier: Modifier = Modifier): UiBounds = text(
    label = text,
    modifier = modifier,
    visuals = SurfaceStyle(foreground = themeValues.colors.mutedForeground, textSize = themeValues.typography.caption),
    wrap = UiTextWrap.Word,
    overflow = UiTextOverflow.Ellipsis,
)

fun UiScope.shadcnFieldError(text: String, modifier: Modifier = Modifier): UiBounds = text(
    label = text,
    modifier = modifier,
    visuals = SurfaceStyle(foreground = themeValues.colors.destructive, textSize = themeValues.typography.caption),
    wrap = UiTextWrap.Word,
    overflow = UiTextOverflow.Ellipsis,
)

fun UiScope.shadcnFieldSet(
    id: String? = null,
    modifier: Modifier = Modifier,
    content: ColumnScope.() -> Unit,
): UiBounds = column(
    id = id,
    modifier = modifier.fillMaxWidthOrDefault(),
    verticalArrangement = Arrangement.spacedBy(24f.dp),
) { content() }

fun UiScope.shadcnFieldLegend(text: String, modifier: Modifier = Modifier): UiBounds =
    shadcnFieldLabel(text = text, modifier = modifier)

fun UiScope.shadcnFieldGroup(
    id: String? = null,
    modifier: Modifier = Modifier,
    content: ColumnScope.() -> Unit,
): UiBounds = column(
    id = id,
    modifier = modifier.fillMaxWidthOrDefault(),
    verticalArrangement = Arrangement.spacedBy(28f.dp),
) { content() }

fun UiScope.shadcnFieldSeparator(
    modifier: Modifier = Modifier,
    label: String? = null,
): UiBounds = if (label == null) {
    separator(modifier = modifier)
} else {
    row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8f.dp),
        verticalAlignment = UiAlignment.Vertical.Center,
    ) {
        surface(
            id = "field.sep.left",
            modifier = Modifier.weight(1f).height(1f.dp),
            style = SurfaceStyle(background = themeValues.colors.border),
        ) {}
        shadcnFieldDescription(label)
        surface(
            id = "field.sep.right",
            modifier = Modifier.weight(1f).height(1f.dp),
            style = SurfaceStyle(background = themeValues.colors.border),
        ) {}
    }
}

fun UiScope.shadcnFieldTextField(
    id: String,
    label: String,
    value: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    errorText: String? = null,
    variant: ShadcnTextFieldVariant = ShadcnTextFieldVariant.Default,
): String {
    shadcnFieldLabel(label)
    val next = shadcnInput(id, value, placeholder, modifier, variant, enabled, errorText != null)
    errorText?.let(::shadcnFieldError)
    return next
}

fun UiScope.shadcnFieldTextarea(
    id: String,
    label: String,
    value: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    errorText: String? = null,
    minLines: Int = 3,
    variant: ShadcnTextFieldVariant = ShadcnTextFieldVariant.Default,
): String {
    shadcnFieldLabel(label)
    val next = shadcnTextarea(id, value, placeholder, modifier, variant, enabled, errorText != null, minLines)
    errorText?.let(::shadcnFieldError)
    return next
}

fun UiScope.shadcnFieldDropdown(
    id: String,
    label: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
): Int? {
    shadcnFieldLabel(label)
    return shadcnSelect(id, options, selectedIndex, modifier, enabled)
}

fun UiScope.shadcnFieldSwitch(
    id: String,
    label: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
): Boolean {
    shadcnFieldLabel(label)
    return shadcnSwitch(id, checked, modifier = modifier, enabled = enabled)
}

fun UiScope.shadcnFieldToggle(
    id: String,
    label: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit = {},
): Boolean {
    shadcnFieldLabel(label)
    return shadcnToggle(id, checked, label = null, modifier = modifier, enabled = enabled, onCheckedChange = onCheckedChange)
}

fun UiScope.shadcnFieldSlider(
    id: String,
    label: String,
    min: Float,
    max: Float,
    value: Float,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
): Float {
    shadcnFieldLabel(label)
    return shadcnSlider(id, min, max, value, modifier = modifier, enabled = enabled)
}

/** Slider field recipe with a compact live value/range label for tooling panels. */
fun UiScope.shadcnFieldSliderWithValue(
    id: String,
    label: String,
    min: Float,
    max: Float,
    value: Float,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueLabel: (Float, Float, Float) -> String = { current, lower, upper ->
        fun rounded(number: Float): String = (round(number * 10f) / 10f).toString()
        "${rounded(current)} (${rounded(lower)}-${rounded(upper)})"
    },
): Float {
    shadcnFieldLabel(label)
    val next = shadcnSlider(
        id = id,
        min = min,
        max = max,
        value = value,
        modifier = modifier.fillMaxWidth().height(20f.dp),
        enabled = enabled,
    )
    text(
        label = valueLabel(next, min, max),
        modifier = Modifier.fillMaxWidth(),
        visuals = SurfaceStyle(foreground = themeValues.colors.mutedForeground, textSize = themeValues.typography.caption),
        wrap = UiTextWrap.None,
        overflow = UiTextOverflow.Clip,
    )
    return next
}

fun UiScope.shadcnFieldRangeSlider(
    id: String,
    label: String,
    min: Float,
    max: Float,
    valueStart: Float,
    valueEnd: Float,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
): Pair<Float, Float> {
    shadcnFieldLabel(label)
    return shadcnRangeSlider(id, min, max, valueStart, valueEnd, modifier = modifier, enabled = enabled)
}
