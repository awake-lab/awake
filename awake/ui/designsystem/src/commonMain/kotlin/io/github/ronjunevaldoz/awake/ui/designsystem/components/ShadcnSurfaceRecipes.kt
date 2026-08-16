// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("UnusedParameter")

package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.api.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.api.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnCardSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnCardVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnCardStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnPopoverContentStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnSurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.popup
import io.github.ronjunevaldoz.awake.ui.headless.spacer
import io.github.ronjunevaldoz.awake.ui.headless.surface

fun UiScope.shadcnSurface(
    id: String,
    modifier: Modifier = Modifier,
    variant: ShadcnSurfaceVariant? = null,
    cacheKey: Any? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = shadcnSurfaceStyle(themeValues, shadcnMetrics, variant),
    cacheKey = cacheKey,
    content = content,
)

fun UiScope.shadcnCard(
    id: String,
    modifier: Modifier = Modifier,
    variant: ShadcnCardVariant = ShadcnCardVariant.Default,
    size: ShadcnCardSize = ShadcnCardSize.Default,
    cacheKey: Any? = null,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    body: ColumnScope.(slot: UiBounds) -> Unit,
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = themeValues.shadcnCardStyle(variant, shadcnMetrics),
    verticalArrangement = Arrangement.spacedBy(0f.dp),
    cacheKey = cacheKey,
) {
    if (header != null) {
        header()
        // CardHeader/CardContent are independently padded in shadcn. The compatibility size
        // axis contributes only a small slot gap; it never draws a divider.
        spacer(Modifier.height((24f + size.dividerGapDp).dp))
    }
    body(it)
    if (footer != null) {
        spacer(Modifier.height(size.dividerGapDp.dp))
        footer()
    }
}

fun UiScope.shadcnPopover(
    id: String,
    anchorSlot: UiBounds,
    expanded: Boolean,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    positionProvider: UiPopupPositionProvider = UiPopupDefaults.popover(),
    properties: UiPopupProperties = UiPopupProperties(),
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiPopupResult = popup(
    anchorSlot = anchorSlot,
    expanded = expanded,
    width = width,
    height = height,
    positionProvider = positionProvider,
    properties = properties,
    id = id,
) {
    surface(
        id = "$id.content",
        modifier = Modifier,
        style = shadcnPopoverContentStyle(themeValues, shadcnMetrics),
        content = content,
    )
}
