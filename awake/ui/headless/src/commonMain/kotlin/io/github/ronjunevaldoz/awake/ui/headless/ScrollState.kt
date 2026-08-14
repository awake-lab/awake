// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.UiScrollConfig
import io.github.ronjunevaldoz.awake.ui.UiScrollState
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll as primitiveVerticalScroll
import io.github.ronjunevaldoz.awake.ui.rememberScrollState as rememberPrimitiveScrollState

/** Runtime-free handle for a scroll position used by Headless modifiers. */
class ScrollState internal constructor(
    internal val primitive: UiScrollState,
) {
    /** Current horizontal scroll offset in authored pixels. */
    val offsetX: Float get() = primitive.offsetX

    /** Current vertical scroll offset in authored pixels. */
    val offsetY: Float get() = primitive.offsetY

    /** Content extent reported by the latest layout pass. */
    val contentWidth: Float get() = primitive.contentWidth
    val contentHeight: Float get() = primitive.contentHeight

    /** Viewport extent reported by the latest layout pass. */
    val viewportWidth: Float get() = primitive.viewportWidth
    val viewportHeight: Float get() = primitive.viewportHeight

    val maxOffsetX: Float get() = primitive.maxOffsetX
    val maxOffsetY: Float get() = primitive.maxOffsetY

    fun scrollTo(offsetX: Float = this.offsetX, offsetY: Float = this.offsetY) {
        primitive.scrollTo(offsetX = offsetX, offsetY = offsetY)
    }
}

fun UiContext.rememberScrollState(
    id: String,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f,
): ScrollState = ScrollState(rememberPrimitiveScrollState(id, initialOffsetX, initialOffsetY))

fun UiScope.rememberScrollState(
    id: String,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f,
): ScrollState = ScrollState(
    with(primitive.context) {
        rememberPrimitiveScrollState(id, initialOffsetX, initialOffsetY)
    },
)

fun Modifier.verticalScroll(
    state: ScrollState,
    config: UiScrollConfig = UiScrollConfig.Default,
): Modifier = HeadlessModifier(asPrimitiveModifier().primitiveVerticalScroll(state.primitive, config))
