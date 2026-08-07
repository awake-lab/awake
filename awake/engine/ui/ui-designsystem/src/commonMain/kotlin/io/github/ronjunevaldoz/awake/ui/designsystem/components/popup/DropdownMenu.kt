package io.github.ronjunevaldoz.awake.ui.designsystem.components.popup

import io.github.ronjunevaldoz.awake.ui.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.font.measureTextWidth
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.align
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.styleable
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.scope.pixelPerfectPixel
import io.github.ronjunevaldoz.awake.ui.popup
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.rememberScrollState
import io.github.ronjunevaldoz.awake.ui.modifier.verticalScroll
import io.github.ronjunevaldoz.awake.ui.textStyle
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.headless.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.buttonSlot
import io.github.ronjunevaldoz.awake.ui.headless.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.headless.input.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.headless.input.text.layoutBitmapText
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
import io.github.ronjunevaldoz.awake.ui.headless.separator
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

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
    itemStyle: Style = Style.Empty
): UiDropdownMenuResult {
    val theme = context.currentTheme
    // Wired the same way every other scrollable surface in this module wires it (see
    // shadcnSidebar's doc comment): a plain Modifier.verticalScroll(state) on the menu surface.
    // Only applied when the caller constrains [height] -- the default WrapContent already grows
    // to fit every item (never needs scroll), and unconditionally attaching a scrollState would
    // route even the WrapContent case through scrollPanel()'s claimSlot(), which cannot resolve
    // WrapContent while it's still inside popup()'s own WrapContent-sizing measure pass.
    val scrollState = if (height != Dimension.WrapContent) context.rememberScrollState("$id.scroll") else null
    var picked: Int? = null
    val popupResult = popup(
        id = id,
        anchorSlot = anchorSlot,
        expanded = expanded,
        width = width,
        height = height,
        verticalArrangement = Arrangement.spacedBy(0f.dp),
        positionProvider = positionProvider,
        properties = properties
    ) { popupSlot ->
        surface(
            id = "$id.menu",
            verticalArrangement = Arrangement.spacedBy(0f.dp),
            modifier = Modifier
                .width(Dimension.Fixed(popupSlot.width.px))
                .height(height)
                .let { if (scrollState != null) it.verticalScroll(scrollState) else it }
                .styleable(theme.components.surface then style then Style {
                    shape(UiShape.sm)
                }),
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
                            color = theme.colors.border.withAlpha(0.72f)
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
                            selected = currentActionIndex == selectedIndex
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
        dismissed = popupResult.dismissed
    )
}



private fun ColumnScope.dropdownMenuItem(
    id: String,
    item: UiDropdownMenuItem,
    width: Float,
    baseHeight: Float,
    style: Style,
    selected: Boolean
): Boolean {
    val resolvedFont = font
    val labelSize = theme.typography.label
    val resolvedTextStyle = textStyle then TextStyle(size = labelSize)
    val glyphPx = pixelPerfectPixel(labelSize.toPx().coerceAtLeast(1f)).coerceAtLeast(1f)
    
    val trailingWidth = item.trailingLabel?.let { label ->
        resolvedFont.measureTextWidth(label, glyphPx) + 8f
    } ?: 0f
    // Leading icon slot: a fixed glyphPx-square reserved only when `item.icon` is set, so
    // icon-less items keep the exact same 12dp label start they had before this field existed.
    val iconSlotWidth = if (item.icon != null) glyphPx + 8f else 0f
    // shadcn px-2 = 8dp horizontal item padding (was 12f hardcoded)
    val labelStart = 8f.dp.toPx() + iconSlotWidth
    val bodyWidth = (width - 16f.dp.toPx() - trailingWidth - iconSlotWidth).coerceAtLeast(glyphPx)

    val lineGap = glyphPx * 0.25f
    val supportingLayout = item.shadcnSupportingText?.takeIf { it.isNotBlank() }?.let {
        layoutBitmapText(
            label = it,
            glyphPx = glyphPx,
            maxWidthPx = (width - 16f.dp.toPx()).coerceAtLeast(glyphPx),
            wrap = UiTextWrap.Word,
            overflow = UiTextOverflow.Ellipsis,
            maxLines = 2,
            advanceOf = { char -> resolvedFont.advanceFor(char, glyphPx) }
        )
    }
    
    val supportingHeight = supportingLayout?.blockHeight(glyphPx, lineGap) ?: 0f
    val computedHeight = if (supportingLayout == null) {
        baseHeight
    } else {
        // Vertical stack: 8dp top + label + 4dp gap + supporting + 8dp bottom
        maxOf(baseHeight, 8f + glyphPx + 4f + supportingHeight + 8f)
    }

    val result = buttonSlot(
        id = id,
        modifier = Modifier.width(width.px).height(computedHeight.px),
        style = style,
        variant = if (selected) UiButtonVariant.Filled else UiButtonVariant.Ghost
    ) { contentSlot ->
        val textColor = when {
            !item.enabled -> theme.colors.mutedForeground
            selected -> theme.colors.accentForeground
            item.destructive -> theme.colors.destructive
            else -> theme.colors.foreground
        }

        val verticalPadding = if (supportingLayout == null) 0f.dp else 8f.dp
        // A one-line item centers its label in the full row height. A two-line item (with
        // supporting text) must pin the label to the top instead -- supportingLayout's own
        // position is a fixed top-offset ("8dp top + label + 4dp gap + supporting"), so
        // centering the label in the taller box would drift it down into that fixed offset
        // and overlap the supporting text.
        val labelAlignment = if (supportingLayout == null) UiAlignment.CenterStart else UiAlignment.TopStart
        val trailingAlignment = if (supportingLayout == null) UiAlignment.CenterEnd else UiAlignment.TopEnd

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
                modifier = Modifier.width(bodyWidth.px).padding(start = labelStart.px, top = verticalPadding, end = 0f.dp, bottom = 0f.dp).align(labelAlignment),
                color = textColor,
                font = resolvedFont,
                overflow = UiTextOverflow.Ellipsis,
                textStyle = resolvedTextStyle
            )

            // --- 1b. Leading icon (optional) ---
            // Only reserves space when present; icon-less items are unaffected (iconSlotWidth
            // is 0, so labelStart/bodyWidth above collapse back to the original 12dp/24f math).
            item.icon?.let { iconContent ->
                val iconBounds = UiBounds(
                    contentSlot.x + 12f, contentSlot.y, glyphPx, contentSlot.height
                )
                childBox(iconBounds, contentAlignment = UiAlignment.Center).iconContent()
            }

            // --- 2. Trailing Shortcut ---
            item.trailingLabel?.let { label ->
                val trailingColor = if (!item.enabled) theme.colors.mutedForeground else if (selected) theme.colors.accentForeground.withAlpha(0.82f) else theme.colors.mutedForeground
                text(
                    label = label,
                    // No overflow/wrap here on purpose: either one makes text()'s own sizing
                    // default the claimed width to FillMax, which fills the whole row and
                    // defeats `.align(trailingAlignment)` -- the End-aligned box has no room
                    // left to shift into, so the shortcut draws left-anchored under the label
                    // instead of at the row's right edge. Shortcuts are short fixed strings
                    // ("Cmd+D", "Del") that never need to wrap or truncate.
                    modifier = Modifier.align(trailingAlignment).padding(start = 0f.dp, top = verticalPadding, end = 8f.dp, bottom = 0f.dp),
                    color = trailingColor,
                    font = resolvedFont,
                    textStyle = resolvedTextStyle
                )
            }

            // --- 3. Supporting Text ---
            supportingLayout?.let {
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
                    modifier = Modifier.padding(start = 8f.dp, top = (8f + glyphPx + 4f).px, end = 8f.dp, bottom = 0f.dp).align(UiAlignment.TopStart),
                    color = if (selected) theme.colors.accentForeground.withAlpha(0.82f) else theme.colors.mutedForeground,
                    font = resolvedFont,
                    wrap = UiTextWrap.Word,
                    overflow = UiTextOverflow.Ellipsis,
                    maxLines = 2,
                    semanticId = "$id.supporting",
                    textStyle = resolvedTextStyle
                )
            }
        }
    }
    return result.clicked && item.enabled
}

data class UiDropdownMenuResult(
    val slot: UiBounds?,
    val selectedIndex: Int?,
    val dismissed: Boolean
)

data class UiDropdownMenuItem(
    val label: String,
    val destructive: Boolean = false,
    val enabled: Boolean = true,
    val shadcnSupportingText: String? = null,
    val trailingLabel: String? = null,
    val icon: (BoxScope.() -> Unit)? = null
) : UiDropdownMenuEntry

sealed interface UiDropdownMenuEntry
data object UiDropdownMenuSeparator : UiDropdownMenuEntry
