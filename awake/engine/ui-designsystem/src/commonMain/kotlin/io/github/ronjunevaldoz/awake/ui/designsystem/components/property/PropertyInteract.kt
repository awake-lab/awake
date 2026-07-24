package io.github.ronjunevaldoz.awake.ui.designsystem.components.property

import io.github.ronjunevaldoz.awake.ui.modifier.Dimension
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier

internal fun UiScope.propertyInteract(
    id: String,
    width: Dimension,
    height: Dimension,
    modifier: UiModifier = Modifier
): PropertyInteraction {
    val slot = claimModifiedSlot(width, height, modifier)
    val hovered = hitTest(slot)
    tryClaimActive(id, hovered)
    val wasActiveBeforeRelease = isActive(id)
    releaseActiveIfMatches(id)
    val active = isActive(id)
    return PropertyInteraction(
        slot = slot,
        hovered = hovered,
        active = active,
        clicked = wasActiveBeforeRelease && !active && hovered
    )
}