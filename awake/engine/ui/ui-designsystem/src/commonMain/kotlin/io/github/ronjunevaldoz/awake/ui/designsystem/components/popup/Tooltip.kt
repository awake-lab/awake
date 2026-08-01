package io.github.ronjunevaldoz.awake.ui.designsystem.components.popup

import io.github.ronjunevaldoz.awake.ui.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.popup
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

fun UiScope.shadcnTooltip(
    anchorSlot: UiBounds,
    visible: Boolean,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    positionProvider: UiPopupPositionProvider = UiPopupDefaults.aligned(
        anchorAlignment = UiAlignment.BottomCenter,
        popupAlignment = UiAlignment.TopCenter,
        offsetY = theme.asShadcnTheme().spacing.xs
    ),
    properties: UiPopupProperties = UiPopupProperties(),
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiPopupResult = popup(
    anchorSlot = anchorSlot,
    expanded = visible,
    width = width,
    height = height,
    verticalArrangement = Arrangement.spacedBy(0f.dp),
    positionProvider = positionProvider,
    properties = properties
) { _ ->
    surface(
        id = "tooltip",
        style = Style { shape(UiShape.sm) } then theme.components.surface then style,
        modifier = Modifier.width(width).height(height)
    ) { slot ->
        content(slot.toBounds())
    }
}
