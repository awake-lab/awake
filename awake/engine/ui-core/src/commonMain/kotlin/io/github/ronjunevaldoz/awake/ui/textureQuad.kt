package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.modifier.Dimension
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier

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