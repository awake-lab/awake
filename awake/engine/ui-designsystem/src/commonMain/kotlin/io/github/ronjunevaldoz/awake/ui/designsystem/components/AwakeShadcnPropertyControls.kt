// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiAbsoluteDslScope
import io.github.ronjunevaldoz.awake.ui.UiColumnDslScope
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.propertyRow

private fun UiAbsoluteDslScope.awakeShadcnPropertyLabel(label: String) {
    text(
        label = label,
        style = Style {
            val shadcnTheme = theme.asAwakeShadcnTheme()
            foreground(shadcnTheme.tokens.mutedForeground)
            textSize(shadcnTheme.typography.caption)
        }
    )
}

fun UiColumnDslScope.awakeShadcnPropertyToggle(
    id: String,
    checked: Boolean,
    height: Float = 36f,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    labelContent: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
): Boolean {
    var resolved = checked
    propertyRow(
        height = height,
        labelContent = labelContent
    ) { slot ->
        resolved = awakeShadcnToggle(
            id = id,
            checked = checked,
            width = slot.width,
            height = slot.height,
            modifier = modifier,
            style = style
        )
    }
    return resolved
}

fun UiColumnDslScope.awakeShadcnPropertyToggle(
    id: String,
    label: String,
    checked: Boolean,
    height: Float = 36f,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = awakeShadcnPropertyToggle(
    id = id,
    checked = checked,
    height = height,
    modifier = modifier,
    style = style
) {
    awakeShadcnPropertyLabel(label)
}

fun UiColumnDslScope.awakeShadcnPropertyDropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    height: Float = 36f,
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty,
    labelContent: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
): Int? {
    var resolved: Int? = null
    propertyRow(
        height = height,
        labelWidth = labelWidth,
        labelContent = labelContent
    ) { slot ->
        resolved = awakeShadcnDropdown(
            id = id,
            options = options,
            selectedIndex = selectedIndex,
            width = slot.width,
            height = slot.height,
            style = style
        )
    }
    return resolved
}

fun UiColumnDslScope.awakeShadcnPropertyDropdown(
    id: String,
    label: String,
    options: List<String>,
    selectedIndex: Int,
    height: Float = 36f,
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty
): Int? = awakeShadcnPropertyDropdown(
    id = id,
    options = options,
    selectedIndex = selectedIndex,
    height = height,
    labelWidth = labelWidth,
    style = style
) {
    awakeShadcnPropertyLabel(label)
}

fun UiColumnDslScope.awakeShadcnPropertySlider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    height: Float = 36f,
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty,
    labelContent: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
): Float {
    var resolved = value
    propertyRow(
        height = height,
        labelWidth = labelWidth,
        labelContent = labelContent
    ) { slot ->
        resolved = awakeShadcnSlider(
            id = id,
            min = min,
            max = max,
            value = value,
            width = slot.width,
            height = slot.height,
            style = style
        )
    }
    return resolved
}

fun UiColumnDslScope.awakeShadcnPropertySlider(
    id: String,
    label: String,
    min: Float,
    max: Float,
    value: Float,
    height: Float = 36f,
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty
): Float = awakeShadcnPropertySlider(
    id = id,
    min = min,
    max = max,
    value = value,
    height = height,
    labelWidth = labelWidth,
    style = style
) {
    awakeShadcnPropertyLabel(label)
}
