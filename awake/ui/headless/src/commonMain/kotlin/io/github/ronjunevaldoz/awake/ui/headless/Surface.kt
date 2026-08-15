// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.surface as primitiveSurface
import io.github.ronjunevaldoz.awake.ui.style.Style

/**
 * Generic painted container with Headless layout content.
 *
 * The public surface uses the shared [Style] contract. Theme resolution, drawing, clipping,
 * semantic recording, and measurement remain inside Core's runtime implementation.
 */
fun UiScope.surface(
    id: String,
    modifier: Modifier = Modifier,
    style: Style = Style.Empty,
    verticalArrangement: Arrangement = Arrangement.Start,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveSurface(
    id = id,
    modifier = modifier.asPrimitiveModifier(),
    style = style,
    verticalArrangement = verticalArrangement.asPrimitiveArrangement(),
    clipContent = clipContent,
) { slot -> content(asHeadlessScope(), slot) }

/** Compatibility overload while callers migrate to [Style]. */
@Deprecated("Use the Style overload", ReplaceWith("surface(id, modifier, style.asStyle(), verticalArrangement, clipContent, content)"))
fun UiScope.surface(
    id: String,
    modifier: Modifier = Modifier,
    style: SurfaceStyle,
    verticalArrangement: Arrangement = Arrangement.Start,
    clipContent: Boolean = false,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = surface(id, modifier, style.asPrimitiveStyle(), verticalArrangement, clipContent, content)
