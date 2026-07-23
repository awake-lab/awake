// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.designsystem.asAwakeShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.AwakeShadcnStyles
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.height
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.unstyled.buttonSlot

private fun UiModifier.withShadcnSize(size: AwakeShadcnButtonSize): UiModifier =
    if (height == null) height(size.heightDp.dp) else this

private fun awakeShadcnButtonStyle(
    theme: UiTheme,
    variant: AwakeShadcnButtonVariant,
    style: Style
): Style = AwakeShadcnStyles.button(theme.asAwakeShadcnTheme(), variant) then style

/** 
 * Shadcn button with a simple text label.
 * Returns true if clicked this frame (standard IMGUI pattern).
 */
fun UiScope.awakeShadcnButton(
    id: String,
    label: String,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnButtonVariant = AwakeShadcnButtonVariant.Primary,
    size: AwakeShadcnButtonSize = AwakeShadcnButtonSize.Md,
    style: Style = Style.Empty,
    centered: Boolean = true,
    verticallyCentered: Boolean = centered,
    onClick: (() -> Unit)? = null
): Boolean {
    val clicked = buttonSlot(
        id = id,
        label = label,
        modifier = modifier.withShadcnSize(size),
        style = awakeShadcnButtonStyle(theme, variant, style),
        variant = variant.toUiButtonVariant(),
        radius = theme.asAwakeShadcnTheme().radii.lg,
        centered = centered,
        verticallyCentered = verticallyCentered
    ).clicked
    if (clicked) onClick?.invoke()
    return clicked
}

/** 
 * Shadcn button with a Compose-style Slot API.
 * The [content] lambda receives a [BoxScope], allowing arbitrary layouts inside the button.
 */
fun UiScope.awakeShadcnButton(
    id: String,
    modifier: UiModifier = UiModifier(),
    variant: AwakeShadcnButtonVariant = AwakeShadcnButtonVariant.Primary,
    size: AwakeShadcnButtonSize = AwakeShadcnButtonSize.Md,
    style: Style = Style.Empty,
    centered: Boolean = true,
    verticallyCentered: Boolean = centered,
    onClick: (() -> Unit)? = null,
    content: BoxScope.(slot: UiSlot) -> Unit
): Boolean {
    val buttonStyle = awakeShadcnButtonStyle(theme, variant, style)
    val result = buttonSlot(
        id = id,
        modifier = modifier.withShadcnSize(size),
        style = buttonStyle,
        variant = variant.toUiButtonVariant(),
        radius = theme.asAwakeShadcnTheme().radii.lg
    ) { contentSlot ->
        val alignment = when {
            centered && verticallyCentered -> UiAlignment.Center
            centered -> UiAlignment.TopCenter
            verticallyCentered -> UiAlignment.CenterStart
            else -> UiAlignment.TopStart
        }
        val box = childBox(contentSlot, contentAlignment = alignment)
        box.content(contentSlot)
    }
    if (result.clicked) onClick?.invoke()
    return result.clicked
}

private fun AwakeShadcnButtonVariant.toUiButtonVariant(): UiButtonVariant = when (this) {
    AwakeShadcnButtonVariant.Outline -> UiButtonVariant.Outline
    AwakeShadcnButtonVariant.Ghost -> UiButtonVariant.Ghost
    else -> UiButtonVariant.Filled
}
