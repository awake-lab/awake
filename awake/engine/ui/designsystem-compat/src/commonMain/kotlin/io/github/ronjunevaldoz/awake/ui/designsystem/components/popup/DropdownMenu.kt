// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.popup

import io.github.ronjunevaldoz.awake.ui.headless.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.api.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.api.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.headless.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.buttonSlot
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.align
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.styleable
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.popup
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.scope.pixelPerfectPixel
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.textStyle
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.unstyled.components.UiMenuItemWidthInput
import io.github.ronjunevaldoz.awake.ui.unstyled.components.intrinsicMenuWidthPx
import io.github.ronjunevaldoz.awake.ui.unstyled.components.measureMenuItemLayout
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.unstyled.separator
import io.github.ronjunevaldoz.awake.ui.headless.UiMenuEntry
import io.github.ronjunevaldoz.awake.ui.headless.UiMenuItem
import io.github.ronjunevaldoz.awake.ui.headless.UiMenuSeparator
import io.github.ronjunevaldoz.awake.ui.headless.UiScope as HeadlessUiScope
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle as HeadlessSurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceVisuals
import io.github.ronjunevaldoz.awake.ui.headless.menu
import io.github.ronjunevaldoz.awake.ui.headless.menuItem

fun UiScope.shadcnDropdownMenu(
    id: String,
    anchorSlot: UiBounds,
    expanded: Boolean,
    items: List<UiDropdownMenuEntry>,
    selectedIndex: Int? = null,
    width: Dimension = Dimension.Fixed(anchorSlot.width.px),
    height: Dimension = Dimension.WrapContent,
    itemHeight: Float = 32f,
    positionProvider: UiPopupPositionProvider = UiPopupDefaults.dropdown(),
    properties: UiPopupProperties = UiPopupProperties(),
    style: Style = Style.Empty,
    itemStyle: Style = Style.Empty,
): UiDropdownMenuResult {
    // WrapContent cannot shrink this menu on its own. popup() resolves it by measuring the
    // content inside a box the full window wide, and every item below sizes to the width it is
    // handed (`Modifier.width(width.px)`) -- so the measure returns the window width back, and
    // the menu paints edge to edge. Nothing in the chain has an intrinsic width, so supply one:
    // the widest item's own ink plus its padding, floored at shadcn's min-w-32.
    val resolvedWidth = if (width == Dimension.WrapContent) {
        val glyphPx = pixelPerfectPixel(theme.typography.label.toPx().coerceAtLeast(1f)).coerceAtLeast(1f)
        val widthInputs = items.filterIsInstance<UiDropdownMenuItem>().map {
            UiMenuItemWidthInput(label = it.label, trailingLabel = it.trailingLabel, hasIcon = it.icon != null)
        }
        Dimension.Fixed(
            intrinsicMenuWidthPx(
                items = widthInputs,
                font = font,
                glyphPx = glyphPx,
                minWidthPx = MENU_MIN_WIDTH_DP.dp.toPx(),
            ).px,
        )
    } else {
        width
    }
    // Wired the same way every other scrollable surface in this module wires it (see
    // shadcnSidebar's doc comment): a plain Modifier.verticalScroll(state) on the menu surface.
    // Only applied when the caller constrains [height] -- the default WrapContent already grows
    // to fit every item (never needs scroll), and unconditionally attaching a scrollState would
    // route even the WrapContent case through scrollPanel()'s claimSlot(), which cannot resolve
    // WrapContent while it's still inside popup()'s own WrapContent-sizing measure pass.
    val scrollState =
        if (height != Dimension.WrapContent) rememberScrollState("$id.scroll") else null
    var picked: Int? = null
    val popupResult = popup(
        id = id,
        anchorSlot = anchorSlot,
        expanded = expanded,
        width = resolvedWidth,
        height = height,
        verticalArrangement = Arrangement.spacedBy(0f.dp),
        positionProvider = positionProvider,
        properties = properties,
    ) { popupSlot ->
        surface(
            id = "$id.menu",
            verticalArrangement = Arrangement.spacedBy(0f.dp),
            modifier = Modifier
                .width(Dimension.Fixed(popupSlot.width.px))
                .height(height)
                .let { if (scrollState != null) it.verticalScroll(scrollState) else it }
                .styleable(
                    // theme.components.surface's default contentPadding is sized for a Card/
                    // Dialog panel (shadcn's p-6), not a dropdown-menu-content (shadcn's p-1,
                    // 4dp -- handled here entirely by the manual p-1 spacers above/below, since
                    // items already carry their own px-2 horizontal inset -- see
                    // dropdownMenuItem's labelStart/bodyWidth math). Left unzeroed, that inherited
                    // panel padding silently ate into every item's claimed width without
                    // intrinsicMenuWidthPx() (which assumes zero panel-level padding, matching
                    // dropdownMenuItem's own budget) ever knowing about it -- the two go out of
                    // sync and the widest item's label truncates by exactly that padding amount.
                    // Zero it before `style` so a caller-supplied `style` can still override it.
                    theme.components.surface then Style { contentPadding(0f.dp) } then style then Style {
                        // Real shadcn's dropdown-menu-content is rounded-md, drawn from the active
                        // theme's own radius scale (not ui-core's disconnected `UiShape` global) so
                        // switching presets actually changes this popup's corners too.
                        shape(theme.asShadcnTheme().radii.md)
                    },
                ),
            clipContent = true,
        ) {
            // shadcn spec: menu panel has p-1 (4dp) top and bottom padding
            spacer(Modifier.height(4f.dp))
            var actionIndex = 0
            items.forEach { entry ->
                when (entry) {
                    UiDropdownMenuSeparator -> {
                        spacer(Modifier.height(4f.dp))
                        separator(
                            thickness = 1f.dp,
                            color = theme.colors.border.withAlpha(0.72f),
                        )
                        spacer(Modifier.height(4f.dp))
                    }

                    is UiDropdownMenuItem -> {
                        val currentActionIndex = actionIndex
                        val menuItemStyle = when {
                            !entry.enabled -> Style.Companion {
                                foreground(theme.colors.mutedForeground)
                                background(theme.colors.background.withAlpha(0.86f))
                            }

                            currentActionIndex == selectedIndex -> Style.Companion {
                                background(theme.colors.accent)
                                foreground(theme.colors.accentForeground)
                            }

                            entry.destructive -> Style.Companion {
                                foreground(theme.colors.destructive)
                            }

                            else -> Style.Empty
                        }
                        val clicked = dropdownMenuItem(
                            id = "$id.item.$currentActionIndex",
                            item = entry,
                            width = this.width,
                            baseHeight = itemHeight,
                            style = itemStyle then menuItemStyle,
                            selected = currentActionIndex == selectedIndex,
                        )
                        if (clicked && entry.enabled) {
                            picked = currentActionIndex
                        }
                        actionIndex += 1
                    }
                }
            }
            spacer(Modifier.height(4f.dp))
        }
    }
    return UiDropdownMenuResult(
        slot = popupResult.slot,
        selectedIndex = picked,
        dismissed = popupResult.dismissed,
    )
}

/** shadcn's dropdown-menu-content carries `min-w-32`. */
private const val MENU_MIN_WIDTH_DP = 128f

private fun ColumnScope.dropdownMenuItem(
    id: String,
    item: UiDropdownMenuItem,
    width: Float,
    baseHeight: Float,
    style: Style,
    selected: Boolean,
): Boolean {
    val resolvedFont = font
    val labelSize = theme.typography.label
    val resolvedTextStyle = textStyle then TextStyle(size = labelSize)
    val glyphPx = pixelPerfectPixel(labelSize.toPx().coerceAtLeast(1f)).coerceAtLeast(1f)

    // Pure geometry (label/icon/trailing/supporting-text placement, row height) lives in
    // ui-headless -- see measureMenuItemLayout's own doc comment. Only color/decoration below
    // is design-system-specific.
    val layout = measureMenuItemLayout(
        trailingLabel = item.trailingLabel,
        hasIcon = item.icon != null,
        supportingText = item.shadcnSupportingText,
        width = width,
        baseHeight = baseHeight,
        font = resolvedFont,
        glyphPx = glyphPx,
    )

    val result = buttonSlot(
        id = id,
        modifier = Modifier.width(width.px).height(layout.computedHeight.px),
        style = style,
        variant = if (selected) UiButtonVariant.Filled else UiButtonVariant.Ghost,
        semanticRole = UiSemanticRole.MenuItem,
    ) { contentSlot ->
        val textColor = when {
            !item.enabled -> theme.colors.mutedForeground
            selected -> theme.colors.accentForeground
            item.destructive -> theme.colors.destructive
            else -> theme.colors.foreground
        }

        // Use a relative child box to anchor content correctly within the button
        val box = childBox(contentSlot)

        box.apply {
            // --- 1. Label (Primary text) ---
            // Explicit width = bodyWidth (already reserves room for the trailing shortcut):
            // without it, overflow=Ellipsis makes text() default the claimed width to the
            // FULL row (fillWidthOrNull()), so a long label only ellipsizes once it exceeds
            // the whole row, not once it reaches the trailing shortcut -- it draws straight
            // through/under the shortcut instead of truncating before it.
            text(
                label = item.label,
                modifier = Modifier.width(layout.bodyWidth.px).padding(
                    start = layout.labelStart.px,
                    top = layout.verticalPadding,
                    end = 0f.dp,
                    bottom = 0f.dp,
                ).align(layout.labelAlignment),
                color = textColor,
                font = resolvedFont,
                overflow = UiTextOverflow.Ellipsis,
                textStyle = resolvedTextStyle,
            )

            // --- 1b. Leading icon (optional) ---
            // Only reserves space when present; icon-less items are unaffected (iconSlotWidth
            // is 0, so labelStart/bodyWidth above collapse back to the original 12dp/24f math).
            item.icon?.let { iconContent ->
                val iconBounds = UiBounds(
                    contentSlot.x + 12f,
                    contentSlot.y,
                    glyphPx,
                    contentSlot.height,
                )
                childBox(iconBounds, contentAlignment = UiAlignment.Center).iconContent()
            }

            // --- 2. Trailing Shortcut ---
            item.trailingLabel?.let { label ->
                val trailingColor = if (!item.enabled) {
                    theme.colors.mutedForeground
                } else if (selected) {
                    theme.colors.accentForeground.withAlpha(0.82f)
                } else {
                    theme.colors.mutedForeground
                }
                text(
                    label = label,
                    // No overflow/wrap here on purpose: either one makes text()'s own sizing
                    // default the claimed width to FillMax, which fills the whole row and
                    // defeats `.align(trailingAlignment)` -- the End-aligned box has no room
                    // left to shift into, so the shortcut draws left-anchored under the label
                    // instead of at the row's right edge. Shortcuts are short fixed strings
                    // ("Cmd+D", "Del") that never need to wrap or truncate.
                    modifier = Modifier.align(layout.trailingAlignment)
                        .padding(start = 0f.dp, top = layout.verticalPadding, end = 8f.dp, bottom = 0f.dp),
                    color = trailingColor,
                    font = resolvedFont,
                    textStyle = resolvedTextStyle,
                )
            }

            // --- 3. Supporting Text ---
            layout.supportingLayout?.let {
                // `glyphPx` is already a resolved raster-pixel value (it went through
                // labelSize.toPx() above), same as the "8dp top + label + 4dp gap" offset
                // baked into computedHeight's own raw-px math. Using `.dp` here would push it
                // through UiDensity.scale a second time -- at density > 1 that inflates this
                // top inset well past what computedHeight budgeted for, so `place()`'s
                // height-vs-container clamp (UiAlignment.kt) chops the wrapped second line
                // even though text()'s own layout pass measured it correctly. `.px` keeps this
                // offset in the same already-scaled unit space as computedHeight.
                text(
                    label = item.shadcnSupportingText!!,
                    modifier = Modifier.padding(
                        start = 8f.dp,
                        top = (8f + glyphPx + 4f).px,
                        end = 8f.dp,
                        bottom = 0f.dp,
                    ).align(UiAlignment.TopStart),
                    color = if (selected) theme.colors.accentForeground.withAlpha(0.82f) else theme.colors.mutedForeground,
                    font = resolvedFont,
                    wrap = UiTextWrap.Word,
                    overflow = UiTextOverflow.Ellipsis,
                    maxLines = 2,
                    semanticId = "$id.supporting",
                    textStyle = resolvedTextStyle,
                )
            }
        }
    }
    return result.clicked && item.enabled
}

data class UiDropdownMenuResult(
    val slot: UiBounds?,
    val selectedIndex: Int?,
    val dismissed: Boolean,
)

data class UiDropdownMenuItem(
    val label: String,
    val destructive: Boolean = false,
    val enabled: Boolean = true,
    val shadcnSupportingText: String? = null,
    val trailingLabel: String? = null,
    val icon: (BoxScope.() -> Unit)? = null,
) : UiDropdownMenuEntry

sealed interface UiDropdownMenuEntry
data object UiDropdownMenuSeparator : UiDropdownMenuEntry

/**
 * Headless-facade dropdown recipe. Popup behavior and selection now come from [menu]; this
 * overload intentionally renders the stable label-only baseline while richer Shadcn item slots
 * remain on the legacy overload until their icon/supporting-text contracts are facade-native.
 */
fun HeadlessUiScope.shadcnDropdownMenu(
    id: String,
    anchorSlot: UiBounds,
    expanded: Boolean,
    items: List<UiDropdownMenuEntry>,
    selectedIndex: Int? = null,
    width: Dimension = Dimension.WrapContent,
    height: Dimension = Dimension.WrapContent,
    positionProvider: UiPopupPositionProvider = UiPopupDefaults.dropdown(),
    properties: UiPopupProperties = UiPopupProperties(),
): UiDropdownMenuResult {
    val entries = buildList<UiMenuEntry> {
        var index = 0
        items.forEach { entry ->
            when (entry) {
                UiDropdownMenuSeparator -> add(UiMenuSeparator)
                is UiDropdownMenuItem -> {
                    add(UiMenuItem("$id.item.$index", index, entry.enabled))
                    index += 1
                }
            }
        }
    }
    val result = menu(
        id = id,
        anchorSlot = anchorSlot,
        expanded = expanded,
        entries = entries,
        width = width,
        height = height,
        positionProvider = positionProvider,
        properties = properties,
        item = { item ->
            val source = items.filterIsInstance<UiDropdownMenuItem>().getOrNull(item.index)
            if (source == null) {
                false
            } else {
                val selected = item.index == selectedIndex
                menuItem(
                    item = item,
                    label = source.label,
                    visuals = SurfaceVisuals(
                        rest = HeadlessSurfaceStyle(
                            background = if (selected) themeValues.colors.accent else themeValues.colors.background,
                            foreground = when {
                                !source.enabled -> themeValues.colors.mutedForeground
                                selected -> themeValues.colors.accentForeground
                                source.destructive -> themeValues.colors.destructive
                                else -> themeValues.colors.foreground
                            },
                        ),
                    ),
                )
            }
        },
        separator = {},
    )
    return UiDropdownMenuResult(result.slot, result.selectedIndex, result.dismissed)
}
