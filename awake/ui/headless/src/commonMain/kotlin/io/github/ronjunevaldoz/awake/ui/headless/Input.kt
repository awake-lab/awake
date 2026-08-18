// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.rangeSlider as primitiveRangeSlider
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.slider as primitiveSlider
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.textField as primitiveTextField
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.textarea as primitiveTextarea
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.style.Style

/** Style-native single-line text input. */
fun UiScope.textField(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    isError: Boolean = false,
    leadingIcon: (BoxScope.() -> Unit)? = null,
    trailingIcon: (BoxScope.() -> Unit)? = null,
    visualTransformation: (String) -> String = { it },
): String = primitive.primitiveTextField(
    id, value, placeholder, modifier, style, enabled, isError,
    leadingIcon?.let { content -> { content(asHeadlessScope()) } },
    trailingIcon?.let { content -> { content(asHeadlessScope()) } }, visualTransformation,
)

/** Style-native multi-line text input. */
fun UiScope.textarea(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    isError: Boolean = false,
    minLines: Int = 3,
): String = primitive.primitiveTextarea(
    id, value, placeholder, modifier, style, enabled, isError, minLines,
)

/** Style-native continuous slider. */
fun UiScope.slider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    showKnob: Boolean = true,
): Float = primitive.primitiveSlider(id, min, max, value, label, modifier, style, enabled, showKnob)

/** Style-native dual-thumb slider. */
fun UiScope.rangeSlider(
    id: String,
    min: Float,
    max: Float,
    valueStart: Float,
    valueEnd: Float,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
): Pair<Float, Float> = primitive.primitiveRangeSlider(
    id, min, max, valueStart, valueEnd, label, modifier, style, enabled,
)
