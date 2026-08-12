// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnStyles
import io.github.ronjunevaldoz.awake.ui.headless.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.buttonSlot
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme

private fun UiModifier.withShadcnSize(size: ShadcnButtonSize): UiModifier =
    if (heightDimension == null) height(size.heightDp) else this

/** Real shadcn's button carries its horizontal inset on the button itself (`px-4` for the
 * default size -- see [ShadcnButtonSize]); this is part of intrinsic measurement as well as
 * painting. Applied before the caller's own `style` so an explicit `contentPadding` override
 * still wins. */
private fun shadcnButtonSizeStyle(size: ShadcnButtonSize): Style {
    return Style {
        contentPadding(horizontal = size.paddingX, vertical = 0f.dp)
    }
}

private fun shadcnButtonStyle(
    theme: UiTheme,
    variant: ShadcnButtonVariant,
    size: ShadcnButtonSize,
    style: Style,
): Style = ShadcnStyles.button(theme.asShadcnTheme(), variant) then
    shadcnButtonSizeStyle(size) then style

/**
 * Shadcn button with a simple text label.
 * Returns true if clicked this frame (standard IMGUI pattern).
 */
@Deprecated(
    message = "Use the Headless UiScope overload. This Core receiver is retained only for migration.",
)
fun UiScope.shadcnButton(
    id: String,
    label: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnButtonVariant = ShadcnButtonVariant.Primary,
    size: ShadcnButtonSize = ShadcnButtonSize.Md,
    style: Style = Style.Empty,
    centered: Boolean = true,
    verticallyCentered: Boolean = centered,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
): Boolean {
    val clicked = buttonSlot(
        id = id,
        label = label,
        modifier = modifier.withShadcnSize(size),
        style = shadcnButtonStyle(theme, variant, size, style),
        variant = variant.toUiButtonVariant(),
        radius = theme.asShadcnTheme().radii.md,
        centered = centered,
        verticallyCentered = verticallyCentered,
        enabled = enabled,
    ).clicked
    if (clicked) onClick?.invoke()
    return clicked
}

/**
 * Shadcn button with a Compose-style Slot API.
 * The [content] lambda receives a [BoxScope], allowing arbitrary layouts inside the button.
 */
@Deprecated(
    message = "Use the Headless UiScope overload. This Core receiver is retained only for migration.",
)
fun UiScope.shadcnButton(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnButtonVariant = ShadcnButtonVariant.Primary,
    size: ShadcnButtonSize = ShadcnButtonSize.Md,
    style: Style = Style.Empty,
    centered: Boolean = true,
    verticallyCentered: Boolean = centered,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: BoxScope.(slot: UiBounds) -> Unit,
): Boolean {
    val buttonStyle = shadcnButtonStyle(theme, variant, size, style)
    val result = buttonSlot(
        id = id,
        modifier = modifier.withShadcnSize(size),
        style = buttonStyle,
        variant = variant.toUiButtonVariant(),
        radius = theme.asShadcnTheme().radii.md,
        enabled = enabled,
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

private fun ShadcnButtonVariant.toUiButtonVariant(): UiButtonVariant = when (this) {
    ShadcnButtonVariant.Outline -> UiButtonVariant.Outline
    ShadcnButtonVariant.Ghost -> UiButtonVariant.Ghost
    else -> UiButtonVariant.Filled
}
