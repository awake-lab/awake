// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled

import io.github.ronjunevaldoz.awake.ui.Dimension
import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.Style
import io.github.ronjunevaldoz.awake.ui.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.core.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.basicText

private val AVATAR_DEFAULT_DIAMETER = 40f.dp

/**
 * Real shadcn's `Avatar` renders an image with an `AvatarFallback` (initials) shown while the
 * image loads or on error. Awake has no image-loading pipeline wired into this rasterizer yet
 * -- this is the fallback-only look, a real gap worth closing once Awake has a general
 * async-image primitive, not something to fake here.
 */
fun UiScope.avatarFallback(
    initials: String,
    modifier: UiModifier = UiModifier(),
    diameter: Dp = AVATAR_DEFAULT_DIAMETER,
    style: Style = Style.Empty
) {
    val resolved = resolveStyle(style = style, defaults = theme.components.checkbox)
    val slot = claimModifiedSlot(
        defaultWidth = Dimension.Fixed(diameter),
        defaultHeight = Dimension.Fixed(diameter),
        modifier = modifier
    )
    emitFillAndBorder(
        slot = slot,
        fillColor = resolved.background ?: theme.tokens.muted,
        radiusPx = 0f,
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: theme.tokens.border,
        shapeSpec = UiShapeSpec.Circle
    )
    if (font != null) {
        basicText(initials, slot = slot, font = font, color = resolved.foreground ?: theme.tokens.foreground, centered = true)
    }
}
