// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.input

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnTextarea
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
import io.github.ronjunevaldoz.awake.ui.scope.resolveStyle
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.style.*

/** `shadcnFieldTextarea`: see the `shadcnField*` control family doc in
 * [io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSwitch] --
 * a horizontal [shadcnField] whose [labelContent] composes a [shadcnFieldLabel] (or a plain
 * string label via the convenience overload) followed by the themed [shadcnTextarea] control. */

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
