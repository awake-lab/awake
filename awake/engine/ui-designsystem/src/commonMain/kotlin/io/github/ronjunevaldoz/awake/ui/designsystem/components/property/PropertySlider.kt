package io.github.ronjunevaldoz.awake.ui.designsystem.components.property

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.unstyled.input.slider
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*


fun UiScope.propertySlider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    modifier: UiModifier = Modifier,
    labelWidth: io.github.ronjunevaldoz.awake.ui.Dp = 64f.dp,
    style: Style = Style.Empty,
    labelContent: BoxScope.(slot: UiSlot) -> Unit
): Float {
    var resolved = value
    propertyRow(
        modifier = modifier,
        labelWidth = labelWidth,
        labelContent = labelContent
    ) { slot ->
        resolved = slider(
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

fun UiScope.propertySlider(
    id: String,
    label: String,
    min: Float,
    max: Float,
    value: Float,
    modifier: UiModifier = Modifier,
    labelWidth: io.github.ronjunevaldoz.awake.ui.Dp = 64f.dp,
    style: Style = Style.Empty
): Float = propertySlider(
    id = id,
    min = min,
    max = max,
    value = value,
    modifier = modifier,
    labelWidth = labelWidth,
    style = style
) {
    text(label)
}
