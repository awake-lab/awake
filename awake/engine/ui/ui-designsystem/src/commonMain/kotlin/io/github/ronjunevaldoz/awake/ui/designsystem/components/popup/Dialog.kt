package io.github.ronjunevaldoz.awake.ui.designsystem.components.popup

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.frameBounds
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.rawSurface
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.styleable
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.popup
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.layout.toDimension
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*


private val DetachedPopupAnchor = UiSlot(-1f, -1f, 0f, 0f)
private val DefaultDialogScrimColor = Color.Black.withAlpha(0.48f)

fun UiScope.shadcnDialog(
    id: String,
    expanded: Boolean,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    properties: UiDialogProperties = UiDialogProperties(),
    header: (ColumnScope.(slot: UiSlot) -> Unit)? = null,
    actions: (RowScope.(slot: UiSlot) -> Unit)? = null,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiPopupResult {
    if (!expanded) return UiPopupResult(slot = null, dismissed = false)

    val frameBounds = frameBounds()
    if (properties.showScrim) {
        context.createAbsolute(
            x = frameBounds.x,
            y = frameBounds.y,
            overlayOnly = true
        ).emit(
            UiDrawPrimitive.Quad(
                frameBounds.x,
                frameBounds.y,
                frameBounds.width,
                frameBounds.height,
                properties.scrimColor ?: DefaultDialogScrimColor
            )
        )
    }

    val popupResult = popup(
        anchorSlot = DetachedPopupAnchor,
        expanded = true,
        width = width,
        height = height,
        verticalArrangement = Arrangement.spacedBy(0f.dp),
        positionProvider = UiPopupDefaults.centered(),
        properties = properties.popupProperties.copy(
            dismissOnClickOutside = properties.dismissOnClickOutside && properties.popupProperties.dismissOnClickOutside
        )
    ) { _ ->
        rawSurface(
            id = id,
            modifier = Modifier
                .width(width)
                .height(height)
                .styleable(theme.components.surface then Style { shape(UiShape.md) } then properties.surfaceStyle),
            clipContent = true
        ) { slot ->
            header?.invoke(this, slot)
            content(slot)
            if (actions != null) {
                row( modifier = Modifier.copy(widthDimension = Dimension.FillMax, heightDimension = 36f.dp.toDimension())) { actionSlot ->
                    actions(actionSlot)
                }
            }
        }
    }
    return popupResult
}
