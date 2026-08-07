// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.popup

import io.github.ronjunevaldoz.awake.ui.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.typography.shadcnSupportingText
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
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

/**
 * [shadcnAlertDialog] primary Slot API: the caller composes their own action row freely (any
 * number of [shadcnButton]s, any variant, even a non-button action) instead of being locked into
 * exactly two named buttons. Mirrors the reference shadcn-compose `ShadcnAlertDialog`, which is
 * just `ShadcnDialog(showCloseButton = false)` with plain buttons composed inside its content --
 * `dismissOnClickOutside` is left to [properties] (default `true`, same as [shadcnDialog]) rather
 * than force-disabled, so this stays behavior-identical to the pre-existing fixed-2-button form.
 * The fixed confirm/dismiss-label overload below is a convenience wrapper built on top of this.
 */
fun UiScope.shadcnAlertDialog(
    id: String,
    expanded: Boolean,
    title: String,
    width: Dimension = Dimension.Fixed(320f.dp),
    properties: UiDialogProperties = UiDialogProperties(),
    style: Style = Style.Empty,
    actions: RowScope.() -> Unit,
    body: ColumnScope.() -> Unit
): UiPopupResult = shadcnDialog(
    id = id,
    expanded = expanded,
    width = width,
    properties = properties.copy(surfaceStyle = properties.surfaceStyle then style),
    showCloseButton = false,
    header = {
        text(title, style = Style { textSize(theme.typography.title) }, wrap = UiTextWrap.Word)
    },
    actions = { actions() }
) {
    body()
}

/** [shadcnAlertDialog] convenience with fixed confirm/dismiss [shadcnButton]s, built on the
 * [actions]-slot primary overload above. Source-compatible with every existing call site. */
fun UiScope.shadcnAlertDialog(
    id: String,
    expanded: Boolean,
    title: String,
    width: Dimension = Dimension.Fixed(320f.dp),
    confirmLabel: String = "Confirm",
    dismissLabel: String? = "Cancel",
    confirmVariant: ShadcnButtonVariant = ShadcnButtonVariant.Primary,
    dismissVariant: ShadcnButtonVariant = ShadcnButtonVariant.Outline,
    confirmStyle: Style = Style.Empty,
    dismissStyle: Style = Style.Empty,
    properties: UiDialogProperties = UiDialogProperties(),
    style: Style = Style.Empty,
    body: ColumnScope.() -> Unit
): UiAlertDialogResult {
    var action: UiAlertDialogAction? = null
    val popup = shadcnAlertDialog(
        id = id,
        expanded = expanded,
        title = title,
        width = width,
        properties = properties,
        style = style,
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
        body()
    }
    return UiAlertDialogResult(popup = popup, action = action)
}

/** [shadcnAlertDialog] convenience with a plain string message. */
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
): UiAlertDialogResult = shadcnAlertDialog(
    id = id,
    expanded = expanded,
    title = title,
    width = width,
    confirmLabel = confirmLabel,
    dismissLabel = dismissLabel,
    confirmVariant = confirmVariant,
    dismissVariant = dismissVariant,
    confirmStyle = confirmStyle,
    dismissStyle = dismissStyle,
    properties = properties,
    style = style
) {
    shadcnSupportingText(message)
}

data class UiAlertDialogResult(
    val popup: UiPopupResult,
    val action: UiAlertDialogAction?
)

enum class UiAlertDialogAction {
    Confirm,
    Dismiss
}
