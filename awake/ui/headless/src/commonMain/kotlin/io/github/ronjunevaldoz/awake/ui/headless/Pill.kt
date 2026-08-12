// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.headless.internal.withIntrinsicLabelSize

/** Neutral compact surface for badges, labels, and key-cap-like content. */
fun UiScope.pill(
    id: String,
    label: String,
    modifier: Modifier = Modifier,
    style: SurfaceStyle = SurfaceStyle(),
    textColor: Color? = style.foreground,
): UiBounds = surface(
    id = id,
    modifier = HeadlessModifier(
        primitive.withIntrinsicLabelSize(
            modifier = modifier.wrapContentWidth().asPrimitiveModifier(),
            label = label,
            style = style.asPrimitiveStyle(),
        ),
    ),
    style = style,
    verticalArrangement = Arrangement.Center,
    clipContent = true,
) {
    text(label = label, color = textColor, centered = true)
}