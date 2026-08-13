// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.api

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds

/** Shared popup state contract implemented by the runtime and exposed by Headless. */
interface UiPopupState {
    var expanded: Boolean

    fun open()
    fun close()
    fun toggle()
}

/** Measured popup extent in runtime coordinates. */
data class UiPopupSize(
    val width: Float,
    val height: Float,
)

/** Platform-neutral popup interaction and clipping policy. */
data class UiPopupProperties(
    val dismissOnClickOutside: Boolean = true,
    val clippingEnabled: Boolean = true,
)

/** Result of composing a popup for one frame. */
data class UiPopupResult(
    val slot: UiBounds?,
    val dismissed: Boolean,
)

/** Computes a popup slot from measured bounds without accessing the UI runtime. */
fun interface UiPopupPositionProvider {
    fun calculatePosition(
        anchorBounds: UiBounds,
        windowBounds: UiBounds,
        popupContentSize: UiPopupSize,
    ): UiBounds
}
