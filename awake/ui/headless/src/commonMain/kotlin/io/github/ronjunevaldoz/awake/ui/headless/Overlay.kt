// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.internal.overlayScrim as primitiveOverlayScrim

/** Draws a full overlay-layer scrim over [slot] using a caller-provided visual color. */
fun UiScope.overlayScrim(slot: UiBounds, color: Color) {
    primitive.primitiveOverlayScrim(slot, color)
}
