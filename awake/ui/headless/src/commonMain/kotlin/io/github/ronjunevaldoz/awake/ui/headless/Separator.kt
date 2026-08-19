// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.style.Style

enum class UiSeparatorOrientation { Horizontal, Vertical }

/**
 * Neutral separator primitive. Reads no ambient theme -- [style] is the caller's complete,
 * self-sufficient answer, same as every other Headless widget (see the `awake-ui-authoring`
 * skill's "headless consumes Style, not a theme recipe" rule). Every real caller either goes
 * through [io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSeparator] (which
 * supplies its own color) or passes [style]/[color] directly; a bare, unstyled `separator()`
 * paints nothing, the same "caller's job to style it" contract [interactiveSurface] documents.
 */
fun UiScope.separator(
    id: String,
    modifier: UiModifier = Modifier,
    thickness: Dp = 1f.dp,
    orientation: UiSeparatorOrientation = UiSeparatorOrientation.Horizontal,
    style: Style = Style.Empty,
): UiBounds {
    val sepModifier = when (orientation) {
        UiSeparatorOrientation.Horizontal -> modifier.fillMaxWidth().height(thickness)
        UiSeparatorOrientation.Vertical -> modifier.fillMaxHeight().width(thickness)
    }
    return surface(
        id = id,
        modifier = sepModifier,
        // A separator is always a straight edge, never a rounded pill -- structural to what
        // this widget is, not something a caller-supplied style should be able to accidentally
        // drop by simply not mentioning shape (surface()'s own baseline shape is rounded).
        style = Style { shape(UiShape.none) } then style,
    ) { _ -> }
}

/** Compatibility bridge for callers that have not moved their separator colour into [Style]. */
@Deprecated("Use style = Style { background(color) }")
fun UiScope.separator(
    id: String,
    modifier: UiModifier = Modifier,
    thickness: Dp = 1f.dp,
    orientation: UiSeparatorOrientation = UiSeparatorOrientation.Horizontal,
    color: Color,
): UiBounds = separator(
    id = id,
    modifier = modifier,
    thickness = thickness,
    orientation = orientation,
    style = Style { background(color); shape(UiShape.none) },
)
