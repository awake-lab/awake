// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.api.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds

/** A neutral menu item contract. Visuals and item content belong to the caller's skin. */
data class UiMenuItem(
    val id: String,
    val index: Int,
    val enabled: Boolean = true,
) : UiMenuEntry

/** A separator is behavior-free and lets skins insert their own divider treatment. */
data object UiMenuSeparator : UiMenuEntry

sealed interface UiMenuEntry

/** Neutral interactive row primitive for menu skins. */
fun ColumnScope.menuItem(
    item: UiMenuItem,
    label: String,
    modifier: Modifier = Modifier,
    visuals: SurfaceVisuals = SurfaceVisuals(),
): Boolean = button(
    id = item.id,
    label = label,
    modifier = modifier,
    visuals = visuals,
    enabled = item.enabled,
)

data class UiMenuResult(
    val slot: UiBounds?,
    val selectedIndex: Int?,
    val dismissed: Boolean,
)

/**
 * Composes menu popup behavior without choosing a visual language.
 *
 * The renderer returns whether the item was activated. Headless owns popup placement, outside
 * dismissal, and selection bookkeeping; a design system owns item surfaces, typography, icons,
 * separators, and disabled/selected colors.
 */
fun UiScope.menu(
    id: String,
    anchorSlot: UiBounds,
    expanded: Boolean,
    entries: List<UiMenuEntry>,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    positionProvider: UiPopupPositionProvider = UiPopupDefaults.dropdown(),
    properties: UiPopupProperties = UiPopupProperties(),
    item: ColumnScope.(UiMenuItem) -> Boolean,
    separator: ColumnScope.() -> Unit = {},
): UiMenuResult {
    var selectedIndex: Int? = null
    val result = popup(
        id = id,
        anchorSlot = anchorSlot,
        expanded = expanded,
        width = width,
        height = height,
        positionProvider = positionProvider,
        properties = properties,
    ) {
        entries.forEach { entry ->
            when (entry) {
                UiMenuSeparator -> separator()
                is UiMenuItem -> if (entry.enabled && item(entry)) selectedIndex = entry.index
            }
        }
    }
    return UiMenuResult(result.slot, selectedIndex, result.dismissed)
}
