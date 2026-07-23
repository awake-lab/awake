// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.popup

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.components.awakeShadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.supportingText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.ext.spacer
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.width

fun UiScope.alertDialog(
    id: String,
    expanded: Boolean,
    title: String,
    message: String,
    width: Dimension = Dimension.Fixed(320f.dp),
    confirmLabel: String = "Confirm",
    dismissLabel: String? = "Cancel",
    confirmVariant: AwakeShadcnButtonVariant = AwakeShadcnButtonVariant.Primary,
    dismissVariant: AwakeShadcnButtonVariant = AwakeShadcnButtonVariant.Outline,
    confirmStyle: Style = Style.Empty,
    dismissStyle: Style = Style.Empty,
    properties: UiDialogProperties = UiDialogProperties(),
    style: Style = Style.Empty
): UiAlertDialogResult {
    var action: UiAlertDialogAction? = null
    val popup = dialog(
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
                    awakeShadcnButton(
                        id = "$id.dismiss",
                        label = label,
                        modifier = UiModifier().width(88f.dp),
                        variant = dismissVariant,
                        size = AwakeShadcnButtonSize.Sm,
                        style = dismissStyle
                    )
                ) {
                    action = UiAlertDialogAction.Dismiss
                }
            }
            spacer(UiModifier().width(8f.dp))
            if (
                awakeShadcnButton(
                    id = "$id.confirm",
                    label = confirmLabel,
                    modifier = UiModifier().width(88f.dp),
                    variant = confirmVariant,
                    size = AwakeShadcnButtonSize.Sm,
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
