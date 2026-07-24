package io.github.ronjunevaldoz.awake.ui.designsystem.components.popup

import io.github.ronjunevaldoz.awake.ui.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.layouts.UiSpacing
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

fun UiScope.shadcnTooltipText(
    anchorSlot: UiBounds,
    visible: Boolean,
    text: String,
    positionProvider: UiPopupPositionProvider = UiPopupDefaults.aligned(
        anchorAlignment = UiAlignment.BottomCenter,
        popupAlignment = UiAlignment.TopCenter,
        offsetY = UiSpacing.xs
    ),
    properties: UiPopupProperties = UiPopupProperties(),
    style: Style = Style.Empty
): UiPopupResult = shadcnTooltip(
    anchorSlot = anchorSlot,
    visible = visible,
    positionProvider = positionProvider,
    properties = properties,
    style = style
) {
    text(
        label = text,
        wrap = UiTextWrap.Word,
        overflow = UiTextOverflow.Ellipsis
    )
}