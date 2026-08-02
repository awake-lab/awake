// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.controls

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popoverStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnDropdownMenu
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.recordSemantic
import io.github.ronjunevaldoz.awake.ui.rememberPopupState
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.buttonSlot
import io.github.ronjunevaldoz.awake.ui.unstyled.input.drawDropdownTriggerContent
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/** Real shadcn's `Select`: a trigger button that opens a [shadcnDropdownMenu] of [options].
 * Owns trigger rendering + popup open/close state; the dropdown itself is composed rather
 * than a bespoke popup. */
fun UiScope.shadcnSelect(
    id: String,
    options: List<String>,
    // Nullable: real shadcn's Select explicitly supports "nothing chosen yet, show
    // placeholder" -- forcing an initial index committed a value the caller never picked.
    selectedIndex: Int?,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    placeholder: String = ""
): Int? {
    val popupState = rememberPopupState(id, key = "expanded")
    val triggerStyle = shadcnFieldStyle(theme, style)
    val trigger = buttonSlot(
        id = "$id.trigger",
        modifier = modifier.height(40f.dp),
        style = triggerStyle
    ) { }
    if (trigger.clicked) {
        popupState.toggle()
    }
    val hasSelection = selectedIndex != null && options.getOrNull(selectedIndex) != null
    val selectedLabel = if (hasSelection) options[selectedIndex!!] else placeholder
    drawDropdownTriggerContent(
        slot = trigger.slot,
        label = selectedLabel,
        expanded = popupState.expanded,
        style = triggerStyle,
        semanticId = "$id.label",
        isPlaceholder = !hasSelection
    )
    recordSemantic(
        role = UiSemanticRole.Dropdown,
        id = id,
        label = selectedLabel,
        bounds = trigger.slot,
        selected = popupState.expanded
    )

    val result = shadcnDropdownMenu(
        id = "$id.dropdown",
        anchorSlot = trigger.slot,
        expanded = popupState.expanded,
        items = options.map { UiDropdownMenuItem(label = it) },
        selectedIndex = selectedIndex,
        width = Dimension.Fixed(trigger.slot.width.px),
        itemHeight = 32f,
        positionProvider = UiPopupDefaults.dropdown(offsetY = 4f.dp),
        style = popoverStyle(theme.asShadcnTheme()) then Style {
            contentPadding(4f.dp)
        }
    )
    if (result.dismissed || result.selectedIndex != null) {
        popupState.close()
    }
    return result.selectedIndex
}
