// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.api.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.api.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.interact

/**
 * Unstyled tooltip primitive with hover-intent delay timer state and popup anchor positioning.
 */
fun UiScope.tooltip(
    id: String,
    tooltipText: String,
    delayMs: Float = 300f,
    modifier: Modifier = Modifier,
    surfaceStyle: SurfaceStyle = SurfaceStyle(contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(8f.dp, 4f.dp)),
    trigger: UiScope.() -> UiBounds,
): UiPopupResult {
    val anchorBounds = trigger()
    val interaction = primitive.interact(id = "$id.trigger", modifier = modifier.asPrimitiveModifier())
    val hoverTimer = rememberStateValue(id, "hoverTimer") { 0f }

    val isHovered = interaction.hovered
    if (isHovered) {
        hoverTimer.value = hoverTimer.value + primitive.context.frameDeltaSeconds() * 1000f
    } else {
        hoverTimer.value = 0f
    }

    val isVisible = hoverTimer.value >= delayMs

    return popup(
        id = "$id.popup",
        anchorSlot = anchorBounds,
        expanded = isVisible,
        positionProvider = tooltipPositionProvider(),
        properties = UiPopupProperties(clippingEnabled = false, dismissOnClickOutside = false),
    ) { _ ->
        surface(
            id = "$id.surface",
            style = surfaceStyle,
        ) {
            text(label = tooltipText, centered = true)
        }
    }
}

private fun tooltipPositionProvider() = UiPopupPositionProvider { anchor, frame, _ ->
    val tooltipWidth = 120f
    val tooltipHeight = 32f
    val x = (anchor.x + anchor.width / 2f - tooltipWidth / 2f).coerceIn(0f, frame.width - tooltipWidth)
    val y = (anchor.y - tooltipHeight - 4f).coerceAtLeast(0f)
    UiBounds(x, y, tooltipWidth, tooltipHeight)
}
