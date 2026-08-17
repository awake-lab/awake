// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.headless.button as primitiveButton
import io.github.ronjunevaldoz.awake.ui.headless.buttonSlot as primitiveButtonSlot

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

/** Slot variant of the Style-native button API. */
fun UiScope.button(
    id: String,
    modifier: Modifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    semanticRole: UiSemanticRole = UiSemanticRole.Button,
    content: BoxScope.(slot: UiBounds) -> Unit,
): Boolean = primitive.primitiveButtonSlot(
    id = id,
    modifier = modifier.asPrimitiveModifier(),
    style = style,
    enabled = enabled,
    semanticRole = semanticRole,
) { slot ->
    BoxScope(primitive.childBox(slot, contentAlignment = UiAlignment.Center)).content(slot)
}.clicked
