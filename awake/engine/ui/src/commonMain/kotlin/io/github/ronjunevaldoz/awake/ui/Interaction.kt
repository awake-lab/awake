// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

internal data class UiInteraction(
    val slot: UiSlot,
    val hovered: Boolean,
    val active: Boolean,
    val clicked: Boolean
)

internal fun UiScope.interact(id: String, width: Dimension, height: Dimension): UiInteraction {
    val slot = claimSlot(width, height)
    val hovered = hitTest(slot)
    tryClaimActive(id, hovered)
    val wasActiveBeforeRelease = isActive(id)
    releaseActiveIfMatches(id)
    val active = isActive(id)
    return UiInteraction(
        slot = slot,
        hovered = hovered,
        active = active,
        clicked = wasActiveBeforeRelease && !active && hovered
    )
}
