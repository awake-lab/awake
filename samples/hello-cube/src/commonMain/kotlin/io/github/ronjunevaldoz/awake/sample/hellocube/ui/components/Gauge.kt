// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.hellocube.ui.components

import io.github.ronjunevaldoz.awake.ui.UiDrawPrimitive
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.toDimension

/**
 * Reference example, not wired to any real demo -- proves Awake's UI DSL is genuinely
 * open: a consumer defines a fully custom widget the exact same way the library defines
 * `button`/`toggle`/etc (an ordinary extension function on [UiScope], built from
 * [UiScope.claimSlot]/[UiScope.emit]), with no library change and no capability gap versus a
 * built-in widget. A read-only value gauge needs no hit-testing (like `textureQuad`); an
 * *interactive* custom widget would additionally call `hitTest`/`tryClaimActive`/
 * `releaseActiveIfMatches`/`widgetState`, the same primitives `button`/`toggle`/`slider` use.
 */
fun UiScope.gauge(
    value: Float,
    width: Float = 120f,
    height: Float = 12f,
    modifier: UiModifier = UiModifier(),
    trackColor: FloatArray = floatArrayOf(0.2f, 0.2f, 0.2f, 1f),
    fillColor: FloatArray = floatArrayOf(0.3f, 0.7f, 0.3f, 1f)
) {
    val slot = claimSlot(modifier.width ?: width.toDimension(), modifier.height ?: height.toDimension())
    emit(UiDrawPrimitive.Quad(slot.x, slot.y, slot.width, slot.height, trackColor))
    val fillWidth = slot.width * value.coerceIn(0f, 1f)
    if (fillWidth > 0f) {
        emit(UiDrawPrimitive.Quad(slot.x, slot.y, fillWidth, slot.height, fillColor))
    }
}
