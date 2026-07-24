package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/**
 * TODO revisit what is the usecase??
 */
fun UiScope.textureQuad(material: Any, modifier: UiModifier = UiModifier()) {
    val slot = claimModifiedSlot(
        defaultWidth = Dimension.FillMax,
        defaultHeight = Dimension.FillMax,
        modifier = modifier
    )
    emit(UiDrawPrimitive.Texture(slot.x, slot.y, slot.width, slot.height, material))
}