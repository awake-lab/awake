// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.popup

import io.github.ronjunevaldoz.awake.ui.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.popup
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme

fun UiScope.shadcnTooltip(
    anchorSlot: UiBounds,
    visible: Boolean,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    positionProvider: UiPopupPositionProvider = UiPopupDefaults.aligned(
        anchorAlignment = UiAlignment.BottomCenter,
        popupAlignment = UiAlignment.TopCenter,
        offsetY = theme.asShadcnTheme().spacing.xs,
    ),
    properties: UiPopupProperties = UiPopupProperties(),
    style: Style = Style.Empty,
    // Also used as the popup's own id so multiple tooltips rendered together (e.g. a preview
    // page open-state proof) don't collide on the same "tooltip" semantic id.
    id: String = "tooltip",
    content: ColumnScope.(slot: UiBounds) -> Unit,
): UiPopupResult = popup(
    id = id,
    anchorSlot = anchorSlot,
    expanded = visible,
    width = width,
    height = height,
    verticalArrangement = Arrangement.spacedBy(0f.dp),
    positionProvider = positionProvider,
    properties = properties,
) { _ ->
    val shadcnTheme = theme.asShadcnTheme()
    // Real shadcn's TooltipContent is `rounded-md bg-foreground px-3 py-1.5 text-xs
    // text-background` -- a solid, inverted-color pill, not a card. This intentionally does NOT
    // route through `theme.components.surface` (unlike the previous version, whose own
    // `shape(UiShape.sm)` rule was dead code -- `components.surface`'s later `shape`/
    // `background`/`borderWidth` rules always overwrote it; see
    // skills/awake-shadcn-styling/SKILL.md's Style.then note).
    surface(
        id = id,
        style = Style {
            background(shadcnTheme.colors.foreground, tokenId = "foreground")
            foreground(shadcnTheme.colors.background, tokenId = "background")
            borderWidth(0f.dp)
            shape(shadcnTheme.radii.md)
            contentPadding(12f.dp, 6f.dp)
            textSize(shadcnTheme.typography.caption, tokenId = "caption")
        } then style,
        modifier = Modifier.width(width).height(height),
    ) { slot ->
        content(slot)
    }
}
