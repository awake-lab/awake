// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds

/** Draws a full overlay-layer scrim over [slot]. The caller chooses its visual token/color. */
fun UiScope.overlayScrim(slot: UiBounds, color: Color) {
    emitOverlay(UiDrawPrimitive.Quad(slot.x, slot.y, slot.width, slot.height, color))
}
