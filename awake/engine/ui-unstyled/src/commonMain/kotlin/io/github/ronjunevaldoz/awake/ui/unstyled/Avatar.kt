// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.unstyled

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.core.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.resolveStyle
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

private val AVATAR_DEFAULT_DIAMETER = 40f.dp

/**
 * Real shadcn's `Avatar` renders an image with an `AvatarFallback` (initials) shown while the
 * image loads or on error. Awake has no image-loading pipeline wired into this rasterizer yet
 * -- this is the fallback-only look, a real gap worth closing once Awake has a general
 * async-image primitive, not something to fake here.
 *
 * Structure only (the circle) -- content is caller-supplied via the slot lambda, per the Text
 * ownership rule (docs/reference/ui-ownership.md): a reusable API must not own both the
 * container structure and all displayed content.
 */
fun UiScope.avatarFallback(
    modifier: UiModifier = Modifier,
    diameter: Dp = AVATAR_DEFAULT_DIAMETER,
    style: Style = Style.Empty,
    content: BoxScope.(slot: UiSlot) -> Unit
) {
    val theme = context.currentTheme
    val resolved = resolveStyle(style = style, defaults = theme.components.avatar)
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
    childBox(slot).content(slot)
}

/** [avatarFallback] convenience that renders plain initials text as the content. */
fun UiScope.avatarFallback(
    initials: String,
    modifier: UiModifier = Modifier,
    diameter: Dp = AVATAR_DEFAULT_DIAMETER,
    style: Style = Style.Empty
) {
    val theme = context.currentTheme
    val resolved = resolveStyle(style = style, defaults = theme.components.avatar)
    avatarFallback(modifier = modifier, diameter = diameter, style = style) { slot ->
        text(
            initials,
            slot = slot,
            font = context.currentFont,
            color = resolved.foreground ?: theme.tokens.foreground,
            centered = true
        )
    }
}
