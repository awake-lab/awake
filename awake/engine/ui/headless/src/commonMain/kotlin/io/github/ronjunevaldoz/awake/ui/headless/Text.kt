// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow as PrimitiveTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextWrap as PrimitiveTextWrap
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text as primitiveText

enum class UiTextWrap { None, Word }
enum class UiTextOverflow { Visible, Clip, Ellipsis }

private fun UiTextWrap.asPrimitive(): PrimitiveTextWrap = when (this) {
    UiTextWrap.None -> PrimitiveTextWrap.None
    UiTextWrap.Word -> PrimitiveTextWrap.Word
}

private fun UiTextOverflow.asPrimitive(): PrimitiveTextOverflow = when (this) {
    UiTextOverflow.Visible -> PrimitiveTextOverflow.Visible
    UiTextOverflow.Clip -> PrimitiveTextOverflow.Clip
    UiTextOverflow.Ellipsis -> PrimitiveTextOverflow.Ellipsis
}

/** Neutral text primitive; typography and color remain caller-provided visual decisions. */
fun UiScope.text(
    label: String,
    modifier: Modifier = Modifier,
    visuals: SurfaceStyle = SurfaceStyle(),
    color: Color? = null,
    centered: Boolean = false,
    wrap: UiTextWrap = UiTextWrap.None,
    overflow: UiTextOverflow = UiTextOverflow.Visible,
    maxLines: Int = if (wrap == UiTextWrap.None) 1 else Int.MAX_VALUE,
    semanticId: String? = null,
): UiBounds = primitive.primitiveText(
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    color = color,
    centered = centered,
    wrap = wrap.asPrimitive(),
    overflow = overflow.asPrimitive(),
    maxLines = maxLines,
    semanticId = semanticId,
)

fun ColumnScope.text(
    label: String,
    modifier: Modifier = Modifier,
    visuals: SurfaceStyle = SurfaceStyle(),
    color: Color? = null,
    centered: Boolean = false,
    wrap: UiTextWrap = UiTextWrap.None,
    overflow: UiTextOverflow = UiTextOverflow.Visible,
    maxLines: Int = if (wrap == UiTextWrap.None) 1 else Int.MAX_VALUE,
    semanticId: String? = null,
): UiBounds = primitive.primitiveText(
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    color = color,
    centered = centered,
    wrap = wrap.asPrimitive(),
    overflow = overflow.asPrimitive(),
    maxLines = maxLines,
    semanticId = semanticId,
)

fun RowScope.text(
    label: String,
    modifier: Modifier = Modifier,
    visuals: SurfaceStyle = SurfaceStyle(),
    color: Color? = null,
    centered: Boolean = false,
    wrap: UiTextWrap = UiTextWrap.None,
    overflow: UiTextOverflow = UiTextOverflow.Visible,
    maxLines: Int = if (wrap == UiTextWrap.None) 1 else Int.MAX_VALUE,
    semanticId: String? = null,
): UiBounds = primitive.primitiveText(
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    color = color,
    centered = centered,
    wrap = wrap.asPrimitive(),
    overflow = overflow.asPrimitive(),
    maxLines = maxLines,
    semanticId = semanticId,
)

fun BoxScope.text(
    label: String,
    modifier: Modifier = Modifier,
    visuals: SurfaceStyle = SurfaceStyle(),
    color: Color? = null,
    centered: Boolean = false,
    wrap: UiTextWrap = UiTextWrap.None,
    overflow: UiTextOverflow = UiTextOverflow.Visible,
    maxLines: Int = if (wrap == UiTextWrap.None) 1 else Int.MAX_VALUE,
    semanticId: String? = null,
): UiBounds = primitive.primitiveText(
    label = label,
    modifier = modifier.asPrimitiveModifier(),
    style = visuals.asPrimitiveStyle(),
    color = color,
    centered = centered,
    wrap = wrap.asPrimitive(),
    overflow = overflow.asPrimitive(),
    maxLines = maxLines,
    semanticId = semanticId,
)
