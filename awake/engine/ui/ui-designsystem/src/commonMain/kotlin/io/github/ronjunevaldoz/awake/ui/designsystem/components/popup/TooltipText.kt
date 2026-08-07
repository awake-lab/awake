// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.popup

import io.github.ronjunevaldoz.awake.ui.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.headless.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.headless.input.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*
import io.github.ronjunevaldoz.awake.ui.theme

fun UiScope.shadcnTooltipText(
    anchorSlot: UiBounds,
    visible: Boolean,
    text: String,
    positionProvider: UiPopupPositionProvider = UiPopupDefaults.aligned(
        anchorAlignment = UiAlignment.BottomCenter,
        popupAlignment = UiAlignment.TopCenter,
        offsetY = theme.asShadcnTheme().spacing.xs,
    ),
    properties: UiPopupProperties = UiPopupProperties(),
    style: Style = Style.Empty,
    id: String = "tooltip",
): UiPopupResult = shadcnTooltip(
    anchorSlot = anchorSlot,
    visible = visible,
    positionProvider = positionProvider,
    properties = properties,
    style = style,
    id = id,
) {
    text(
        label = text,
        wrap = UiTextWrap.Word,
        overflow = UiTextOverflow.Ellipsis,
    )
}
