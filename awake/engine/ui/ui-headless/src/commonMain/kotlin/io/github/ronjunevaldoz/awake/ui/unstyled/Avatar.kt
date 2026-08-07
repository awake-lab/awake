// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShapeSpec
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.withSizeFallback
import io.github.ronjunevaldoz.awake.ui.scope.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.scope.resolveStyle
import io.github.ronjunevaldoz.awake.ui.style.Style

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
 *
 * Size is authored via `modifier` (`.size(...)`/`.width()`/`.height()`), not a separate
 * `diameter` param -- `UiModifier` already owns sizing, per the modifier-first policy.
 */
fun UiScope.avatarFallback(
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    content: BoxScope.(slot: UiBounds) -> Unit,
) {
    val theme = context.currentTheme
    val resolved = resolveStyle(style = style, defaults = theme.components.avatar)
    val slot = claimModifiedSlot(
        modifier.withSizeFallback(
            Dimension.Fixed(AVATAR_DEFAULT_DIAMETER),
            Dimension.Fixed(AVATAR_DEFAULT_DIAMETER),
        ),
    )
    emitFillAndBorder(
        slot = slot,
        fillColor = resolved.background ?: theme.colors.muted,
        radiusPx = 0f,
        borderWidth = resolved.borderWidth,
        borderColor = resolved.borderColor ?: theme.colors.border,
        shapeSpec = UiShapeSpec.Circle,
    )
    childBox(slot).content(slot)
    recordSemantic(
        role = UiSemanticRole.Avatar,
        id = modifier.testTag,
        bounds = slot,
    )
}

/**
 * [avatarFallback] convenience that renders plain initials text as the content. Threads
 * [style]'s resolved [io.github.ronjunevaldoz.awake.ui.theme.TextStyle] (e.g. a caller's
 * `textSize(...)`) into the initials draw call -- previously dropped on the floor here, so a
 * caller-supplied text size (e.g. a size-driven `shadcnAvatar`) had no effect on the rendered
 * initials.
 */
fun UiScope.avatarFallback(
    initials: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
) {
    val theme = context.currentTheme
    val resolved = resolveStyle(style = style, defaults = theme.components.avatar)
    avatarFallback(modifier = modifier, style = style) { slot ->
        text(
            initials,
            slot = slot,
            font = context.currentFont,
            color = resolved.foreground ?: theme.colors.foreground,
            textStyle = resolved.textStyle,
            centered = true,
        )
    }
}
