package io.github.ronjunevaldoz.awake.ui.designsystem.components.popup

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.ext.rawSurface
import io.github.ronjunevaldoz.awake.ui.layouts.ext.row
import io.github.ronjunevaldoz.awake.ui.popup


private val DetachedPopupAnchor = UiSlot(-1f, -1f, 0f, 0f)
private val DefaultDialogScrimColor = Color.Black.withAlpha(0.48f)

fun UiScope.dialog(
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

    val frameBounds = context.frameBounds()
    if (properties.showScrim) {
        context.createAbsolute(
            x = frameBounds.x,
            y = frameBounds.y,
            font = font,
            theme = theme,
            textScale = textScale,
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
        gap = 0f,
        positionProvider = UiPopupDefaults.centered(),
        properties = properties.popupProperties.copy(
            dismissOnClickOutside = properties.dismissOnClickOutside && properties.popupProperties.dismissOnClickOutside
        )
    ) { _ ->
        rawSurface(
            id = id,
            width = width,
            height = height,
            radius = UiShape.md,
            style = theme.components.surface then properties.surfaceStyle,
            clipContent = true
        ) { slot ->
            header?.invoke(this, slot)
            content(slot)
            if (actions != null) {
                row(height = 36f.dp, width = Dimension.FillMax) { actionSlot ->
                    actions(actionSlot)
                }
            }
        }
    }
    return popupResult
}