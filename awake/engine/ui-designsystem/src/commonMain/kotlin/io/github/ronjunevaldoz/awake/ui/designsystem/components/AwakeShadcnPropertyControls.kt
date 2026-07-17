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
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.propertyRow
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.width

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
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty,
    labelContent: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
): Boolean {
    var resolved = checked
    propertyRow(
        modifier = modifier.height(36f.px),
        labelContent = labelContent
    ) { slot ->
        resolved = awakeShadcnToggle(
            id = id,
            checked = checked,
            modifier = UiModifier().width(slot.width.px).height(slot.height.px),
            style = style
        )
    }
    return resolved
}

fun UiColumnDslScope.awakeShadcnPropertyToggle(
    id: String,
    checked: Boolean,
    height: Dp,
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
            modifier = modifier.width(slot.width.px).height(slot.height.px),
            style = style
        )
    }
    return resolved
}

fun UiColumnDslScope.awakeShadcnPropertyToggle(
    id: String,
    label: String,
    checked: Boolean,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = awakeShadcnPropertyToggle(
    id = id,
    checked = checked,
    modifier = modifier,
    style = style
) {
    awakeShadcnPropertyLabel(label)
}

fun UiColumnDslScope.awakeShadcnPropertyDropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = UiModifier(),
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty,
    labelContent: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
): Int? {
    var resolved: Int? = null
    propertyRow(
        modifier = modifier.height(36f.px),
        labelWidth = labelWidth,
        labelContent = labelContent
    ) { slot ->
        resolved = awakeShadcnDropdown(
            id = id,
            options = options,
            selectedIndex = selectedIndex,
            modifier = UiModifier().width(slot.width.px).height(slot.height.px),
            style = style
        )
    }
    return resolved
}

fun UiColumnDslScope.awakeShadcnPropertyDropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    height: Dp,
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
            modifier = UiModifier().width(slot.width.px).height(slot.height.px),
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
    modifier: UiModifier = UiModifier(),
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty
): Int? = awakeShadcnPropertyDropdown(
    id = id,
    options = options,
    selectedIndex = selectedIndex,
    modifier = modifier,
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
    modifier: UiModifier = UiModifier(),
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty,
    labelContent: UiAbsoluteDslScope.(slot: UiSlot) -> Unit
): Float {
    var resolved = value
    propertyRow(
        modifier = modifier.height(36f.px),
        labelWidth = labelWidth,
        labelContent = labelContent
    ) { slot ->
        resolved = awakeShadcnSlider(
            id = id,
            min = min,
            max = max,
            value = value,
            modifier = UiModifier().width(slot.width.px).height(slot.height.px),
            style = style
        )
    }
    return resolved
}

fun UiColumnDslScope.awakeShadcnPropertySlider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    height: Dp,
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
            modifier = UiModifier().width(slot.width.px).height(slot.height.px),
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
    modifier: UiModifier = UiModifier(),
    labelWidth: Dp = 64f.dp,
    style: Style = Style.Empty
): Float = awakeShadcnPropertySlider(
    id = id,
    min = min,
    max = max,
    value = value,
    modifier = modifier,
    labelWidth = labelWidth,
    style = style
) {
    awakeShadcnPropertyLabel(label)
}

