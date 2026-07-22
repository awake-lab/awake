package io.github.ronjunevaldoz.awake.ui.designsystem.components.popup

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.supportingText
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.layouts.ext.spacer
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.unstyled.button
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.width

fun UiScope.alertDialog(
    id: String,
    expanded: Boolean,
    title: String,
    message: String,
    width: Dimension = Dimension.Fixed(320f.px),
    confirmLabel: String = "Confirm",
    dismissLabel: String? = "Cancel",
    confirmVariant: UiButtonVariant = UiButtonVariant.Filled,
    dismissVariant: UiButtonVariant = UiButtonVariant.Ghost,
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
            text(title, style = Style.Companion { textSize(theme.typography.title) }, wrap = UiTextWrap.Word)
        },
        actions = {
            dismissLabel?.let { label ->
                if (
                    button(
                        id = "$id.dismiss",
                        label = label,
                        modifier = UiModifier().width(96f.px).height(32f.px),
                        style = dismissStyle,
                        variant = dismissVariant,
                        radius = UiShape.sm
                    )
                ) {
                    action = UiAlertDialogAction.Dismiss
                }
            }
            spacer(UiModifier().width(8f.dp))
            if (
                button(
                    id = "$id.confirm",
                    label = confirmLabel,
                    modifier = UiModifier().width(96f.px). height(32f.px),
                    style = confirmStyle,
                    variant = confirmVariant,
                    radius = UiShape.sm
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