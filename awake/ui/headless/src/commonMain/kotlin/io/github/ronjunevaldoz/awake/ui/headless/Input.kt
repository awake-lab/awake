// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.rangeSlider as primitiveRangeSlider
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.slider as primitiveSlider
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.textField as primitiveTextField
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.textarea as primitiveTextarea

/** Generic single-line text input. State and focus behavior stay in the Headless primitive. */
fun UiScope.textField(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
    enabled: Boolean = true,
    isError: Boolean = false,
    leadingIcon: (BoxScope.() -> Unit)? = null,
    trailingIcon: (BoxScope.() -> Unit)? = null,
    visualTransformation: (String) -> String = { it },
): String = primitive.primitiveTextField(
    id = id,
    value = value,
    placeholder = placeholder,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    enabled = enabled,
    isError = isError,
    leadingIcon = leadingIcon?.let { content -> { content(asHeadlessScope()) } },
    trailingIcon = trailingIcon?.let { content -> { content(asHeadlessScope()) } },
    visualTransformation = visualTransformation,
)

/** Generic multi-line text input with content-derived height. */
fun UiScope.textarea(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
    enabled: Boolean = true,
    isError: Boolean = false,
    minLines: Int = 3,
): String = primitive.primitiveTextarea(
    id = id,
    value = value,
    placeholder = placeholder,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    enabled = enabled,
    isError = isError,
    minLines = minLines,
)

/** Generic continuous slider. The returned value is the next immediate-mode value. */
fun UiScope.slider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    label: String? = null,
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
    enabled: Boolean = true,
    showKnob: Boolean = true,
): Float = primitive.primitiveSlider(
    id = id,
    min = min,
    max = max,
    value = value,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    enabled = enabled,
    showKnob = showKnob,
)

/** Generic dual-thumb slider. The returned pair is the next immediate-mode range. */
fun UiScope.rangeSlider(
    id: String,
    min: Float,
    max: Float,
    valueStart: Float,
    valueEnd: Float,
    label: String? = null,
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
    enabled: Boolean = true,
): Pair<Float, Float> = primitive.primitiveRangeSlider(
    id = id,
    min = min,
    max = max,
    valueStart = valueStart,
    valueEnd = valueEnd,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    enabled = enabled,
)
