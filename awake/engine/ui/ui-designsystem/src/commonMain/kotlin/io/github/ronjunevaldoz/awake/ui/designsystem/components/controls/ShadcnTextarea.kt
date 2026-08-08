// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.controls

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnTextFieldVariant
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.textarea

/** Real shadcn's `Textarea`: a multi-line text field. Delegates entirely to [textarea],
 * sharing [shadcnInput]'s field styling via [shadcnFieldStyle]. */
fun UiScope.shadcnTextarea(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = Modifier,
    variant: ShadcnTextFieldVariant = ShadcnTextFieldVariant.Default,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    isError: Boolean = false,
    minLines: Int = 3,
): String = textarea(
    id = id,
    value = value,
    placeholder = placeholder,
    modifier = modifier,
    style = shadcnFieldStyle(theme, variant, style),
    enabled = enabled,
    isError = isError,
    minLines = minLines,
)
