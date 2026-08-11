// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text as primitiveText

/** Neutral text primitive; typography and color remain caller-provided visual decisions. */
fun UiScope.text(
    label: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
    centered: Boolean = false,
    wrap: UiTextWrap = UiTextWrap.None,
    overflow: UiTextOverflow = UiTextOverflow.Visible,
    maxLines: Int = if (wrap == UiTextWrap.None) 1 else Int.MAX_VALUE,
    semanticId: String? = null,
): UiBounds = primitive.primitiveText(
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    color = color,
    centered = centered,
    wrap = wrap,
    overflow = overflow,
    maxLines = maxLines,
    semanticId = semanticId,
)

fun ColumnScope.text(
    label: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
    centered: Boolean = false,
    wrap: UiTextWrap = UiTextWrap.None,
    overflow: UiTextOverflow = UiTextOverflow.Visible,
    maxLines: Int = if (wrap == UiTextWrap.None) 1 else Int.MAX_VALUE,
    semanticId: String? = null,
): UiBounds = primitive.primitiveText(
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    color = color,
    centered = centered,
    wrap = wrap,
    overflow = overflow,
    maxLines = maxLines,
    semanticId = semanticId,
)
