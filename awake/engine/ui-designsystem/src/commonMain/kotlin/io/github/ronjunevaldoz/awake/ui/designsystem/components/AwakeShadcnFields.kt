// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.UiDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.UiDslScope
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.buttonSlot
import io.github.ronjunevaldoz.awake.ui.checkbox
import io.github.ronjunevaldoz.awake.ui.dropdown
import io.github.ronjunevaldoz.awake.ui.dropdownMenu
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.rememberPopupState
import io.github.ronjunevaldoz.awake.ui.slider
import io.github.ronjunevaldoz.awake.ui.toggle
import io.github.ronjunevaldoz.awake.ui.width

fun UiScope.awakeShadcnToggle(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = toggle(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier,
    style = AwakeShadcnStyles.field(theme.asAwakeShadcnTheme()) then style
)

fun UiScope.awakeShadcnToggle(
    id: String,
    checked: Boolean,
    width: Float,
    height: Float = 32f,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = toggle(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier.width(width.px).height(height.px),
    style = AwakeShadcnStyles.field(theme.asAwakeShadcnTheme()) then style
)

fun UiScope.awakeShadcnCheckbox(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = checkbox(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier,
    style = AwakeShadcnStyles.checkbox(theme.asAwakeShadcnTheme()) then style
)

fun UiScope.awakeShadcnCheckbox(
    id: String,
    checked: Boolean,
    width: Float,
    height: Float = 24f,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = checkbox(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier.width(width.px).height(height.px),
    style = AwakeShadcnStyles.checkbox(theme.asAwakeShadcnTheme()) then style
)

fun UiScope.awakeShadcnDropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Int? = dropdown(
    id = id,
    options = options,
    selectedIndex = selectedIndex,
    modifier = modifier,
    style = AwakeShadcnStyles.field(theme.asAwakeShadcnTheme()) then style
)

fun UiScope.awakeShadcnDropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    width: Float,
    height: Float = 36f,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Int? = dropdown(
    id = id,
    options = options,
    selectedIndex = selectedIndex,
    modifier = modifier.width(width.px).height(height.px),
    style = AwakeShadcnStyles.field(theme.asAwakeShadcnTheme()) then style
)

fun UiScope.awakeShadcnSlider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Float = slider(
    id = id,
    min = min,
    max = max,
    value = value,
    label = label,
    modifier = modifier,
    style = AwakeShadcnStyles.slider(theme.asAwakeShadcnTheme()) then style
)

fun UiScope.awakeShadcnSlider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    width: Float,
    height: Float = 36f,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Float = slider(
    id = id,
    min = min,
    max = max,
    value = value,
    label = label,
    modifier = modifier.width(width.px).height(height.px),
    style = AwakeShadcnStyles.slider(theme.asAwakeShadcnTheme()) then style
)

fun UiDslScope.awakeShadcnToggle(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = toggle(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier,
    style = AwakeShadcnStyles.field(theme.asAwakeShadcnTheme()) then style
)

fun UiDslScope.awakeShadcnToggle(
    id: String,
    checked: Boolean,
    width: Float,
    height: Float = 32f,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = toggle(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier.width(width.px).height(height.px),
    style = AwakeShadcnStyles.field(theme.asAwakeShadcnTheme()) then style
)

fun UiDslScope.awakeShadcnCheckbox(
    id: String,
    checked: Boolean,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = checkbox(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier,
    style = AwakeShadcnStyles.checkbox(theme.asAwakeShadcnTheme()) then style
)

fun UiDslScope.awakeShadcnCheckbox(
    id: String,
    checked: Boolean,
    width: Float,
    height: Float = 24f,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Boolean = checkbox(
    id = id,
    checked = checked,
    label = label,
    modifier = modifier.width(width.px).height(height.px),
    style = AwakeShadcnStyles.checkbox(theme.asAwakeShadcnTheme()) then style
)

fun UiDslScope.awakeShadcnDropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Int? {
    val popupState = context.rememberPopupState(id, key = "expanded")
    val triggerStyle = AwakeShadcnStyles.field(theme.asAwakeShadcnTheme()) then style
    val trigger = buttonSlot(
        id = id,
        label = "",
        modifier = modifier.height(36f.px),
        style = triggerStyle
    )
    if (trigger.clicked) {
        popupState.toggle()
    }
    drawDropdownTriggerContent(trigger.slot, options.getOrNull(selectedIndex) ?: "", popupState.expanded, triggerStyle)

    val result = dropdownMenu(
        id = "$id.dropdown",
        anchorSlot = trigger.slot,
        expanded = popupState.expanded,
        items = options.map { UiDropdownMenuItem(label = it) },
        selectedIndex = selectedIndex,
        width = Dimension.Fixed(trigger.slot.width.px),
        itemHeight = 32f,
        positionProvider = UiPopupDefaults.dropdown(offsetY = 4f.dp),
        style = AwakeShadcnStyles.surface(theme.asAwakeShadcnTheme(), AwakeShadcnSurfaceVariant.Popover) then Style {
            contentPadding(4f.dp)
        },
        itemStyle = Style {
            shape(theme.asAwakeShadcnTheme().radii.md)
        }
    )
    if (result.dismissed || result.selectedIndex != null) {
        popupState.close()
    }
    return result.selectedIndex
}

fun UiDslScope.awakeShadcnDropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    width: Float,
    height: Float = 36f,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Int? = awakeShadcnDropdown(
    id = id,
    options = options,
    selectedIndex = selectedIndex,
    modifier = modifier.width(width.px).height(height.px),
    style = style
)

fun UiDslScope.awakeShadcnSlider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Float = slider(
    id = id,
    min = min,
    max = max,
    value = value,
    label = label,
    modifier = modifier,
    style = AwakeShadcnStyles.slider(theme.asAwakeShadcnTheme()) then style
)

fun UiDslScope.awakeShadcnSlider(
    id: String,
    min: Float,
    max: Float,
    value: Float,
    width: Float,
    height: Float = 36f,
    label: String? = null,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Float = slider(
    id = id,
    min = min,
    max = max,
    value = value,
    label = label,
    modifier = modifier.width(width.px).height(height.px),
    style = AwakeShadcnStyles.slider(theme.asAwakeShadcnTheme()) then style
)
