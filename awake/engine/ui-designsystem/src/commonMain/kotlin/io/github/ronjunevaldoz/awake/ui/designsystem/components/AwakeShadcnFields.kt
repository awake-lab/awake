// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.UiDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.UiDslScope
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.checkbox
import io.github.ronjunevaldoz.awake.ui.dropdown
import io.github.ronjunevaldoz.awake.ui.dropdownMenu
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.rememberPopupState
import io.github.ronjunevaldoz.awake.ui.slider
import io.github.ronjunevaldoz.awake.ui.textField
import io.github.ronjunevaldoz.awake.ui.toggle
import io.github.ronjunevaldoz.awake.ui.UiTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnStyles
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.asAwakeShadcnTheme

private fun awakeShadcnFieldStyle(theme: UiTheme, style: Style): Style =
    AwakeShadcnStyles.field(theme.asAwakeShadcnTheme()) then style

private fun awakeShadcnCheckboxStyle(theme: UiTheme, style: Style): Style =
    AwakeShadcnStyles.checkbox(theme.asAwakeShadcnTheme()) then style

private fun awakeShadcnSliderStyle(theme: UiTheme, style: Style): Style =
    AwakeShadcnStyles.slider(theme.asAwakeShadcnTheme()) then style

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
    style = awakeShadcnFieldStyle(theme, style)
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
    style = awakeShadcnCheckboxStyle(theme, style)
)

fun UiScope.awakeShadcnTextField(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): String = textField(
    id = id,
    value = value,
    placeholder = placeholder,
    modifier = modifier,
    style = awakeShadcnFieldStyle(theme, style)
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
    style = awakeShadcnFieldStyle(theme, style)
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
    style = awakeShadcnSliderStyle(theme, style)
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
    style = awakeShadcnFieldStyle(theme, style)
)

fun UiDslScope.awakeShadcnTextField(
    id: String,
    value: String,
    placeholder: String = "",
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): String = textField(
    id = id,
    value = value,
    placeholder = placeholder,
    modifier = modifier,
    style = awakeShadcnFieldStyle(theme, style)
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
    style = awakeShadcnCheckboxStyle(theme, style)
)

fun UiDslScope.awakeShadcnDropdown(
    id: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = UiModifier(),
    style: Style = Style.Empty
): Int? {
    val popupState = context.rememberPopupState(id, key = "expanded")
    val triggerStyle = awakeShadcnFieldStyle(theme, style)
    val trigger = buttonSlot(
        id = "$id.trigger",
        modifier = modifier.height(36f.px),
        style = triggerStyle
    ) { }
    if (trigger.clicked) {
        popupState.toggle()
    }
    val selectedLabel = options.getOrNull(selectedIndex) ?: ""
    drawDropdownTriggerContent(
        slot = trigger.slot,
        label = selectedLabel,
        expanded = popupState.expanded,
        style = triggerStyle,
        semanticId = "$id.label"
    )
    recordSemantic(
        role = UiSemanticRole.Dropdown,
        id = id,
        label = selectedLabel,
        bounds = trigger.slot,
        selected = popupState.expanded
    )

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
        }
    )
    if (result.dismissed || result.selectedIndex != null) {
        popupState.close()
    }
    return result.selectedIndex
}

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
    style = awakeShadcnSliderStyle(theme, style)
)
