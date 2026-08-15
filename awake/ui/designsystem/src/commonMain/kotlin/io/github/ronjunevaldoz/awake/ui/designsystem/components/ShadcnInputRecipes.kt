// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("TooManyFunctions")

package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnTextFieldVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnInputGroupAffixStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnInputGroupAffixTextStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnInputGroupStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnSelectedOptionStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnSelectOptionStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnSliderStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnTextFieldStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnTextareaStyle
import io.github.ronjunevaldoz.awake.ui.headless.BoxScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
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
    style = themeValues.shadcnTextFieldStyle(ShadcnTextFieldVariant.Default),
    selectedStyle = shadcnSelectedOptionStyle(themeValues),
    optionStyle = shadcnSelectOptionStyle(themeValues),
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
    style = themeValues.shadcnTextFieldStyle(ShadcnTextFieldVariant.Default),
    selectedStyle = shadcnSelectedOptionStyle(themeValues),
    optionStyle = shadcnSelectOptionStyle(themeValues),
    filterStyle = themeValues.shadcnTextFieldStyle(ShadcnTextFieldVariant.Ghost),
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
    style = themeValues.shadcnTextFieldStyle(variant),
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
    style = themeValues.shadcnTextareaStyle(variant),
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
    style = shadcnSliderStyle(themeValues),
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
    style = shadcnSliderStyle(themeValues),
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
        style = shadcnInputGroupStyle(themeValues),
    ) {
        row(verticalAlignment = UiAlignment.Vertical.Center) {
            prefixText?.let { prefix ->
                surface(
                    id = "$id.prefix",
                    modifier = Modifier.padding(start = 12f.dp, end = 12f.dp),
                    style = shadcnInputGroupAffixStyle(themeValues),
                ) {
                    text(label = prefix, style = shadcnInputGroupAffixTextStyle(themeValues))
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
                    style = shadcnInputGroupAffixStyle(themeValues),
                ) {
                    text(label = suffix, style = shadcnInputGroupAffixTextStyle(themeValues))
                }
            }
        }
    }
    return result
}
