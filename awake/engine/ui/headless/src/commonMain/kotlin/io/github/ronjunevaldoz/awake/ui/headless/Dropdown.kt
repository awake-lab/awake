// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.unstyled.input.drawDropdownTriggerContent
import io.github.ronjunevaldoz.awake.ui.unstyled.components.contextMenuTrigger as primitiveContextMenuTrigger
import io.github.ronjunevaldoz.awake.ui.unstyled.components.filterOptionsByQuery as primitiveFilterOptionsByQuery
import io.github.ronjunevaldoz.awake.ui.unstyled.input.select as primitiveSelect

/** Generic select behavior with a stable Headless receiver. */
fun UiScope.select(
    id: String,
    options: List<String>,
    selectedIndex: Int? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "",
    visuals: SurfaceVisuals = SurfaceVisuals(),
    selectedVisuals: SurfaceStyle? = null,
): Int? = primitive.primitiveSelect(
    id = id,
    options = options,
    selectedIndex = selectedIndex ?: -1,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    selectedStyle = selectedVisuals?.asPrimitiveStyle(),
    enabled = enabled,
    placeholder = placeholder,
)

fun ColumnScope.select(
    id: String,
    options: List<String>,
    selectedIndex: Int? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "",
    visuals: SurfaceVisuals = SurfaceVisuals(),
    selectedVisuals: SurfaceStyle? = null,
): Int? = primitive.primitiveSelect(
    id = id,
    options = options,
    selectedIndex = selectedIndex ?: -1,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    selectedStyle = selectedVisuals?.asPrimitiveStyle(),
    enabled = enabled,
    placeholder = placeholder,
)

fun RowScope.select(
    id: String,
    options: List<String>,
    selectedIndex: Int? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "",
    visuals: SurfaceVisuals = SurfaceVisuals(),
    selectedVisuals: SurfaceStyle? = null,
): Int? = primitive.primitiveSelect(
    id = id,
    options = options,
    selectedIndex = selectedIndex ?: -1,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    selectedStyle = selectedVisuals?.asPrimitiveStyle(),
    enabled = enabled,
    placeholder = placeholder,
)

/** Generic searchable combobox behavior; visual policy is supplied by the calling skin. */
fun UiScope.combobox(
    id: String,
    options: List<String>,
    selectedIndex: Int? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "",
    filterPlaceholder: String = "Search...",
    emptyLabel: String = "No results found.",
    visuals: SurfaceVisuals = SurfaceVisuals(),
    selectedVisuals: SurfaceStyle? = null,
    optionVisuals: SurfaceVisuals = SurfaceVisuals(),
    filterVisuals: SurfaceVisuals = SurfaceVisuals(),
): Int? {
    val popupState = rememberPopupState(id, key = "expanded")
    val filterState = rememberStateValue(id, key = "filter") { "" }
    val selectedLabel = options.getOrNull(selectedIndex ?: -1) ?: placeholder
    val trigger = buttonSlot(
        id = "$id.trigger",
        modifier = modifier,
        visuals = visuals,
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
        style = visuals.asPrimitiveStyle(),
        semanticId = id,
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
            visuals = filterVisuals,
            enabled = enabled,
        )
        filterState.value = query
        separator()
        val filtered = filterOptionsByQuery(options, query)
        if (filtered.isEmpty()) {
            text(emptyLabel, modifier = Modifier.fillMaxWidth(), visuals = optionVisuals.rest)
        } else {
            filtered.forEach { indexed ->
                val rowVisuals = if (indexed.index == selectedIndex && selectedVisuals != null) {
                    optionVisuals.copy(rest = selectedVisuals)
                } else {
                    optionVisuals
                }
                if (
                    button(
                        id = "$id.option.${indexed.index}",
                        label = indexed.value,
                        modifier = Modifier.fillMaxWidth().height(32f.dp),
                        visuals = rowVisuals,
                        enabled = enabled,
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

/** Neutral searchable option filtering, preserving each option's source index for selection. */
fun filterOptionsByQuery(options: List<String>, query: String): List<IndexedValue<String>> =
    primitiveFilterOptionsByQuery(options, query)

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
