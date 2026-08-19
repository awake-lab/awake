// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.internal.layout

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.canvas

/** Draws a full overlay-layer scrim over [slot]. The caller chooses its visual token/color. */
fun UiPrimitiveScope.overlayScrim(slot: UiBounds, color: Color) {
    canvas(slot) { drawRect(0f, 0f, slot.width, slot.height, color, overlay = true) }
}
