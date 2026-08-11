// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.controls

import io.github.ronjunevaldoz.awake.ui.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popoverStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDropdownMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnDropdownMenu
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.headless.buttonSlot
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.rememberPopupState
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.drawDropdownTriggerContent
import io.github.ronjunevaldoz.awake.ui.withGraphicsLayerAlpha

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
    placeholder: String = "",
    enabled: Boolean = true,
): Int? {
    val popupState = rememberPopupState(id, key = "expanded")
    // shadcnFieldStyle(theme, style) resolves to ShadcnStyles.field's Default variant, which is
    // shared with the real Input (shadcnInput) -- Input and SelectTrigger disagree on vertical
    // padding in real shadcn (py-1 vs py-2), so this re-pins contentPadding to the
    // trigger-specific value after the shared base style, the same "later rule wins" compose
    // pattern used throughout this module (see skills/awake-shadcn-styling/SKILL.md).
    val shadcnResolvedTheme = theme.asShadcnTheme()
    val triggerStyle = shadcnFieldStyle(theme, style) then Style {
        contentPadding(
            shadcnResolvedTheme.metrics.fieldPaddingX,
            shadcnResolvedTheme.metrics.fieldPaddingY,
        )
    }
    val trigger = buttonSlot(
        id = "$id.trigger",
        modifier = modifier.height(36f.dp),
        style = triggerStyle,
        enabled = enabled,
    ) { }
    if (trigger.clicked) {
        popupState.toggle()
    }
    val hasSelection = selectedIndex != null && options.getOrNull(selectedIndex) != null
    val selectedLabel = if (hasSelection) options[selectedIndex!!] else placeholder
    // buttonSlot's own disabled-dim already covers the trigger's fill/border (see
    // buttonSlotInternal); the label/chevron paint on top of that fill separately below, so
    // they need their own matching group-alpha rather than sharing buttonSlot's (that would
    // compound into a double dim).
    withGraphicsLayerAlpha(if (enabled) 1f else 0.5f) {
        drawDropdownTriggerContent(
            slot = trigger.slot,
            label = selectedLabel,
            expanded = popupState.expanded,
            style = triggerStyle,
            semanticId = "$id.label",
            isPlaceholder = !hasSelection,
        )
    }
    recordSemantic(
        role = UiSemanticRole.Dropdown,
        id = id,
        label = selectedLabel,
        bounds = trigger.slot,
        selected = popupState.expanded,
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
        },
    )
    if (result.dismissed || result.selectedIndex != null) {
        popupState.close()
    }
    return result.selectedIndex
}

/** Generic-`T` shape matching shadcn-compose's `ShadcnSelect<T>` (callback-driven, not
 * return-value -- the reference always resolves a picked [T] through [onValueChange], never a
 * raw index). Wraps the [List]<String>/`Int?` overload above rather than duplicating the
 * trigger/popup wiring: maps [value] -> index via [options].indexOf on the way in, and the
 * resulting index -> [T] via [options].getOrNull on the way out. */
fun <T> UiScope.shadcnSelect(
    id: String,
    value: T?,
    options: List<T>,
    onValueChange: (T) -> Unit,
    label: (T) -> String = { it.toString() },
    placeholder: String = "",
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
) {
    val selectedIndex = value?.let { options.indexOf(it).takeIf { i -> i >= 0 } }
    val resultIndex = shadcnSelect(
        id = id,
        options = options.map(label),
        selectedIndex = selectedIndex,
        modifier = modifier,
        style = style,
        placeholder = placeholder,
        enabled = enabled,
    )
    resultIndex?.let { options.getOrNull(it)?.let(onValueChange) }
}
