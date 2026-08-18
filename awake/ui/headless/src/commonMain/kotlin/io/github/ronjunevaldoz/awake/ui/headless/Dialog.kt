// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.api.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.style.Style

/** Neutral behavior inputs for a modal [dialog]. Visual style is [dialog]'s own `style` param --
 * not a second Style-typed field here, see skills/awake-ui-authoring's one-style-channel rule. */
data class DialogProperties(
    val dismissOnClickOutside: Boolean = true,
    val showScrim: Boolean = false,
    val scrimColor: Color? = null,
    val popupProperties: UiPopupProperties = UiPopupProperties(),
)

/** Result/action contracts for a branded alert-dialog recipe. */
data class UiAlertDialogResult(
    val popup: UiPopupResult,
    val action: UiAlertDialogAction?,
)

enum class UiAlertDialogAction {
    Confirm,
    Dismiss,
}

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
    style: Style = Style.Empty,
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
            modifier = Modifier,
            style = style,
            content = content,
        )
    }
}

private val DetachedDialogAnchor = UiBounds(-1f, -1f, 0f, 0f)
