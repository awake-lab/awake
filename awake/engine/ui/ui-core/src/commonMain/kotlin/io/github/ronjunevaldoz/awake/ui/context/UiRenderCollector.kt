// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive

internal class UiRenderCollector {
    private val primitives = ArrayList<UiDrawPrimitive>()
    private val overlayPrimitives = ArrayList<UiDrawPrimitive>()

    fun beginFrame() {
        primitives.clear()
        overlayPrimitives.clear()
    }

    fun emit(primitive: UiDrawPrimitive, measuring: Boolean) {
        if (!measuring) primitives += primitive
    }

    fun emitOverlay(primitive: UiDrawPrimitive, measuring: Boolean) {
        if (!measuring) overlayPrimitives += primitive
    }

    fun endFrame(): List<UiDrawPrimitive> = primitives + overlayPrimitives
}
