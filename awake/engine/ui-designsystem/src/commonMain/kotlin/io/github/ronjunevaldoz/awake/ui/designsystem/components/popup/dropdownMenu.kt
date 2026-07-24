package io.github.ronjunevaldoz.awake.ui.designsystem.components.popup

import io.github.ronjunevaldoz.awake.ui.modifier.Dimension
import io.github.ronjunevaldoz.awake.ui.styling.Style
import io.github.ronjunevaldoz.awake.ui.UiAlignment
import io.github.ronjunevaldoz.awake.ui.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.UiPopupPositionProvider
import io.github.ronjunevaldoz.awake.ui.UiPopupProperties
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.font
import io.github.ronjunevaldoz.awake.ui.font.measureTextWidth
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.ext.rawSurface
import io.github.ronjunevaldoz.awake.ui.layouts.ext.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.align
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.styleable
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.pixelPerfectPixel
import io.github.ronjunevaldoz.awake.ui.popup
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.textStyle
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.TextStyle
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.unstyled.UiButtonVariant
import io.github.ronjunevaldoz.awake.ui.unstyled.buttonSlot
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.layoutBitmapText
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text
import io.github.ronjunevaldoz.awake.ui.unstyled.separator

fun UiScope.dropdownMenu(
    id: String,
    anchorSlot: UiSlot,
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
    var picked: Int? = null
    val popupResult = popup(
        anchorSlot = anchorSlot,
        expanded = expanded,
        width = width,
        height = height,
        verticalArrangement = Arrangement.spacedBy(0f.dp),
        positionProvider = positionProvider,
        properties = properties
    ) { popupSlot ->
        rawSurface(
            id = "$id.menu",
            width = Dimension.Fixed(popupSlot.width.px),
            height = height,
            verticalArrangement = Arrangement.spacedBy(0f.dp),
            modifier = Modifier
                .styleable(theme.components.surface then style then Style {
                    shape(UiShape.sm)
                }),
            clipContent = true,
        ) {
            var actionIndex = 0
            items.forEach { entry ->
                when (entry) {
                    UiDropdownMenuSeparator -> {
                        spacer(Modifier.height(4f.dp))
                        separator(
                            width = Dimension.FillMax,
                            thickness = 1f.dp,
                            color = theme.tokens.border.withAlpha(0.72f)
                        )
                        spacer(Modifier.height(4f.dp))
                    }
                    is UiDropdownMenuItem -> {
                        val currentActionIndex = actionIndex
                        val menuItemStyle = when {
                            !entry.enabled -> Style.Companion {
                                foreground(theme.tokens.mutedForeground)
                                background(theme.tokens.background.withAlpha(0.86f))
                            }
                            currentActionIndex == selectedIndex -> Style.Companion {
                                background(theme.tokens.accent)
                                foreground(theme.tokens.accentForeground)
                            }
                            entry.destructive -> Style.Companion {
                                foreground(theme.tokens.destructive)
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
    val bodyWidth = (width - 24f - trailingWidth).coerceAtLeast(glyphPx)

    val lineGap = glyphPx * 0.25f
    val supportingLayout = item.supportingText?.takeIf { it.isNotBlank() }?.let {
        layoutBitmapText(
            label = it,
            glyphPx = glyphPx,
            maxWidthPx = (width - 24f).coerceAtLeast(glyphPx),
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
            !item.enabled -> theme.tokens.mutedForeground
            selected -> theme.tokens.accentForeground
            item.destructive -> theme.tokens.destructive
            else -> theme.tokens.foreground
        }

        val verticalPadding = if (supportingLayout == null) 0f.dp else 8f.dp
        
        // Use a relative child box to anchor content correctly within the button
        val box = childBox(contentSlot)
        
        box.apply {
            // --- 1. Label (Primary text) ---
            text(
                label = item.label,
                modifier = Modifier.padding(start = 12f.dp, top = verticalPadding, end = 0f.dp, bottom = 0f.dp).align(UiAlignment.CenterStart),
                color = textColor,
                font = resolvedFont,
                overflow = UiTextOverflow.Ellipsis,
                textStyle = resolvedTextStyle
            )

            // --- 2. Trailing Shortcut ---
            item.trailingLabel?.let { label ->
                val trailingColor = if (!item.enabled) theme.tokens.mutedForeground else if (selected) theme.tokens.accentForeground.withAlpha(0.82f) else theme.tokens.mutedForeground
                text(
                    label = label,
                    modifier = Modifier.align(UiAlignment.CenterEnd).padding(start = 0f.dp, top = verticalPadding, end = 12f.dp, bottom = 0f.dp),
                    color = trailingColor,
                    font = resolvedFont,
                    overflow = UiTextOverflow.Ellipsis,
                    textStyle = resolvedTextStyle
                )
            }

            // --- 3. Supporting Text ---
            supportingLayout?.let {
                text(
                    label = item.supportingText!!,
                    modifier = Modifier.padding(start = 12f.dp, top = (8f + glyphPx + 4f).dp, end = 12f.dp, bottom = 0f.dp).align(UiAlignment.TopStart),
                    color = if (selected) theme.tokens.accentForeground.withAlpha(0.82f) else theme.tokens.mutedForeground,
                    font = resolvedFont,
                    wrap = UiTextWrap.Word,
                    overflow = UiTextOverflow.Ellipsis,
                    maxLines = 2,
                    textStyle = resolvedTextStyle
                )
            }
        }
    }
    return result.clicked && item.enabled
}

data class UiDropdownMenuResult(
    val slot: UiSlot?,
    val selectedIndex: Int?,
    val dismissed: Boolean
)

data class UiDropdownMenuItem(
    val label: String,
    val destructive: Boolean = false,
    val enabled: Boolean = true,
    val supportingText: String? = null,
    val trailingLabel: String? = null
) : UiDropdownMenuEntry

sealed interface UiDropdownMenuEntry
data object UiDropdownMenuSeparator : UiDropdownMenuEntry
