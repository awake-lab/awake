// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.popup

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.styling.Style
import io.github.ronjunevaldoz.awake.ui.UiPopupProperties

data class UiDialogProperties(
    val dismissOnClickOutside: Boolean = true,
    val showScrim: Boolean = true,
    val scrimColor: Color? = null,
    val popupProperties: UiPopupProperties = UiPopupProperties(),
    val surfaceStyle: Style = Style.Empty
)
