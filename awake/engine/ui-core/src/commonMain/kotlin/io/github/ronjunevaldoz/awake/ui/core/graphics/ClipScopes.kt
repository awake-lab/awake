package io.github.ronjunevaldoz.awake.ui.core.graphics

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPath
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.UiSlot
import io.github.ronjunevaldoz.awake.ui.bounds
import io.github.ronjunevaldoz.awake.ui.toPath

fun UiScope.clip(rect: UiSlot, content: UiScope.() -> Unit) {
    val resolved = context.pushClipInternal(rect)
    emit(UiDrawPrimitive.ClipPush(resolved))
    content()
    val restore = context.popClipInternal()
    emit(UiDrawPrimitive.ClipPop(restore))
}

fun UiScope.clip(path: UiPath, content: UiScope.() -> Unit) {
    val resolvedBounds = context.pushClipInternal(path.bounds())
    emit(UiDrawPrimitive.ClipPathPush(path, resolvedBounds))
    content()
    val restore = context.popClipInternal()
    emit(UiDrawPrimitive.ClipPop(restore))
}

fun UiScope.clip(shape: UiShapeSpec, rect: UiSlot, content: UiScope.() -> Unit) {
    clip(shape.toPath(rect), content)
}
