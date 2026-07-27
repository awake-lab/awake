// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.input

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSelect
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnInput
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTextarea
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.ShadcnFieldOrientation
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnField
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldError
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldLabel
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/** The `shadcnField*` control family: each is a horizontal [io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnField]
 * (label beside control -- matches this family's pre-existing property-row look) whose
 * [labelContent] composes a [shadcnFieldLabel] (or a plain string label via the convenience
 * overload) followed by the themed control itself. */

fun ColumnScope.shadcnFieldToggle(
    id: String,
    checked: Boolean,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    labelContent: UiScope.() -> Unit
): Boolean {
    var resolved = checked
    shadcnField(modifier = modifier, orientation = ShadcnFieldOrientation.Horizontal) {
        labelContent()
        resolved = shadcnToggle(id = id, checked = checked, style = style)
    }
    return resolved
}

fun ColumnScope.shadcnFieldToggle(
    id: String,
    checked: Boolean,
    height: Dp,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    labelContent: UiScope.() -> Unit
): Boolean = shadcnFieldToggle(
    id = id,
    checked = checked,
    modifier = modifier.height(height),
    style = style,
    labelContent = labelContent
)

fun ColumnScope.shadcnFieldToggle(
    id: String,
    label: String,
    checked: Boolean,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): Boolean = shadcnFieldToggle(
    id = id,
    checked = checked,
    modifier = modifier,
    style = style
) {
    shadcnFieldLabel(label)
}

fun ColumnScope.shadcnFieldSwitch(
    id: String,
    checked: Boolean,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    labelContent: UiScope.() -> Unit
): Boolean {
    var resolved = checked
    shadcnField(modifier = modifier, orientation = ShadcnFieldOrientation.Horizontal) {
        labelContent()
        resolved = shadcnSwitch(id = id, checked = checked, style = style)
    }
    return resolved
}

fun ColumnScope.shadcnFieldSwitch(
    id: String,
    checked: Boolean,
    height: Dp,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    labelContent: UiScope.() -> Unit
): Boolean = shadcnFieldSwitch(
    id = id,
    checked = checked,
    modifier = modifier.height(height),
    style = style,
    labelContent = labelContent
)

fun ColumnScope.shadcnFieldSwitch(
    id: String,
    label: String,
    checked: Boolean,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): Boolean = shadcnFieldSwitch(
    id = id,
    checked = checked,
    modifier = modifier,
    style = style
) {
    shadcnFieldLabel(label)
}

fun ColumnScope.shadcnFieldDropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    labelContent: UiScope.() -> Unit
): Int? {
    var resolved: Int? = null
    shadcnField(modifier = modifier, orientation = ShadcnFieldOrientation.Horizontal) {
        labelContent()
        resolved = shadcnSelect(
            id = id,
            options = options,
            selectedIndex = selectedIndex,
            modifier = Modifier.weight(1f).height(40f.dp),
            style = style
        )
    }
    return resolved
}

fun ColumnScope.shadcnFieldDropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    height: Dp,
    style: Style = Style.Empty,
    labelContent: UiScope.() -> Unit
): Int? {
    var resolved: Int? = null
    shadcnField(modifier = Modifier.height(height), orientation = ShadcnFieldOrientation.Horizontal) {
        labelContent()
        resolved = shadcnSelect(
            id = id,
            options = options,
            selectedIndex = selectedIndex,
            modifier = Modifier.weight(1f).height(height),
            style = style
        )
    }
    return resolved
}

fun ColumnScope.shadcnFieldDropdown(
    id: String,
    label: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): Int? = shadcnFieldDropdown(
    id = id,
    options = options,
    selectedIndex = selectedIndex,
    modifier = modifier,
    style = style
) {
    shadcnFieldLabel(label)
}

fun ColumnScope.shadcnFieldTextField(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    // Real shadcn's TextField itself never renders error/helper text -- that's the enclosing
    // Field's job (a separate description/error-text slot below the control). Matching that
    // split here: passing errorText both flips the field into its error visual state (red
    // border) and renders the message via shadcnFieldError underneath.
    errorText: String? = null,
    labelContent: UiScope.() -> Unit
): String {
    var resolved = value
    shadcnField(modifier = modifier, orientation = ShadcnFieldOrientation.Horizontal) {
        labelContent()
        resolved = shadcnInput(
            id = id,
            value = value,
            placeholder = placeholder,
            modifier = Modifier.weight(1f).height(40f.dp),
            style = style,
            enabled = enabled,
            isError = errorText != null
        )
    }
    if (errorText != null) {
        shadcnFieldError(errorText)
    }
    return resolved
}

fun ColumnScope.shadcnFieldTextField(
    id: String,
    label: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    errorText: String? = null
): String = shadcnFieldTextField(
    id = id,
    value = value,
    placeholder = placeholder,
    modifier = modifier,
    style = style,
    enabled = enabled,
    errorText = errorText
) {
    shadcnFieldLabel(label)
}

fun ColumnScope.shadcnFieldTextarea(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    errorText: String? = null,
    minLines: Int = 3,
    labelContent: UiScope.() -> Unit
): String {
    val resolvedDefaults = theme.components.textField
    val resolvedStyle = resolveStyle(
        style = style,
        defaults = resolvedDefaults,
        state = MutableStyleState(disabled = !enabled)
    )
    val fontHeight = resolveGlyphPx(font, resolvedStyle.textStyle) ?: 0f
    val padding = resolvedStyle.contentPadding
    val totalPadding = padding.top + padding.bottom
    val lineGap = fontHeight * 0.25f
    val minHeight = (fontHeight * minLines) + (lineGap * (minLines - 1)).coerceAtLeast(0f) + totalPadding.toPx()

    var resolved = value
    shadcnField(modifier = modifier.height(minHeight.dp), orientation = ShadcnFieldOrientation.Horizontal) {
        labelContent()
        resolved = shadcnTextarea(
            id = id,
            value = value,
            placeholder = placeholder,
            modifier = Modifier.weight(1f).height(minHeight.dp),
            style = style,
            enabled = enabled,
            isError = errorText != null,
            minLines = minLines
        )
    }
    if (errorText != null) {
        shadcnFieldError(errorText)
    }
    return resolved
}

fun ColumnScope.shadcnFieldTextarea(
    id: String,
    label: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    errorText: String? = null,
    minLines: Int = 3
): String = shadcnFieldTextarea(
    id = id,
    value = value,
    placeholder = placeholder,
    modifier = modifier,
    style = style,
    enabled = enabled,
    errorText = errorText,
    minLines = minLines
) {
    shadcnFieldLabel(label)
}

fun ColumnScope.shadcnFieldSlider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    labelContent: UiScope.() -> Unit
): Float {
    var resolved = value
    shadcnField(modifier = modifier, orientation = ShadcnFieldOrientation.Horizontal) {
        labelContent()
        resolved = shadcnSlider(
            id = id,
            min = min,
            max = max,
            value = value,
            modifier = Modifier.weight(1f).height(40f.dp),
            style = style
        )
    }
    return resolved
}

fun ColumnScope.shadcnFieldSlider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    height: Dp,
    style: Style = Style.Empty,
    labelContent: UiScope.() -> Unit
): Float {
    var resolved = value
    shadcnField(modifier = Modifier.height(height), orientation = ShadcnFieldOrientation.Horizontal) {
        labelContent()
        resolved = shadcnSlider(
            id = id,
            min = min,
            max = max,
            value = value,
            modifier = Modifier.weight(1f).height(height),
            style = style
        )
    }
    return resolved
}

fun ColumnScope.shadcnFieldSlider(
    id: String,
    label: String,
    min: Float,
    max: Float,
    value: Float,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): Float = shadcnFieldSlider(
    id = id,
    min = min,
    max = max,
    value = value,
    modifier = modifier,
    style = style
) {
    shadcnFieldLabel(label)
}
