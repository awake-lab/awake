// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.surface as primitiveSurface
import io.github.ronjunevaldoz.awake.ui.layouts.interactiveSurface as primitiveInteractiveSurface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.styleable
import io.github.ronjunevaldoz.awake.ui.style.Style

/**
 * Generic painted container with Headless layout content.
 *
 * The public surface uses the shared [Style] contract. Theme resolution, drawing, clipping,
 * semantic recording, and measurement remain inside Core's runtime implementation.
 */
fun UiScope.surface(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    verticalArrangement: Arrangement = Arrangement.Start,
    clipContent: Boolean = false,
    // Opt-in cross-frame cache for the WrapContent sizing trial -- see UiScope.column.
    cacheKey: Any? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveSurface(
    id = id,
    modifier = modifier,
    style = style,
    cacheKey = cacheKey,
    verticalArrangement = verticalArrangement.asPrimitiveArrangement(),
    clipContent = clipContent,
) { slot -> content(asHeadlessScope(), slot) }

/**
 * [surface] for a widget built by composing gesture ([Modifier.clickable]) and interaction
 * state onto it, rather than baking click handling into the widget itself -- see the
 * `awake-ui-authoring` skill's "4 independent pieces" note. [semanticRole] lets the caller say
 * what it actually is (Button, Checkbox, ...) instead of the generic Panel [surface] defaults
 * to. Kept as a separate function from [surface] itself, not new params on it -- see
 * [io.github.ronjunevaldoz.awake.ui.layouts.interactiveSurface]'s own doc for why.
 */
fun UiScope.interactiveSurface(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    verticalArrangement: Arrangement = Arrangement.Start,
    clipContent: Boolean = false,
    semanticRole: UiSemanticRole = UiSemanticRole.Panel,
    cacheKey: Any? = null,
    // Unlike [surface] (branded card-shaped baseline), this has NO default reach into the
    // ambient theme -- ui-headless does not read Local*/theme state itself (see the
    // `awake-ui-authoring` skill). [style] is the caller's complete, self-sufficient answer;
    // an interactive widget composes its own themed defaults INTO [style] before calling this.
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = primitive.primitiveInteractiveSurface(
    id = id,
    modifier = modifier.styleable(style),
    cacheKey = cacheKey,
    verticalArrangement = verticalArrangement.asPrimitiveArrangement(),
    clipContent = clipContent,
    semanticRole = semanticRole,
    defaults = Style.Empty,
) { slot -> content(asHeadlessScope(), slot) }
