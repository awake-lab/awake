// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

// Real shadcn's TabsList is a muted rounded track; the active TabsTrigger gets a raised
// card-colored background, inactive ones are chromeless labels. Composed from
// shadcnButton the same way shadcnRadioGroup composes from checkbox(): reuse the
// existing variant/style system rather than a new low-level widget.
/** Real shadcn's `Tabs`: a muted track of buttons where the [selectedIndex] gets a raised
 * card-colored background and the rest stay chromeless until hover. Composed from
 * [shadcnButton]. */
fun ColumnScope.shadcnTabs(
    id: String,
    tabs: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = Modifier,
    tabWidth: Dp = 96f.dp,
    height: Dp = 32f.dp
): Int {
    var resolved = selectedIndex
    val shadcnTheme = theme.asShadcnTheme()
    surface(
        id = "$id.track",
        modifier = (modifier).copy(
            widthDimension = modifier.widthDimension ?: Dimension.WrapContent,
            heightDimension = Dimension.Fixed(height)
        ),
        style = Style {
            shape(shadcnTheme.radii.md)
            background(shadcnTheme.palette.muted)
        }
    ) {
        row(
            horizontalArrangement = Arrangement.spacedBy(2f.dp),
            modifier = Modifier.height(height)
        ) {
            tabs.forEachIndexed { index, label ->
                val active = index == selectedIndex
                val tabStyle: Style = if (active) {
                    Style {
                        background(shadcnTheme.card)
                        foreground(shadcnTheme.tokens.foreground)
                    }
                } else {
                    Style { foreground(shadcnTheme.tokens.mutedForeground) }
                }
                val tabHeight: Dp = (height.value - 4f).dp
                val tabModifier = UiModifier(
                    widthDimension = Dimension.Fixed(tabWidth),
                    heightDimension = Dimension.Fixed(tabHeight)
                )
                // UiButtonVariant.Ghost's resolveFill hardcodes fill to transparent unless
                // hovered/active, ignoring any style override -- so the active tab (which must
                // show its card-colored background at rest, not just on hover) uses Primary
                // (-> UiButtonVariant.Filled, which always honors the resolved background)
                // with that background/foreground overridden by tabStyle; inactive tabs stay
                // Ghost for the real chromeless-until-hover look.
                val clicked = shadcnButton(
                    id = "$id.$index",
                    label = label,
                    modifier = tabModifier,
                    variant = if (active) ShadcnButtonVariant.Primary else ShadcnButtonVariant.Ghost,
                    style = tabStyle
                )
                if (clicked) resolved = index
            }
        }
    }
    return resolved
}
