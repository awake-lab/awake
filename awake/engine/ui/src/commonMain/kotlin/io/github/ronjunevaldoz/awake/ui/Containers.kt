// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

fun UiScope.textureQuad(width: Float, height: Float, material: Any, modifier: UiModifier = UiModifier()) {
    val slot = claimSlot(modifier.width ?: width.toDimension(), modifier.height ?: height.toDimension())
    emit(UiDrawPrimitive.Texture(slot.x, slot.y, slot.width, slot.height, material))
}

fun UiScope.panel(
    id: String,
    width: Dimension,
    height: Dimension,
    radius: Dp = UiShape.md,
    borderWidth: Dp = UiShape.none,
    style: Style = Style.Empty,
    modifier: UiModifier = UiModifier(),
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot {
    val slot = claimSlot(modifier.width ?: width, modifier.height ?: height)
    val resolved = resolveStyle(
        style = style,
        defaults = theme.components.panel then Style {
            shape(radius)
            borderWidth(borderWidth)
        }
    )
    emitFillAndBorder(
        slot = slot,
        fillColor = resolved.background ?: TransparentColor,
        radiusPx = resolved.shape.toPx(),
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: theme.tokens.border
    )
    context.column(slot, font = font, theme = theme, textScale = resolved.textScale, insets = resolved.contentPadding).content(slot)
    return slot
}

fun UiScope.clip(rect: UiSlot, content: UiScope.() -> Unit) {
    val resolved = context.pushClipInternal(rect)
    emit(UiDrawPrimitive.ClipPush(resolved))
    content()
    val restore = context.popClipInternal()
    emit(UiDrawPrimitive.ClipPop(restore))
}
