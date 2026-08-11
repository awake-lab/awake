// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.navigation

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.font.measureTextWidth
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.scope.resolveGlyphPx
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.tailwind.Tw
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.toPx
import kotlin.math.ceil

/** One tab in a [shadcnTabs] track. Mirrors shadcn-compose's `ShadcnTabItem`: [value] is the
 * stable selection identity, [label] is what's drawn -- two tabs can share a [label] without
 * colliding, since selection keys off [value]. */
data class ShadcnTabItem(val value: String, val label: String)

// Real shadcn's TabsList is a muted rounded track; the active TabsTrigger gets a raised
// card-colored background, inactive ones are chromeless labels. Composed from
// shadcnButton the same way shadcnRadioGroup composes from checkbox(): reuse the
// existing variant/style system rather than a new low-level widget.
/** Real shadcn's `Tabs`: a muted track of buttons where the tab matching [selected] gets a
 * raised card-colored background and the rest stay chromeless until hover. Composed from
 * [shadcnButton]. Each tab hugs its own [ShadcnTabItem.label] content width (real shadcn's
 * `tabs.tsx` "default" variant never forces a shared fixed width across triggers) -- so the
 * active tab's raised background resizes to whichever tab is actually selected, rather than
 * assuming every tab is the same width. Returns the resolved selected [ShadcnTabItem.value]. */
fun ColumnScope.shadcnTabs(
    id: String,
    items: List<ShadcnTabItem>,
    selected: String,
    modifier: UiModifier = Modifier,
    // shadcn's TabsList is h-9 (36dp) on the new-york-v4 baseline this module targets.
    height: Dp = Tw.Spacing.s9,
): String {
    var resolved = selected
    val shadcnTheme = theme.asShadcnTheme()
    // Content-hugging width: measure each tab's own label at the same text size shadcnButton's
    // Primary/Ghost variants both resolve to (theme.typography.label), then add the same
    // horizontal padding real shadcn's TabsTrigger uses (px-3 / spacing.md on each side). No
    // per-tab contentPadding Style is set below, so this measured width IS the label's padded
    // box -- shadcnButton's centered text() fills it exactly.
    val glyphPx = resolveGlyphPx(textStyle = TextStyle(size = shadcnTheme.typography.label))
    // Real TabsTrigger is `px-2` = 8dp per side. (A stale comment here claimed px-3/16dp --
    // wrong against the current registry source on both counts.) ShadcnButtonSize.Xs is the
    // size whose paddingX IS px-2, so the width measured here and the inset shadcnButton
    // actually applies stay in agreement -- inheriting the default Md (px-4) would inset 16dp
    // per side into a box measured for 8dp and truncate every tab label.
    val tabSize = ShadcnButtonSize.Xs
    val horizontalPaddingPx = tabSize.paddingX.toPx() * 2f
    // Real shadcn's TabsList reserves a p-1 (4px) inset so the active trigger's raised
    // background sits inside the track, not flush against its edges -- previously the row
    // filled the track's full height with no inset, so the active highlight's own rounded
    // corners poked past the track's rounded corners at the top/bottom. Applied via
    // contentPadding on the track's own Style rather than padding() on the nested row,
    // since Surface already exposes this as an explicit content-padding concept distinct
    // from the row's own outer placement.
    // Real TabsList is `p-[3px]` -- an arbitrary value, not the p-1(4dp) scale step.
    val trackInset = 3f.dp
    surface(
        id = "$id.track",
        modifier = (modifier).copy(
            widthDimension = modifier.widthDimension ?: Dimension.WrapContent,
            heightDimension = Dimension.Fixed(height),
        ),
        style = Style {
            shape(shadcnTheme.radii.md)
            background(shadcnTheme.palette.muted, tokenId = "muted")
            contentPadding(trackInset)
        },
    ) {
        row(
            // Real TabsList's default variant has NO gap class -- triggers sit flush, each
            // rounded fill floating inside the shared p-[3px] track. (Only the `line` variant
            // gets gap-1, which this component doesn't render.)
            horizontalArrangement = Arrangement.spacedBy(0f.dp),
            modifier = Modifier.height(Dimension.FillMax),
        ) {
            items.forEachIndexed { index, item ->
                val active = item.value == selected
                val tabStyle: Style = if (active) {
                    Style {
                        background(shadcnTheme.card)
                        foreground(shadcnTheme.colors.foreground)
                    }
                } else {
                    Style { foreground(shadcnTheme.colors.mutedForeground) }
                }
                // ceil: the button subtracts exactly `horizontalPaddingPx` back out, so an
                // unrounded width leaves the label zero sub-pixel slack and the renderer
                // truncates on a fractional remainder.
                val tabWidthPx =
                    ceil(font.measureTextWidth(item.label, glyphPx)) + horizontalPaddingPx
                val tabModifier = UiModifier(
                    widthDimension = Dimension.Fixed(tabWidthPx.px),
                    heightDimension = Dimension.FillMax,
                )
                // UiButtonVariant.Ghost's resolveFill hardcodes fill to transparent unless
                // hovered/active, ignoring any style override -- so the active tab (which must
                // show its card-colored background at rest, not just on hover) uses Primary
                // (-> UiButtonVariant.Filled, which always honors the resolved background)
                // with that background/foreground overridden by tabStyle; inactive tabs stay
                // Ghost for the real chromeless-until-hover look.
                val clicked = shadcnButton(
                    id = "$id.$index",
                    label = item.label,
                    modifier = tabModifier,
                    variant = if (active) ShadcnButtonVariant.Primary else ShadcnButtonVariant.Ghost,
                    size = tabSize,
                    style = tabStyle,
                )
                if (clicked) resolved = item.value
            }
        }
    }
    return resolved
}

/** [shadcnTabs] convenience over plain label strings, where each tab's identity is its own
 * label text (`value == label`) and selection is by index -- source-compatible with call sites
 * that don't need [ShadcnTabItem]'s separate value/label identity. */
fun ColumnScope.shadcnTabs(
    id: String,
    tabs: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = Modifier,
    height: Dp = Tw.Spacing.s9,
): Int {
    val items = tabs.map { ShadcnTabItem(value = it, label = it) }
    val selectedValue = tabs.getOrNull(selectedIndex) ?: tabs.firstOrNull() ?: ""
    val resolvedValue = shadcnTabs(
        id = id,
        items = items,
        selected = selectedValue,
        modifier = modifier,
        height = height,
    )
    return tabs.indexOf(resolvedValue).takeIf { it >= 0 } ?: selectedIndex
}
