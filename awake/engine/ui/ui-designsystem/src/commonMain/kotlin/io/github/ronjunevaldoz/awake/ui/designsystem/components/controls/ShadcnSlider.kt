// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.controls

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnStyles
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.slider
import io.github.ronjunevaldoz.awake.ui.style.*

internal fun shadcnSliderStyle(theme: UiTheme, style: Style): Style =
    ShadcnStyles.slider(theme.asShadcnTheme()) then style

/** Real shadcn's `Slider`: a draggable track/thumb picking a value in [min]..[max], sharing
 * its track/fill chrome with [shadcnProgress]. Delegates entirely to [slider]. */
fun UiScope.shadcnSlider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    label: String? = null,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty
): Float = slider(
    id = id,
    min = min,
    max = max,
    value = value,
    label = label,
    modifier = modifier,
    style = shadcnSliderStyle(theme, style)
)
