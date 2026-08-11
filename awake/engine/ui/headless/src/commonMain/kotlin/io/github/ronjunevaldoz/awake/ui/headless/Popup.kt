// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.UiPopupDefaults as PrimitivePopupDefaults
import io.github.ronjunevaldoz.awake.ui.api.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.api.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.api.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.defaultArrangement
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.popup as primitivePopup

/**
 * Standard generic popup placement policies.
 *
 * This is the public Headless home for popup behavior defaults. Core's implementation remains
 * the runtime bridge during the migration; callers never need its type or package.
 */
object UiPopupDefaults {
    fun aligned(
        anchorAlignment: UiAlignment = UiAlignment.BottomStart,
        popupAlignment: UiAlignment = UiAlignment.TopStart,
        offsetX: Dp = Dp(0f),
        offsetY: Dp = Dp(0f),
    ): UiPopupPositionProvider = PrimitivePopupDefaults.aligned(
        anchorAlignment = anchorAlignment,
        popupAlignment = popupAlignment,
        offsetX = offsetX,
        offsetY = offsetY,
    )

    fun dropdown(
        offsetX: Dp = Dp(0f),
        offsetY: Dp = Dp(0f),
    ): UiPopupPositionProvider = PrimitivePopupDefaults.dropdown(offsetX = offsetX, offsetY = offsetY)

    fun centered(): UiPopupPositionProvider = PrimitivePopupDefaults.centered()

    fun popover(
        offsetX: Dp = Dp(0f),
        offsetY: Dp = 4.dp,
    ): UiPopupPositionProvider = PrimitivePopupDefaults.popover(offsetX = offsetX, offsetY = offsetY)
}

/**
 * Composes generic popup behavior through the public Headless receiver.
 *
 * Core retains measurement, animation, placement, and input processing; this façade deliberately
 * exposes only stable contracts and a Headless [ColumnScope] content receiver. More rendering
 * configuration will move here only after its types have equally narrow ownership.
 */
fun UiScope.popup(
    anchorSlot: UiBounds,
    expanded: Boolean,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    positionProvider: UiPopupPositionProvider = UiPopupDefaults.dropdown(),
    properties: UiPopupProperties = UiPopupProperties(),
    id: String? = null,
    fadeDurationMs: Float = 150f,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiPopupResult = primitive.primitivePopup(
    anchorSlot = anchorSlot,
    expanded = expanded,
    width = width,
    height = height,
    verticalArrangement = defaultArrangement(),
    modifier = Modifier,
    positionProvider = positionProvider,
    properties = properties,
    id = id,
    fadeDurationMs = fadeDurationMs,
) { slot ->
    content(asHeadlessScope(), slot)
}
