// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.input

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSelect
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSlider
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnInput
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnTextarea
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnToggle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnField
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

private fun BoxScope.shadcnFieldLabel(label: String) {
    text(
        label = label,
        verticallyCentered = true,
        style = Style {
            val shadcnTheme = theme.asShadcnTheme()
            foreground(shadcnTheme.tokens.foreground)
            textSize(shadcnTheme.typography.caption)
        }
    )
}

fun ColumnScope.shadcnFieldToggle(
    id: String,
    checked: Boolean,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    labelContent: BoxScope.(slot: UiSlot) -> Unit
): Boolean {
    var resolved = checked
    shadcnField(
        modifier = modifier.height(40f.dp),
        labelContent = labelContent
    ) { _ ->
        resolved = shadcnToggle(
            id = id,
            checked = checked,
            style = style
        )
    }
    return resolved
}

fun ColumnScope.shadcnFieldToggle(
    id: String,
    checked: Boolean,
    height: Dp,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    labelContent: BoxScope.(slot: UiSlot) -> Unit
): Boolean {
    var resolved = checked
    shadcnField(
        height = height,
        labelContent = labelContent
    ) { _ ->
        resolved = shadcnToggle(
            id = id,
            checked = checked,
            modifier = modifier,
            style = style
        )
    }
    return resolved
}

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

fun ColumnScope.shadcnFieldDropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = Modifier,
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty,
    labelContent: BoxScope.(slot: UiSlot) -> Unit
): Int? {
    var resolved: Int? = null
    shadcnField(
        modifier = modifier.height(40f.dp),
        labelWidth = labelWidth,
        labelContent = labelContent
    ) { slot ->
        resolved = shadcnSelect(
            id = id,
            options = options,
            selectedIndex = selectedIndex,
            modifier = Modifier.width(slot.width.px).height(slot.height.px),
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
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty,
    labelContent: BoxScope.(slot: UiSlot) -> Unit
): Int? {
    var resolved: Int? = null
    shadcnField(
        height = height,
        labelWidth = labelWidth,
        labelContent = labelContent
    ) { slot ->
        resolved = shadcnSelect(
            id = id,
            options = options,
            selectedIndex = selectedIndex,
            modifier = Modifier.width(slot.width.px).height(slot.height.px),
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
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty
): Int? = shadcnFieldDropdown(
    id = id,
    options = options,
    selectedIndex = selectedIndex,
    modifier = modifier,
    labelWidth = labelWidth,
    style = style
) {
    shadcnFieldLabel(label)
}

fun ColumnScope.shadcnFieldTextField(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = Modifier,
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    // Real shadcn's TextField itself never renders error/helper text -- that's the
    // enclosing Field/FieldGroup's job (a separate description/error-text slot below the
    // control). Matching that split here: passing errorText both flips the field into its
    // error visual state (red border) and renders the message as its own row underneath,
    // instead of the field having to know how to lay out helper text internally.
    errorText: String? = null,
    labelContent: BoxScope.(slot: UiSlot) -> Unit
): String {
    var resolved = value
    shadcnField(
        modifier = modifier.height(40f.dp),
        labelWidth = labelWidth,
        labelContent = labelContent
    ) { slot ->
        resolved = shadcnInput(
            id = id,
            value = value,
            placeholder = placeholder,
            modifier = Modifier.width(slot.width.px).height(slot.height.px),
            style = style,
            enabled = enabled,
            isError = errorText != null
        )
    }
    if (errorText != null) {
        shadcnSupportingText(
            errorText,
            style = Style { foreground(theme.asShadcnTheme().tokens.destructive) }
        )
    }
    return resolved
}

fun ColumnScope.shadcnFieldTextField(
    id: String,
    label: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = Modifier,
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    errorText: String? = null
): String = shadcnFieldTextField(
    id = id,
    value = value,
    placeholder = placeholder,
    modifier = modifier,
    labelWidth = labelWidth,
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
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    errorText: String? = null,
    minLines: Int = 3,
    labelContent: BoxScope.(slot: UiSlot) -> Unit
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
    shadcnField(
        modifier = modifier.height(minHeight.px),
        labelWidth = labelWidth,
        labelContent = labelContent
    ) { slot ->
        resolved = shadcnTextarea(
            id = id,
            value = value,
            placeholder = placeholder,
            modifier = Modifier.width(slot.width.px).height(slot.height.px),
            style = style,
            enabled = enabled,
            isError = errorText != null,
            minLines = minLines
        )
    }
    if (errorText != null) {
        shadcnSupportingText(
            errorText,
            style = Style { foreground(theme.asShadcnTheme().tokens.destructive) }
        )
    }
    return resolved
}

fun ColumnScope.shadcnFieldTextarea(
    id: String,
    label: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = Modifier,
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    errorText: String? = null,
    minLines: Int = 3
): String = shadcnFieldTextarea(
    id = id,
    value = value,
    placeholder = placeholder,
    modifier = modifier,
    labelWidth = labelWidth,
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
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty,
    labelContent: BoxScope.(slot: UiSlot) -> Unit
): Float {
    var resolved = value
    shadcnField(
        modifier = modifier.height(40f.dp),
        labelWidth = labelWidth,
        labelContent = labelContent
    ) { slot ->
        resolved = shadcnSlider(
            id = id,
            min = min,
            max = max,
            value = value,
            modifier = Modifier.width(slot.width.px).height(slot.height.px),
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
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty,
    labelContent: BoxScope.(slot: UiSlot) -> Unit
): Float {
    var resolved = value
    shadcnField(
        height = height,
        labelWidth = labelWidth,
        labelContent = labelContent
    ) { slot ->
        resolved = shadcnSlider(
            id = id,
            min = min,
            max = max,
            value = value,
            modifier = Modifier.width(slot.width.px).height(slot.height.px),
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
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty
): Float = shadcnFieldSlider(
    id = id,
    min = min,
    max = max,
    value = value,
    modifier = modifier,
    labelWidth = labelWidth,
    style = style
) {
    shadcnFieldLabel(label)
}
