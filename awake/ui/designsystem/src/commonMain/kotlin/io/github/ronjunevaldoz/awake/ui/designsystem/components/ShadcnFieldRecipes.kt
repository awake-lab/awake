// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("TooManyFunctions")

package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnTextFieldVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnFieldDescriptionStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnFieldErrorStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnFieldLabelStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnFieldSeparatorStyle
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiModifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.headless.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidthOrDefault
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.weight
import kotlin.math.round

fun UiScope.shadcnField(
    id: String? = null,
    modifier: UiModifier = Modifier,
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
    modifier: UiModifier = Modifier,
    required: Boolean = false,
    disabled: Boolean = false,
): UiBounds = text(
    label = if (required) "$text *" else text,
    modifier = modifier,
    style = shadcnFieldLabelStyle(themeValues, disabled),
)

fun UiScope.shadcnFieldDescription(text: String, modifier: UiModifier = Modifier): UiBounds = text(
    label = text,
    modifier = modifier,
    style = shadcnFieldDescriptionStyle(themeValues),
    wrap = UiTextWrap.Word,
    overflow = UiTextOverflow.Ellipsis,
)

fun UiScope.shadcnFieldError(text: String, modifier: UiModifier = Modifier): UiBounds = text(
    label = text,
    modifier = modifier,
    style = shadcnFieldErrorStyle(themeValues),
    wrap = UiTextWrap.Word,
    overflow = UiTextOverflow.Ellipsis,
)

/** Shared body behind [shadcnFieldSet] and [shadcnFieldGroup] -- identical column shape, only
 * [gap] differs between the two. */
private fun UiScope.shadcnFieldContainer(
    id: String?,
    modifier: UiModifier,
    cacheKey: Any?,
    gap: Dp,
    content: ColumnScope.() -> Unit,
): UiBounds = column(
    id = id,
    modifier = modifier.fillMaxWidthOrDefault(),
    verticalArrangement = Arrangement.spacedBy(gap),
    cacheKey = cacheKey,
) { content() }

fun UiScope.shadcnFieldSet(
    id: String? = null,
    modifier: UiModifier = Modifier,
    cacheKey: Any? = null,
    content: ColumnScope.() -> Unit,
): UiBounds = shadcnFieldContainer(id, modifier, cacheKey, 24f.dp, content)

fun UiScope.shadcnFieldLegend(text: String, modifier: UiModifier = Modifier): UiBounds =
    shadcnFieldLabel(text = text, modifier = modifier)

fun UiScope.shadcnFieldGroup(
    id: String? = null,
    modifier: UiModifier = Modifier,
    cacheKey: Any? = null,
    content: ColumnScope.() -> Unit,
): UiBounds = shadcnFieldContainer(id, modifier, cacheKey, 28f.dp, content)

fun UiScope.shadcnFieldSeparator(
    id: String,
    modifier: UiModifier = Modifier,
    label: String? = null,
): UiBounds = if (label == null) {
    shadcnSeparator(id = id, modifier = modifier)
} else {
    row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8f.dp),
        verticalAlignment = UiAlignment.Vertical.Center,
    ) {
        surface(
            id = "$id.left",
            modifier = Modifier.weight(1f).height(1f.dp),
            style = shadcnFieldSeparatorStyle(themeValues),
        ) {}
        shadcnFieldDescription(label)
        surface(
            id = "$id.right",
            modifier = Modifier.weight(1f).height(1f.dp),
            style = shadcnFieldSeparatorStyle(themeValues),
        ) {}
    }
}

@Suppress("LongParameterList")
fun UiScope.shadcnFieldTextField(
    id: String,
    label: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = Modifier,
    enabled: Boolean = true,
    errorText: String? = null,
    variant: ShadcnTextFieldVariant = ShadcnTextFieldVariant.Default,
): String {
    var next = value
    shadcnField {
        shadcnFieldLabel(label)
        next = shadcnInput(id, value, placeholder, modifier, variant, enabled, errorText != null)
        errorText?.let(::shadcnFieldError)
    }
    return next
}

@Suppress("LongParameterList")
fun UiScope.shadcnFieldTextarea(
    id: String,
    label: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = Modifier,
    enabled: Boolean = true,
    errorText: String? = null,
    minLines: Int = 3,
    variant: ShadcnTextFieldVariant = ShadcnTextFieldVariant.Default,
): String {
    var next = value
    shadcnField {
        shadcnFieldLabel(label)
        next = shadcnTextarea(id, value, placeholder, modifier, variant, enabled, errorText != null, minLines)
        errorText?.let(::shadcnFieldError)
    }
    return next
}

fun UiScope.shadcnFieldDropdown(
    id: String,
    label: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = Modifier,
    enabled: Boolean = true,
): Int? {
    var picked: Int? = null
    shadcnField {
        shadcnFieldLabel(label)
        picked = shadcnSelect(id, options, selectedIndex, modifier, enabled)
    }
    return picked
}

fun UiScope.shadcnFieldSwitch(
    id: String,
    label: String,
    checked: Boolean,
    modifier: UiModifier = Modifier,
    enabled: Boolean = true,
): Boolean {
    var next = checked
    shadcnField {
        shadcnFieldLabel(label)
        next = shadcnSwitch(id, checked, modifier = modifier, enabled = enabled)
    }
    return next
}

@Suppress("LongParameterList")
fun UiScope.shadcnFieldToggle(
    id: String,
    label: String,
    checked: Boolean,
    modifier: UiModifier = Modifier,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit = {},
): Boolean {
    var next = checked
    shadcnField {
        shadcnFieldLabel(label)
        next = shadcnToggle(
            id,
            checked,
            label = null,
            modifier = modifier,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
        )
    }
    return next
}

@Suppress("LongParameterList")
fun UiScope.shadcnFieldSlider(
    id: String,
    label: String,
    min: Float,
    max: Float,
    value: Float,
    modifier: UiModifier = Modifier,
    enabled: Boolean = true,
): Float {
    var next = value
    shadcnField {
        shadcnFieldLabel(label)
        next = shadcnSlider(id, min, max, value, modifier = modifier, enabled = enabled)
    }
    return next
}

/** Slider field recipe with a compact live value/range label for tooling panels. */
@Suppress("LongParameterList", "MagicNumber")
fun UiScope.shadcnFieldSliderWithValue(
    id: String,
    label: String,
    min: Float,
    max: Float,
    value: Float,
    modifier: UiModifier = Modifier,
    enabled: Boolean = true,
    valueLabel: (Float, Float, Float) -> String = { current, lower, upper ->
        fun rounded(number: Float): String = (round(number * 10f) / 10f).toString()
        "${rounded(current)} (${rounded(lower)}-${rounded(upper)})"
    },
): Float {
    var next = value
    shadcnField {
        shadcnFieldLabel(label)
        next = shadcnSlider(
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
            style = shadcnFieldDescriptionStyle(themeValues),
            wrap = UiTextWrap.None,
            overflow = UiTextOverflow.Clip,
        )
    }
    return next
}

@Suppress("LongParameterList")
fun UiScope.shadcnFieldRangeSlider(
    id: String,
    label: String,
    min: Float,
    max: Float,
    valueStart: Float,
    valueEnd: Float,
    modifier: UiModifier = Modifier,
    enabled: Boolean = true,
): Pair<Float, Float> {
    var next = valueStart to valueEnd
    shadcnField {
        shadcnFieldLabel(label)
        next = shadcnRangeSlider(id, min, max, valueStart, valueEnd, modifier = modifier, enabled = enabled)
    }
    return next
}
