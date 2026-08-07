// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.controls

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnStyles
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnTextFieldVariant
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.headless.input.text.textField
import io.github.ronjunevaldoz.awake.ui.style.*

internal fun shadcnFieldStyle(theme: UiTheme, style: Style): Style =
    ShadcnStyles.field(theme.asShadcnTheme()) then style

internal fun shadcnFieldStyle(
    theme: UiTheme,
    variant: ShadcnTextFieldVariant,
    style: Style
): Style = ShadcnStyles.field(theme.asShadcnTheme(), variant) then style

/** Real shadcn's `Input`: a single-line text field. Delegates entirely to [textField]. */
fun UiScope.shadcnInput(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = Modifier,
    variant: ShadcnTextFieldVariant = ShadcnTextFieldVariant.Default,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    isError: Boolean = false,
    leadingIcon: (BoxScope.() -> Unit)? = null,
    trailingIcon: (BoxScope.() -> Unit)? = null,
    visualTransformation: (String) -> String = { it }
): String = textField(
    id = id,
    value = value,
    placeholder = placeholder,
    modifier = modifier,
    style = shadcnFieldStyle(theme, variant, style),
    enabled = enabled,
    isError = isError,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    visualTransformation = visualTransformation
)

