// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.graphics

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPath
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.bounds
import io.github.ronjunevaldoz.awake.ui.safeInteriorMargin
import io.github.ronjunevaldoz.awake.ui.toPath

fun UiPrimitiveScope.clip(rect: UiBounds, content: UiPrimitiveScope.() -> Unit) {
    val resolved = context.pushClipInternal(rect, overlay = emitsToOverlay)
    emit(UiDrawPrimitive.ClipPush(resolved))
    content()
    val restore = context.popClipInternal(overlay = emitsToOverlay)
    emit(UiDrawPrimitive.ClipPop(restore))
}

fun UiPrimitiveScope.clip(path: UiPath, content: UiPrimitiveScope.() -> Unit) = clipPath(path, safeInteriorRect = null, content)

fun UiPrimitiveScope.clip(shape: UiShapeSpec, rect: UiBounds, content: UiPrimitiveScope.() -> Unit) {
    val margin = shape.safeInteriorMargin(rect)
    val safeInteriorRect = UiBounds(
        x = rect.x + margin,
        y = rect.y + margin,
        width = (rect.width - 2f * margin).coerceAtLeast(0f),
        height = (rect.height - 2f * margin).coerceAtLeast(0f),
    )
    clipPath(shape.toPath(rect), safeInteriorRect, content)
}

private fun UiPrimitiveScope.clipPath(path: UiPath, safeInteriorRect: UiBounds?, content: UiPrimitiveScope.() -> Unit) {
    val resolvedBounds = context.pushClipInternal(path.bounds(), overlay = emitsToOverlay)
    emit(UiDrawPrimitive.ClipPathPush(path, resolvedBounds, safeInteriorRect))
    content()
    val restore = context.popClipInternal(overlay = emitsToOverlay)
    emit(UiDrawPrimitive.ClipPop(restore))
}
