// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.modifier

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiScrollConfig
import io.github.ronjunevaldoz.awake.ui.UiScrollState
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.style.Style

/**
 * Per-widget-call structural override only -- width/height are layout concerns, while fill,
 * shape, border, text scale, and padding now belong to [Style]. That keeps this type closer
 * to real Compose's "modifier is structure/behavior, style is visuals" split, which matters
 * once consumer-authored widgets and composite containers start reusing the same style stack
 * as built-ins.
 */
data class UiModifier(
    val width: Dimension? = null,
    val height: Dimension? = null,
    val testTag: String? = null,
    val alignment: UiAlignment? = null,
    val offsetX: Dp = UiShape.none,
    val offsetY: Dp = UiShape.none,
    val insets: UiInsets = UiInsets.Zero,
    val forceHover: Boolean? = null,
    val forceActive: Boolean? = null,
    val forceFocus: Boolean? = null,
    val scrollState: UiScrollState? = null,
    val scrollConfig: UiScrollConfig = UiScrollConfig.Default,
    val graphicsLayer: UiGraphicsLayer? = null,
    val styleable: Style? = null
)

val Modifier: UiModifier
    get() = UiModifier()
