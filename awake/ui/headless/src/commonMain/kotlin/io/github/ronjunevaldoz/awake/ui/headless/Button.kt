// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.internal.layout.withIntrinsicLabelWidth
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.withGraphicsLayerAlpha

/**
 * Style-native button API. State rules belong in [style], not in a parallel visual DTO.
 *
 * A thin wrapper over the slot-form [button] below -- [label] draws through the exact same
 * [interactiveSurface]/[row] machinery any content-lambda caller gets, via a content lambda that
 * just renders [text]. The two previously used independently-drifted paths (this overload's own
 * `primitiveButton`/`buttonSlotInternal` route vs. the slot form's `interactiveSurface`), which
 * is what let a resolved-style/hover bug live in only one of them; see `surfaceCore`'s doc for
 * the fix that made them equivalent.
 *
 * [withIntrinsicLabelWidth] resolves the button's own width up front (a real caller width wins
 * unchanged; an unset width becomes the label's measured natural width; a `fillMaxWidth()`
 * caught mid an ancestor's own WrapContent trial -- e.g. a vertical shadcnButtonGroup's members
 * -- reports that same natural width for the trial only, same as before). That leaves the
 * button's width fully resolved (never [io.github.ronjunevaldoz.awake.ui.api.layout.Dimension.WrapContent])
 * by the time the slot form's own row/text render, so [label] can always claim the full content
 * width there -- letting [text]'s own [centered] flag (not the row's fixed `Arrangement.Center`,
 * see the slot form's doc) decide where the label draws.
 */
fun UiScope.button(
    id: String,
    label: String? = null,
    modifier: Modifier = Modifier,
    style: Style = Style.Empty,
    centered: Boolean = true,
    enabled: Boolean = true,
    semanticRole: UiSemanticRole = UiSemanticRole.Button,
): Boolean {
    val sizedModifier = if (label != null) {
        primitive.withIntrinsicLabelWidth(
            modifier = modifier.asPrimitiveModifier(),
            label = label,
            style = style,
            defaults = Style { shape(0f.dp) },
        ).toHeadless()
    } else {
        modifier
    }
    return button(
        id = id,
        modifier = sizedModifier,
        style = style,
        enabled = enabled,
        semanticRole = semanticRole,
    ) {
        if (label != null) {
            text(
                label = label,
                modifier = Modifier.fillMaxSize(),
                centered = centered,
                overflow = UiTextOverflow.Ellipsis,
                semanticId = "$id.label",
            )
        }
    }
}

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
        button(
            id = id,
            label = label,
            modifier = modifier,
            style = style,
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
