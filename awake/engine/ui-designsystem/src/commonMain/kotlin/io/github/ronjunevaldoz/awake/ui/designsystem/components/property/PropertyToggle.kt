package io.github.ronjunevaldoz.awake.ui.designsystem.components.property

import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.unstyled.input.toggle.toggle
import io.github.ronjunevaldoz.awake.ui.width

fun UiScope.propertyToggle(
    id: String,
    checked: Boolean,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    labelContent: BoxScope.(slot: UiSlot) -> Unit
): Boolean {
    var resolved = checked
    propertyRow(
        modifier = modifier.height(28f.dp),
        labelContent = labelContent
    ) { slot ->
        resolved = toggle(
            id = id,
            checked = checked,
            modifier = UiModifier().width(slot.width.px).height(slot.height.px),
            style = style
        )
    }
    return resolved
}


fun ColumnScope.propertyToggle(
    id: String,
    label: String,
    checked: Boolean,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = propertyToggle(
    id = id,
    checked = checked,
    modifier = modifier,
    style = style
) {
    text(label)
}
