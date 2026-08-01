// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

fun UiScope.resolveStyle(
    style: Style = Style.Empty,
    defaults: Style = Style.Empty,
    state: StyleState = MutableStyleState()
): ResolvedStyle = (defaults then style).resolve(state, context.currentTextStyle)

fun UiScope.recordSemantic(
    role: UiSemanticRole,
    bounds: UiBounds,
    id: String? = null,
    label: String? = null,
    contentBounds: UiBounds? = null,
    clippedBounds: UiBounds? = null,
    truncated: Boolean = false,
    lineCount: Int = 0,
    selected: Boolean? = null
) {
    context.recordSemanticInternal(
        UiSemanticNode(
            role = role,
            bounds = bounds,
            id = id,
            label = label,
            contentBounds = contentBounds,
            clippedBounds = clippedBounds,
            truncated = truncated,
            lineCount = lineCount,
            selected = selected
        )
    )
}
