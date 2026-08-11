// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.UiScope as HeadlessUiScope

/** Draws a full overlay-layer scrim over [slot]. The caller chooses its visual token/color. */
fun UiPrimitiveScope.overlayScrim(slot: UiBounds, color: Color) {
    emitOverlay(UiDrawPrimitive.Quad(slot.x, slot.y, slot.width, slot.height, color))
}

/** Public-facade overload of [overlayScrim] for ordinary Headless and design-system recipes. */
fun HeadlessUiScope.overlayScrim(slot: UiBounds, color: Color) {
    primitive.overlayScrim(slot, color)
}
