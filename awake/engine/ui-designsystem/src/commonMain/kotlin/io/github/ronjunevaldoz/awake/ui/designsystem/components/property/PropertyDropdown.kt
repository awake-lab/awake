package io.github.ronjunevaldoz.awake.ui.designsystem.components.property

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.unstyled.input.dropdown
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.width

fun UiScope.propertyDropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = UiModifier(),
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty,
    labelContent: BoxScope.(slot: UiSlot) -> Unit
): Int? {
    var resolved: Int? = null
    propertyRow(
        modifier = modifier.height(28f.dp),
        labelWidth = labelWidth,
        labelContent = labelContent
    ) { slot ->
        resolved = dropdown(
            id = id,
            options = options,
            selectedIndex = selectedIndex,
            modifier = UiModifier().width(slot.width.px).height(slot.height.px),
            style = style
        )
    }
    return resolved
}

fun UiScope.propertyDropdown(
    id: String,
    label: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = UiModifier(),
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty
): Int? = propertyDropdown(
    id = id,
    options = options,
    selectedIndex = selectedIndex,
    modifier = modifier,
    labelWidth = labelWidth,
    style = style
) {
    text(label)
}