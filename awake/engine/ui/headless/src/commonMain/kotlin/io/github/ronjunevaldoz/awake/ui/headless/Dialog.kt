// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.api.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds

/** Neutral behavior and visual inputs for a modal [dialog]. */
data class DialogProperties(
    val dismissOnClickOutside: Boolean = true,
    val showScrim: Boolean = false,
    val scrimColor: Color? = null,
    val popupProperties: UiPopupProperties = UiPopupProperties(),
    val surface: SurfaceStyle = SurfaceStyle(),
)

/**
 * Generic modal popup behavior with optional caller-provided scrim and neutral surface values.
 *
 * Placement, dismissal, clipping, and overlay ordering are reusable Headless behavior. A design
 * system supplies its branded colors, shapes, and defaults through [DialogProperties].
 */
fun UiScope.dialog(
    id: String,
    expanded: Boolean,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    properties: DialogProperties = DialogProperties(),
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiPopupResult {
    if (!expanded) return UiPopupResult(slot = null, dismissed = false)

    if (properties.showScrim) {
        properties.scrimColor?.let { color -> overlayScrim(frameBounds(), color) }
    }

    return popup(
        id = id,
        anchorSlot = DetachedDialogAnchor,
        expanded = true,
        width = width,
        height = height,
        positionProvider = UiPopupDefaults.centered(),
        properties = properties.popupProperties.copy(
            dismissOnClickOutside = properties.dismissOnClickOutside &&
                properties.popupProperties.dismissOnClickOutside,
        ),
    ) {
        surface(
            id = id,
            modifier = if (height == Dimension.WrapContent) Modifier else Modifier.fillMaxHeight(),
            style = properties.surface,
            clipContent = true,
            content = content,
        )
    }
}

private val DetachedDialogAnchor = UiBounds(-1f, -1f, 0f, 0f)
