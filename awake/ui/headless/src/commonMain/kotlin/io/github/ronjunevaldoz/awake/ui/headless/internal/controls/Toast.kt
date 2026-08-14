// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.internal.controls

import io.github.ronjunevaldoz.awake.ui.UiPrimitiveScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.headless.internal.text.text
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.rememberFloatState
import io.github.ronjunevaldoz.awake.ui.scope.frameDeltaSeconds
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.style.Style

private const val TOAST_WIDTH_DP = 280f
private const val TOAST_HEIGHT_DP = 44f

/**
 * Real shadcn's `Toast`/Sonner concept: a non-interactive message that appears and disappears
 * on its own -- no Radix primitive here (Radix has no Toast), only shadcn/Sonner's. [id] tracks
 * how long this exact toast has been shown across frames, so a caller keeping a list of active
 * toast ids just calls this once per id per frame and drops it once it returns `false`, the same
 * "keep calling while true" shape [io.github.ronjunevaldoz.awake.ui.headless.checkbox]'s `Boolean`
 * return already uses -- no separate toast-manager type needed on this side, the caller's own
 * list IS the manager.
 *
 * v1 has no fade-in/out (real shadcn/Sonner animates both) -- [durationMs] is a hard show/hide
 * cutoff. Wire `animateFloatTween`-driven opacity once a caller actually needs it; nothing here
 * blocks adding it later since the elapsed-time state this already tracks is exactly what a fade
 * would key off of.
 */
fun UiPrimitiveScope.toast(
    id: String,
    message: String,
    modifier: UiModifier = Modifier,
    durationMs: Float = 3000f,
    style: Style = Style.Empty,
): Boolean {
    val elapsed = rememberFloatState(id, "elapsed")
    if (elapsed.value >= durationMs) return false
    elapsed.value += frameDeltaSeconds() * 1000f

    val theme = context.currentTheme
    val surface = resolveSurface(
        // WrapContent needs a measuring composite pass to resolve (AbsoluteScope.claimSlot
        // throws without one, see UiScopeMetrics), so -- same reasoning Dropdown.kt/TextField.kt
        // already landed on -- both axes fall back to a fixed size, not content-derived.
        modifier = modifier.withSizeFallback(
            Dimension.Fixed(TOAST_WIDTH_DP.dp),
            Dimension.Fixed(TOAST_HEIGHT_DP.dp),
        ),
        style = style,
        defaults = theme.components.surface,
    )
    paintSurface(slot = surface.slot, resolved = surface.resolved)
    text(
        message,
        slot = surface.contentSlot,
        font = context.currentFont,
        color = surface.resolved.foreground ?: theme.colors.foreground,
        verticallyCentered = true,
        semanticId = "$id.label",
    )
    recordSemantic(
        role = UiSemanticRole.Toast,
        id = id,
        label = message,
        bounds = surface.slot,
    )
    return true
}
