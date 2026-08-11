// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.popup

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.headless.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.api.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.styleable
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.popup
import io.github.ronjunevaldoz.awake.ui.scope.frameBounds
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.unstyled.overlayScrim

private val DetachedPopupAnchor = UiBounds(-1f, -1f, 0f, 0f)
private val DefaultDialogScrimColor = Color.Black.withAlpha(0.48f)

fun UiScope.shadcnDialog(
    id: String,
    expanded: Boolean,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    properties: UiDialogProperties = UiDialogProperties(),
    showCloseButton: Boolean = false,
    closeIcon: (BoxScope.() -> Unit)? = null,
    header: (ColumnScope.(slot: UiBounds) -> Unit)? = null,
    actions: (RowScope.(slot: UiBounds) -> Unit)? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiPopupResult {
    if (!expanded) return UiPopupResult(slot = null, dismissed = false)

    var closeClicked = false

    val frameBounds = frameBounds()
    if (properties.showScrim) {
        overlayScrim(frameBounds, properties.scrimColor ?: DefaultDialogScrimColor)
    }

    val popupResult = popup(
        id = id,
        anchorSlot = DetachedPopupAnchor,
        expanded = true,
        width = width,
        height = height,
        verticalArrangement = Arrangement.spacedBy(0f.dp),
        positionProvider = UiPopupDefaults.centered(),
        properties = properties.popupProperties.copy(
            dismissOnClickOutside = properties.dismissOnClickOutside && properties.popupProperties.dismissOnClickOutside,
        ),
    ) { _ ->
        surface(
            id = id,
            modifier = Modifier
                .width(width)
                .height(height)
                // Real shadcn's dialog content is rounded-lg, drawn from the active theme's own
                // radius scale rather than ui-core's disconnected `UiShape` global.
                .styleable(theme.components.surface then Style { shape(theme.asShadcnTheme().radii.lg) } then properties.surfaceStyle),
            clipContent = true,
        ) { slot ->
            val boundsSlot = slot
            if (showCloseButton) {
                row(
                    modifier = Modifier.width(Dimension.FillMax),
                    horizontalArrangement = Arrangement.End,
                ) {
                    shadcnButton(
                        id = "$id.close",
                        modifier = Modifier.width(24f.dp).height(24f.dp),
                        variant = ShadcnButtonVariant.Ghost,
                        size = ShadcnButtonSize.Xs,
                    ) {
                        if (closeIcon != null) closeIcon() else text("x")
                    }.let { clicked -> if (clicked) closeClicked = true }
                }
            }
            header?.invoke(this, boundsSlot)
            content(boundsSlot)
            if (actions != null) {
                row(
                    modifier = Modifier.width(Dimension.FillMax).height(36f.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) { actionSlot ->
                    actions(actionSlot)
                }
            }
        }
    }
    return if (closeClicked) popupResult.copy(dismissed = true) else popupResult
}
