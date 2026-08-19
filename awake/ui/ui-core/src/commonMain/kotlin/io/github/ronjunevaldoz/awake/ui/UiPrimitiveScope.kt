// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.LayoutWeight
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext

/**
 * Raw runtime primitives used to construct layouts and emit a UI frame.
 *
 * This is deliberately a core-only authoring surface. Ordinary widgets should use Headless'
 * public `UiPrimitiveScope` facade rather than depending on this runtime interface directly.
 */
@AwakeUiDsl
interface UiPrimitiveScope {
    val emitsToOverlay: Boolean

    /** Internal runtime owner. Never expose this through ordinary widget APIs.
     *
     * Not yet narrowed further: unlike `emit`/`emitOverlay` (moved to the internal-only
     * [UiPrimitiveEmitter] once [CanvasScope][io.github.ronjunevaldoz.awake.ui.CanvasScope]
     * became the sole draw path), `context` still has real callers outside `ui-core`'s draw
     * path -- production theme/local push-pop in `ui-designsystem`
     * (`ShadcnButtonGroupRecipes.kt`, `ShadcnThemeLocals.kt`) and `ui-headless`
     * (`ScrollState.kt`, `UiScope.requestFocus`), plus the hundreds of
     * `primitive.context.createAbsolute/createColumn/createBox(...)` test-scope factory calls
     * across all three UI modules. See
     * `docs/tasks/2026-08-18-ui-capability-scopes-plan.md` step 4 for the re-measurement; this
     * is a separate, much larger migration than the draw-scope one, not scoped here.
     */
    val context: UiContext

    fun claimSlot(width: Dimension, height: Dimension, weight: LayoutWeight? = null): UiBounds

    fun hitTest(slot: UiBounds): Boolean
    fun isActive(id: String): Boolean
    fun tryClaimActive(id: String, hovered: Boolean)
    fun releaseActiveIfMatches(id: String)

    fun widgetState(id: String): WidgetState
}

/**
 * Raw primitive-emission capability underneath the draw scope -- [CanvasScope][io.github.ronjunevaldoz.awake.ui.CanvasScope]'s
 * own `draw*` members and the shared paint helpers in `graphics/` (`dispatchPrimitive` and
 * everything that calls it) route through this, one layer below the public authoring surface.
 * Deliberately not on [UiPrimitiveScope]: nothing outside `ui-core`'s own draw plumbing needs
 * raw `emit`/`emitOverlay` now that `CanvasScope` is the sole draw path (see
 * `docs/tasks/2026-08-18-ui-capability-scopes-plan.md` step 4). The one concrete implementer is
 * `AbstractUiScope`.
 */
internal interface UiPrimitiveEmitter {
    /** Normal, in-order draw primitive -- painted in call order. */
    fun emit(primitive: UiDrawPrimitive)

    /** Always painted after every [emit]-ed primitive this frame. */
    fun emitOverlay(primitive: UiDrawPrimitive)
}
