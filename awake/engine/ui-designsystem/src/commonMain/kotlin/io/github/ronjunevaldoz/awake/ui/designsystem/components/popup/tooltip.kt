package io.github.ronjunevaldoz.awake.ui.designsystem.components.popup

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.UiSpacing
import io.github.ronjunevaldoz.awake.ui.popup
import io.github.ronjunevaldoz.awake.ui.layouts.ext.surface

fun UiScope.tooltip(
    anchorSlot: UiSlot,
    visible: Boolean,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    positionProvider: UiPopupPositionProvider = UiPopupDefaults.aligned(
        anchorAlignment = UiAlignment.BottomCenter,
        popupAlignment = UiAlignment.TopCenter,
        offsetY = UiSpacing.xs
    ),
    properties: UiPopupProperties = UiPopupProperties(),
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiPopupResult = popup(
    anchorSlot = anchorSlot,
    expanded = visible,
    width = width,
    height = height,
    gap = 0f,
    positionProvider = positionProvider,
    properties = properties
) { _ ->
    surface(
        id = "tooltip",
        width = width,
        height = height,
        radius = UiShape.sm,
        style = theme.components.surface then style
    ) { slot ->
        content(slot)
    }
}