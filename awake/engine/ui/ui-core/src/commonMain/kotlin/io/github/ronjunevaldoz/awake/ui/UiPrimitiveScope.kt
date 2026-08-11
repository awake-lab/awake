// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiContext
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.LayoutWeight

/**
 * Raw runtime primitives used to construct layouts and emit a UI frame.
 *
 * This is deliberately a core-only authoring surface. Ordinary widgets should use Headless'
 * public `UiPrimitiveScope` facade rather than depending on this runtime interface directly.
 */
@AwakeUiDsl
interface UiPrimitiveScope {
    val emitsToOverlay: Boolean

    /** Internal runtime owner. Never expose this through ordinary widget APIs. */
    val context: UiContext

    fun claimSlot(width: Dimension, height: Dimension, weight: LayoutWeight? = null): UiBounds

    fun hitTest(slot: UiBounds): Boolean
    fun isActive(id: String): Boolean
    fun tryClaimActive(id: String, hovered: Boolean)
    fun releaseActiveIfMatches(id: String)

    /** Normal, in-order draw primitive -- painted in call order. */
    fun emit(primitive: UiDrawPrimitive)

    /** Always painted after every [emit]-ed primitive this frame. */
    fun emitOverlay(primitive: UiDrawPrimitive)

    fun widgetState(id: String): WidgetState
}
