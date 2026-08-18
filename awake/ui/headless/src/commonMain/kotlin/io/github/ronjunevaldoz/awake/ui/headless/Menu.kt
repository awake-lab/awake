// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.style.Style

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
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
): Boolean = button(
    id = item.id,
    label = label,
    modifier = modifier,
    style = style,
    enabled = item.enabled,
    semanticRole = UiSemanticRole.MenuItem,
)

/**
 * A menu outcome IS a popup outcome plus the index that was chosen, so it composes
 * [UiPopupResult] rather than re-declaring its fields -- the shape [UiAlertDialogResult] already
 * uses.
 *
 * [slot]/[dismissed] stay available directly so call sites read the same as before.
 */
data class UiMenuResult(
    val popup: UiPopupResult,
    val selectedIndex: Int?,
) {
    val slot: UiBounds? get() = popup.slot
    val dismissed: Boolean get() = popup.dismissed
}
