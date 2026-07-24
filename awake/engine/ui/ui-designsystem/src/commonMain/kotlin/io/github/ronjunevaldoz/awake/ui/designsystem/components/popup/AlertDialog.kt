// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.popup

import io.github.ronjunevaldoz.awake.ui.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.supportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

fun UiScope.shadcnAlertDialog(
    id: String,
    expanded: Boolean,
    title: String,
    message: String,
    width: Dimension = Dimension.Fixed(320f.dp),
    confirmLabel: String = "Confirm",
    dismissLabel: String? = "Cancel",
    confirmVariant: ShadcnButtonVariant = ShadcnButtonVariant.Primary,
    dismissVariant: ShadcnButtonVariant = ShadcnButtonVariant.Outline,
    confirmStyle: Style = Style.Empty,
    dismissStyle: Style = Style.Empty,
    properties: UiDialogProperties = UiDialogProperties(),
    style: Style = Style.Empty
): UiAlertDialogResult {
    var action: UiAlertDialogAction? = null
    val popup = shadcnDialog(
        id = id,
        expanded = expanded,
        width = width,
        properties = properties.copy(surfaceStyle = properties.surfaceStyle then style),
        header = {
            text(title, style = Style { textSize(theme.typography.title) }, wrap = UiTextWrap.Word)
        },
        actions = {
            // Standard action row
            dismissLabel?.let { label ->
                if (
                    shadcnButton(
                        id = "$id.dismiss",
                        label = label,
                        modifier = Modifier.width(88f.dp),
                        variant = dismissVariant,
                        size = ShadcnButtonSize.Sm,
                        style = dismissStyle
                    )
                ) {
                    action = UiAlertDialogAction.Dismiss
                }
            }
            spacer(Modifier.width(8f.dp))
            if (
                shadcnButton(
                    id = "$id.confirm",
                    label = confirmLabel,
                    modifier = Modifier.width(88f.dp),
                    variant = confirmVariant,
                    size = ShadcnButtonSize.Sm,
                    style = confirmStyle
                )
            ) {
                action = UiAlertDialogAction.Confirm
            }
        }
    ) {
        supportingText(message)
    }
    return UiAlertDialogResult(popup = popup, action = action)
}

data class UiAlertDialogResult(
    val popup: UiPopupResult,
    val action: UiAlertDialogAction?
)

enum class UiAlertDialogAction {
    Confirm,
    Dismiss
}
