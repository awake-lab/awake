// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.context

/**
 * Platform pointer-cursor shape a widget wants shown this frame -- [UiContext.requestCursor]'s
 * only argument. Resets to [Default] every [UiContext.beginFrame]; the last widget to call
 * [UiContext.requestCursor] in a frame wins (mirrors [UiContext]'s general "last write this
 * frame wins" shape, e.g. `setActive`). The embedding platform reads the resolved value off
 * [UiFrameOutput.effects]' `cursor` field and applies it via its own windowing API -- `ui-core`
 * owns only the request/reset contract, never the platform call itself.
 */
enum class UiCursor {
    Default,
    ResizeHorizontal,
    ResizeVertical,
    Pointer,
    Text,
}
