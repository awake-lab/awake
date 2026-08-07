// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.modifier

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.inputState
import io.github.ronjunevaldoz.awake.ui.isFocused
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.requestFocus

/** Holds a [Modifier.clickable]-attached handler. Stored on [UiModifier.clickAction] --
 * deliberately not named `clickable` on the data class itself, to dodge the same builder-vs-
 * stored-field name collision `forceHover`/`forceActive`/`forceFocus`/`testTag`/`styleable`/
 * `graphicsLayer` already carry (see `UiModifier.kt`'s class doc and
 * `docs/reference/ui-ownership.md`'s Hard Rule 7). */
data class UiClickable(val enabled: Boolean = true, val onClick: () -> Unit)

/** Attaches a click handler to this modifier chain, mirroring Compose's
 * `Modifier.clickable(enabled, onClick)` call shape. This does not, by itself, make every
 * widget interactive -- unlike Compose, where any layout node wired to the input pipeline
 * automatically honors `Modifier.clickable`, Awake widgets must opt in by calling
 * [UiScope.resolveClickable] once they've claimed their own slot (the same point
 * `forceHover`/`forceActive`/`forceFocus` are already resolved). See [resolveClickable]'s doc
 * for why: Awake's click detection is id-based (`tryClaimActive`/`isActive`), and only the
 * widget itself knows the stable `id` to key that state on. */
fun UiModifier.clickable(enabled: Boolean = true, onClick: () -> Unit): UiModifier =
    copy(clickAction = UiClickable(enabled, onClick))

/** Resolves a [UiModifier.clickable] handler (if any) against this widget's already-claimed
 * [slot], using [id] as the active-state key. Built entirely on the existing
 * `hitTest`/`tryClaimActive`/`isActive`/`releaseActiveIfMatches` mechanism -- the same
 * press-inside, release-inside "must stay hovered through the whole press" semantics
 * `ui-headless`'s `interact()` (see `Buttons.kt`'s call chain) already uses for `button`/
 * `checkbox`/etc., just generalized to any widget that owns a stable `id` and a claimed slot,
 * not a parallel input pipeline. No-op if `modifier.clickAction` is null. */
fun UiScope.resolveClickable(id: String, slot: UiBounds, modifier: UiModifier) {
    val click = modifier.clickAction ?: return
    val hovered = hitTest(slot)
    tryClaimActive(id, hovered && click.enabled)
    val wasActive = isActive(id)
    releaseActiveIfMatches(id)
    val stillActive = isActive(id)
    val activatedByKeyboard = click.enabled && isFocused(id) && inputState.activatePressed
    val activatedByPointer = wasActive && !stillActive && hovered
    // A pointer click also claims keyboard focus -- otherwise Space-repeat-activation (below)
    // has nothing to be focused on, since nothing else in this engine drives Tab-focus yet.
    if (activatedByPointer && click.enabled) requestFocus(id)
    if (click.enabled && (activatedByPointer || activatedByKeyboard)) click.onClick()
}
