package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.scope.claimModifiedSlot

/**
 * TODO revisit what is the usecase??
 */
fun UiScope.textureQuad(material: Any, modifier: UiModifier = Modifier) {
    val slot = claimModifiedSlot(modifier.withSizeFallback(Dimension.FillMax, Dimension.FillMax))
    emit(UiDrawPrimitive.Texture(slot.x, slot.y, slot.width, slot.height, material))
}
