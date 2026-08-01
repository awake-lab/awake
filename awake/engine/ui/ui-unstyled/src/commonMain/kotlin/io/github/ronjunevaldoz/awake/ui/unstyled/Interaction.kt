// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier

internal data class UiInteraction(
    val slot: UiBounds,
    val hovered: Boolean,
    val active: Boolean,
    val clicked: Boolean
)

internal fun UiScope.interact(
    id: String,
    modifier: UiModifier = Modifier
): UiInteraction {
    val slot = claimModifiedSlot(modifier)
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
