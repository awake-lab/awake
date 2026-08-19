// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.graphics.dispatchPrimitive
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.scope.claimModifiedSlot

/**
 * Draws an offscreen render target into the layout slot [modifier] claims. [material] is typed
 * [Any] rather than `awake-engine-render-api`'s `Material` interface -- that module already
 * depends on this one, so a `Material` reference here would create a cycle (see
 * [io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive.Texture]'s own doc for the full reasoning).
 */
fun UiPrimitiveScope.textureQuad(material: Any, modifier: UiModifier = Modifier) {
    val slot = claimModifiedSlot(modifier.withSizeFallback(Dimension.FillMax, Dimension.FillMax))
    dispatchPrimitive(UiDrawPrimitive.Texture(slot.x, slot.y, slot.width, slot.height, material), overlay = false)
}
