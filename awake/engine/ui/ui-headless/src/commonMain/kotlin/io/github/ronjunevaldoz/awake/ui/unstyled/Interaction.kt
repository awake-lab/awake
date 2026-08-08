// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.inputState
import io.github.ronjunevaldoz.awake.ui.isFocused
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.requestFocus
import io.github.ronjunevaldoz.awake.ui.scope.claimModifiedSlot

internal data class UiInteraction(
    val slot: UiBounds,
    val hovered: Boolean,
    val active: Boolean,
    val clicked: Boolean,
)

/** [enabled] mirrors [io.github.ronjunevaldoz.awake.ui.modifier.resolveClickable]'s own gating
 * shape: when `false`, [tryClaimActive] is simply never called with a claimable state, so
 * `active`/`clicked` (and, for widgets that drive dragging off `isActive`, like `slider`, the
 * drag itself) can never fire -- no separate `enabled` check needed at each call site below. */
internal fun UiScope.interact(
    id: String,
    modifier: UiModifier = Modifier,
    enabled: Boolean = true,
): UiInteraction {
    val slot = claimModifiedSlot(modifier)
    val hovered = hitTest(slot)
    tryClaimActive(id, hovered && enabled)
    val wasActiveBeforeRelease = isActive(id)
    releaseActiveIfMatches(id)
    val active = isActive(id)
    val activatedByKeyboard = enabled && isFocused(id) && inputState.activatePressed
    val activatedByPointer = wasActiveBeforeRelease && !active && hovered
    // A pointer click also claims keyboard focus -- otherwise Space-repeat-activation (below)
    // has nothing to be focused on, since nothing else in this engine drives Tab-focus yet.
    if (activatedByPointer && enabled) requestFocus(id)
    return UiInteraction(
        slot = slot,
        hovered = hovered,
        active = active,
        clicked = activatedByPointer || activatedByKeyboard,
    )
}
