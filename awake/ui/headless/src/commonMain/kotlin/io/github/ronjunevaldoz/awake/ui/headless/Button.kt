// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.withGraphicsLayerAlpha
import io.github.ronjunevaldoz.awake.ui.headless.button as primitiveButton

/**
 * Style-native button API. State rules belong in [style], not in a parallel visual DTO.
 *
 * [label] draws through the same primitive slot machinery as the trailing-lambda overload below
 * ([io.github.ronjunevaldoz.awake.ui.headless.internal.controls.buttonSlot]'s label-aware
 * overload) rather than composing `button(id) { text(label) }` at this layer: doing the
 * composition here needs to re-resolve the button's own style (for the label's weight/size) a
 * second time, and that second resolution silently drifted from what buttonSlotInternal itself
 * computes -- a real visible regression (labels rendered smaller) caught by
 * `ui-awake-shadcn-showcase`'s snapshot signature, not a false positive. Delegating to the
 * primitive keeps exactly one place that resolves a button's text style.
 */
fun UiScope.button(
    id: String,
    label: String? = null,
    modifier: Modifier = Modifier,
    style: Style = Style.Empty,
    centered: Boolean = true,
    enabled: Boolean = true,
    semanticRole: UiSemanticRole = UiSemanticRole.Button,
): Boolean = primitive.primitiveButton(
    id = id,
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = style,
    radius = 0.dp,
    centered = centered,
    enabled = enabled,
    semanticRole = semanticRole,
)

/** Callback-oriented button API for application composition. */
fun UiScope.button(
    id: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: Style = Style.Empty,
    centered: Boolean = true,
    enabled: Boolean = true,
    semanticRole: UiSemanticRole = UiSemanticRole.Button,
) {
    if (
        primitive.primitiveButton(
            id = id,
            label = label,
            modifier = modifier.asPrimitiveModifier(),
            style = style,
            radius = 0.dp,
            centered = centered,
            enabled = enabled,
            semanticRole = semanticRole,
        )
    ) onClick()
}

/**
 * Slot variant of the Style-native button API.
 *
 * Built on [interactiveSurface] wrapping a [row] -- gesture ([Modifier.clickable]) and the
 * visual container are composed, not baked into a button-specific primitive (see the
 * `awake-ui-authoring` skill's "4 independent pieces" note). This is deliberately the same
 * `Surface { Row { content } }` shape Jetpack Compose's own `Button` is built from -- centered
 * content is `Arrangement.Center` + `Vertical.Center` on that row, not a `Box`.
 *
 * Reads no ambient theme -- `ui-headless` must not (see the `awake-ui-authoring` skill's "headless
 * consumes Style, not a theme recipe" rule). [style] is the caller's complete, self-sufficient
 * answer; `shape(0)` is the only default, a genuinely neutral value, not a theme lookup. Every
 * real caller (`shadcnButton` and friends) already supplies a complete themed [Style], so this
 * was previously reading `theme.components.button` and then immediately being overridden by it
 * anyway -- dead weight, not a real fallback.
 */
fun UiScope.button(
    id: String,
    modifier: Modifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    semanticRole: UiSemanticRole = UiSemanticRole.Button,
    content: RowScope.(slot: UiBounds) -> Unit,
): Boolean {
    var clicked = false
    primitive.withGraphicsLayerAlpha(if (enabled) 1f else 0.5f) {
        interactiveSurface(
            id = id,
            modifier = modifier
                .fillMaxWidthOrDefault()
                .heightOrDefault(40f.dp)
                .clickable(enabled) { clicked = true },
            style = Style { shape(0f.dp) } then style,
            semanticRole = semanticRole,
        ) { slot ->
            row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = UiAlignment.Vertical.Center,
            ) {
                content(slot)
            }
        }
    }
    return clicked
}
