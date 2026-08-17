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
 *
 * Defaults to hugging [content] ([wrapContentWidthOrDefault], not `fillMaxWidthOrDefault`) --
 * a button has no intrinsic reason to claim its parent's full width, and every real caller that
 * wants that (shadcnSidebar's menu items) already opts in with its own explicit
 * `modifier.fillMaxWidth()` before calling this. `fillMaxWidthOrDefault()` here meant a
 * content-lambda button with no caller-supplied width (shadcnButton's icon+label form, e.g.
 * Studio's top-bar Save/Play) resolved FillMax against whatever ambient trial bound happened to
 * be live -- inside a WrapContent-sizing row that bound is the measurement sentinel
 * ([io.github.ronjunevaldoz.awake.ui.context.UNBOUNDED_MAIN_AXIS], 100000px), so the button
 * silently baked in a ~100000px width and rendered off past the right edge instead of drawing
 * its label.
 *
 * The inner centering row mirrors that same caller-sized-vs-not split, not an unconditional
 * `fillMaxSize()`: `interactiveSurface`'s own WrapContent measurement trial (the no-explicit-
 * width case above) has to run THIS row to find out how wide the button should be -- a
 * `fillMaxWidth()` row inside that still-being-measured trial forces it to fill the trial's
 * bound instead (the same sentinel), and `Arrangement.Center` then off-centers the real content
 * deep inside it, which is what fed the sentinel-tainted width back into the surface's own
 * "how wide am I" answer instead of excluding it. A caller-sized button has a real bound
 * already (no trial), so `fillMaxSize()` for centering shorter content within it is safe there.
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
    val hasExplicitWidth = modifier.asPrimitiveModifier().widthDimension != null
    primitive.withGraphicsLayerAlpha(if (enabled) 1f else 0.5f) {
        interactiveSurface(
            id = id,
            modifier = modifier
                .wrapContentWidthOrDefault()
                .heightOrDefault(40f.dp)
                .clickable(enabled) { clicked = true },
            style = Style { shape(0f.dp) } then style,
            semanticRole = semanticRole,
        ) { slot ->
            row(
                modifier = if (hasExplicitWidth) {
                    Modifier.fillMaxSize()
                } else {
                    Modifier.fillMaxHeight().wrapContentWidth()
                },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = UiAlignment.Vertical.Center,
            ) {
                content(slot)
            }
        }
    }
    return clicked
}
