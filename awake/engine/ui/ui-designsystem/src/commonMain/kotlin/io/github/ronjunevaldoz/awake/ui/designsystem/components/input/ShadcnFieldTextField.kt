// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.input

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.designsystem.components.controls.shadcnInput
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.ShadcnFieldOrientation
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnField
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldError
import io.github.ronjunevaldoz.awake.ui.designsystem.components.property.shadcnFieldLabel
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.style.*

/** `shadcnFieldTextField`: see the `shadcnField*` control family doc in
 * [io.github.ronjunevaldoz.awake.ui.designsystem.components.input.shadcnFieldSwitch] --
 * a horizontal [shadcnField] whose [labelContent] composes a [shadcnFieldLabel] (or a plain
 * string label via the convenience overload) followed by the themed [shadcnInput] control. */

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
