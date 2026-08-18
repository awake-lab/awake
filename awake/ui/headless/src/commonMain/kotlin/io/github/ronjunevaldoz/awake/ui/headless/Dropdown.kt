// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.drawDropdownTriggerContent
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.contextMenuTrigger as primitiveContextMenuTrigger
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.filterOptionsByQuery as primitiveFilterOptionsByQuery
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.select as primitiveSelect
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.buttonSlot as primitiveButtonSlot

/** Generic select behavior with a stable Headless receiver. */
fun UiScope.select(
    id: String,
    options: List<String>,
    selectedIndex: Int? = null,
    modifier: UiModifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "",
    style: Style = Style.Empty,
    selectedStyle: Style? = null,
    optionStyle: Style? = null,
): Int? = primitive.primitiveSelect(
    id = id,
    options = options,
    selectedIndex = selectedIndex ?: -1,
    modifier = modifier,
    style = style,
    selectedStyle = selectedStyle,
    optionStyle = optionStyle,
    enabled = enabled,
    placeholder = placeholder,
)

/** Generic searchable combobox behavior; visual policy is supplied by the calling skin. */
fun UiScope.combobox(
    id: String,
    options: List<String>,
    selectedIndex: Int? = null,
    modifier: UiModifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "",
    filterPlaceholder: String = "Search...",
    emptyLabel: String = "No results found.",
    style: Style = Style.Empty,
    selectedStyle: Style? = null,
    optionStyle: Style = Style.Empty,
    filterStyle: Style = Style.Empty,
): Int? {
    val popupState = rememberPopupState(id, key = "expanded")
    val filterState = rememberStateValue(id, key = "filter") { "" }
    val selectedLabel = options.getOrNull(selectedIndex ?: -1) ?: placeholder
    val trigger = primitive.primitiveButtonSlot(
        id = "$id.trigger",
        modifier = modifier,
        style = style,
        enabled = enabled,
    )
    if (trigger.clicked) {
        val opening = !popupState.expanded
        popupState.toggle()
        if (opening) filterState.value = ""
    }
    primitive.drawDropdownTriggerContent(
        slot = trigger.slot,
        label = selectedLabel,
        expanded = popupState.expanded,
        style = style,
        // The Dropdown node below owns `id`; the label is a separate text node. Passing `id`
        // here made both claim it, which is a duplicate semantic id. primitiveSelect has
        // always used the `.label` suffix -- this widget just never matched it.
        semanticId = "$id.label",
        isPlaceholder = selectedIndex == null,
    )
    primitive.recordSemantic(
        role = UiSemanticRole.Dropdown,
        id = id,
        label = selectedLabel,
        bounds = trigger.slot,
        selected = popupState.expanded,
    )

    var picked: Int? = null
    val popupResult = popup(
        id = id,
        anchorSlot = trigger.slot,
        expanded = popupState.expanded,
        width = Dimension.Fixed(trigger.slot.width.px),
        height = Dimension.WrapContent,
        positionProvider = UiPopupDefaults.dropdown(offsetY = 4f.dp),
    ) {
        val query = textField(
            id = "$id.filter",
            value = filterState.value,
            placeholder = filterPlaceholder,
            modifier = Modifier.fillMaxWidth().height(36f.dp),
            style = filterStyle,
            enabled = enabled,
        )
        filterState.value = query
        separator()
        val filtered = primitiveFilterOptionsByQuery(options, query)
        if (filtered.isEmpty()) {
            text(emptyLabel, modifier = Modifier.fillMaxWidth(), style = optionStyle)
        } else {
            filtered.forEach { indexed ->
                val rowStyle = if (indexed.index == selectedIndex) selectedStyle ?: optionStyle else optionStyle
                if (
                    button(
                        id = "$id.option.${indexed.index}",
                        label = indexed.value,
                        modifier = Modifier.fillMaxWidth().height(32f.dp),
                        style = rowStyle,
                        enabled = enabled,
                        semanticRole = UiSemanticRole.MenuItem,
                    )
                ) {
                    picked = indexed.index
                }
            }
        }
    }
    if (popupResult.dismissed || picked != null) {
        popupState.close()
        filterState.value = ""
    }
    return picked
}

/** Secondary-click detection and cursor anchoring without exposing Core's trigger type. */
fun UiScope.contextMenuTrigger(
    id: String,
    expanded: Boolean,
    target: UiBounds,
): HeadlessContextMenuTrigger {
    val trigger = primitive.primitiveContextMenuTrigger(id, expanded, target)
    return HeadlessContextMenuTrigger(trigger.shouldOpen, trigger.anchor)
}

data class HeadlessContextMenuTrigger(
    val shouldOpen: Boolean,
    val anchor: UiBounds,
)
