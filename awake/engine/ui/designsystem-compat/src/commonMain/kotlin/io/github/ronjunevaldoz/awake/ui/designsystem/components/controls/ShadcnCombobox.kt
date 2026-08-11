// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.controls

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.headless.UiPopupDefaults
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.UiShape
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnResolvedTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnPopover
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.headless.buttonSlot
import io.github.ronjunevaldoz.awake.ui.api.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.box
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.weight
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.px
import io.github.ronjunevaldoz.awake.ui.rememberPopupState
import io.github.ronjunevaldoz.awake.ui.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.scope.claimModifiedSlot
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.components.filterOptionsByQuery
import io.github.ronjunevaldoz.awake.ui.unstyled.input.drawDropdownTriggerContent
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.separator
import io.github.ronjunevaldoz.awake.ui.withGraphicsLayerAlpha

private val COMBOBOX_OPTION_HEIGHT = 32f.dp
private val COMBOBOX_LEADING_SLOT = 16f.dp
private val COMBOBOX_DOT_SIZE = 6f.dp

/** Real shadcn's `Combobox`: a [shadcnSelect]-shaped trigger opening a [shadcnPopover] whose
 * body is an embedded [shadcnInput] filter followed by the matching options -- shadcn's own
 * Combobox is a `Popover` wrapping a `Command` (cmdk), not a `DropdownMenu`, so this is built
 * on [shadcnPopover] rather than [io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnDropdownMenu]
 * (whose self-contained item list has no room for a filter row above it). The selected row's
 * check mark has no matching icon on [io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnIcons] yet, so a small
 * primary-colored dot stands in for it, reserved in a fixed leading slot the same way
 * [io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnDropdownMenu]'s own item icon slot works. */
@Deprecated(
    message = "Use the Headless UiScope overload. This Core receiver is retained only for migration.",
)
fun UiScope.shadcnCombobox(
    id: String,
    options: List<String>,
    selectedIndex: Int?,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    placeholder: String = "",
    filterPlaceholder: String = "Search...",
    emptyLabel: String = "No results found.",
    enabled: Boolean = true,
): Int? {
    val popupState = rememberPopupState(id, key = "expanded")
    // Ephemeral, popup-local text -- unlike `selectedIndex`, the caller never needs to control
    // or read this back, so it lives in this widget's own state slot the same way textField()
    // keeps its cursor position (see skills/awake-shadcn-styling & TextField.kt).
    val filterState = rememberStateValue(id, key = "filter") { "" }
    val resolvedTheme = theme.asShadcnTheme()
    // Same trigger-specific vertical-padding re-pin shadcnSelect uses -- shadcnFieldStyle's
    // shared base disagrees with the trigger's own py-2 (see ShadcnSelect.kt's doc comment).
    val triggerStyle = shadcnFieldStyle(theme, style) then Style {
        contentPadding(resolvedTheme.metrics.fieldPaddingX, resolvedTheme.metrics.fieldPaddingY)
    }
    val trigger = buttonSlot(
        id = "$id.trigger",
        modifier = modifier.height(36f.dp),
        style = triggerStyle,
        enabled = enabled,
    ) { }
    if (trigger.clicked) {
        val opening = !popupState.expanded
        popupState.toggle()
        if (opening) filterState.value = ""
    }
    val hasSelection = selectedIndex != null && options.getOrNull(selectedIndex) != null
    val selectedLabel = if (hasSelection) options[selectedIndex!!] else placeholder
    withGraphicsLayerAlpha(alpha = if (enabled) 1f else 0.5f) {
        drawDropdownTriggerContent(
            slot = trigger.slot,
            label = selectedLabel,
            expanded = popupState.expanded,
            style = triggerStyle,
            semanticId = "$id.label",
            isPlaceholder = !hasSelection,
        )
    }
    recordSemantic(
        role = UiSemanticRole.Dropdown,
        id = id,
        label = selectedLabel,
        bounds = trigger.slot,
        selected = popupState.expanded,
    )

    var picked: Int? = null
    val popoverResult = shadcnPopover(
        id = "$id.popover",
        anchorSlot = trigger.slot,
        expanded = popupState.expanded,
        // At least the trigger width -- matches shadcnSelect's own dropdown sizing.
        width = Dimension.Fixed(trigger.slot.width.px),
        height = Dimension.WrapContent,
        positionProvider = UiPopupDefaults.dropdown(offsetY = 4f.dp),
        // shadcnPopover already applies popoverStyle(resolvedTheme) as its own base -- only the
        // override needs stating here. The Command list owns its own inset (filter row + option
        // rows below); a popover p-4 here would double-pad every row.
        style = Style { contentPadding(0f.dp) },
    ) {
        picked = shadcnComboboxPopoverContent(
            config = ShadcnComboboxPopoverConfig(id, options, selectedIndex, filterPlaceholder, emptyLabel, resolvedTheme),
            filterValue = filterState.value,
            onFilterChange = { filterState.value = it },
        )
    }
    if (popoverResult.dismissed || picked != null) {
        popupState.close()
        filterState.value = ""
    }
    return picked
}

/** Bundles [shadcnComboboxPopoverContent]'s per-render config -- keeps that function (and this
 * call site) under the parameter-count a single flat argument list would need. */
private data class ShadcnComboboxPopoverConfig(
    val id: String,
    val options: List<String>,
    val selectedIndex: Int?,
    val filterPlaceholder: String,
    val emptyLabel: String,
    val resolvedTheme: ShadcnResolvedTheme,
)

/** Popover body: the embedded filter [shadcnInput] followed by the matching option rows (or
 * [emptyLabel] when nothing matches). Split out of [shadcnCombobox] to keep that function's own
 * trigger/popup-state wiring readable; returns whichever option index was clicked this frame. */
private fun ColumnScope.shadcnComboboxPopoverContent(
    config: ShadcnComboboxPopoverConfig,
    filterValue: String,
    onFilterChange: (String) -> Unit,
): Int? {
    val id = config.id
    val resolvedTheme = config.resolvedTheme
    val nextFilter = shadcnInput(
        id = "$id.filter",
        value = filterValue,
        placeholder = config.filterPlaceholder,
        modifier = Modifier.fillMaxWidth().height(36f.dp),
        style = Style {
            // Flush inside the popover panel -- no separate input chrome, matching cmdk's
            // borderless embedded search field.
            borderWidth(0f.dp)
            shape(0f.dp)
        },
    )
    onFilterChange(nextFilter)
    separator(thickness = 1f.dp, color = resolvedTheme.colors.border)
    spacer(Modifier.height(4f.dp))

    val filtered = filterOptionsByQuery(config.options, nextFilter)
    var picked: Int? = null
    if (filtered.isEmpty()) {
        shadcnText(
            config.emptyLabel,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8f.dp, vertical = 8f.dp),
            muted = true,
        )
    } else {
        filtered.forEach { (optionIndex, option) ->
            val clicked = shadcnComboboxOption(
                id = "$id.option.$optionIndex",
                label = option,
                selected = optionIndex == config.selectedIndex,
                resolvedTheme = resolvedTheme,
            )
            if (clicked) picked = optionIndex
        }
    }
    spacer(Modifier.height(4f.dp))
    return picked
}

/** Generic-`T` `shadcnCombobox`, mirroring [shadcnSelect]'s own `List<String>`/callback split:
 * wraps the [List]<String>/`Int?` overload above instead of duplicating trigger/popup wiring. */
@Deprecated(
    message = "Use the Headless UiScope overload. This Core receiver is retained only for migration.",
)
fun <T> UiScope.shadcnCombobox(
    id: String,
    value: T?,
    options: List<T>,
    onValueChange: (T) -> Unit,
    label: (T) -> String = { it.toString() },
    placeholder: String = "",
    filterPlaceholder: String = "Search...",
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    enabled: Boolean = true,
) {
    val selectedIndex = value?.let { options.indexOf(it).takeIf { i -> i >= 0 } }
    val resultIndex = shadcnCombobox(
        id = id,
        options = options.map(label),
        selectedIndex = selectedIndex,
        modifier = modifier,
        style = style,
        placeholder = placeholder,
        filterPlaceholder = filterPlaceholder,
        enabled = enabled,
    )
    resultIndex?.let { options.getOrNull(it)?.let(onValueChange) }
}

/** One filtered option row: ghost button, hover/selected accent fill, and a fixed leading slot
 * holding the selected-row dot (see this file's top doc comment for why a dot, not an icon). */
private fun UiScope.shadcnComboboxOption(
    id: String,
    label: String,
    selected: Boolean,
    resolvedTheme: ShadcnResolvedTheme,
): Boolean = shadcnButton(
    id = id,
    modifier = Modifier.fillMaxWidth().height(COMBOBOX_OPTION_HEIGHT),
    variant = ShadcnButtonVariant.Ghost,
    style = Style {
        contentPadding(horizontal = 8f.dp, vertical = 0f.dp)
        if (selected) {
            background(resolvedTheme.colors.accent)
            foreground(resolvedTheme.colors.accentForeground)
        }
        hovered {
            background(resolvedTheme.colors.accent)
            foreground(resolvedTheme.colors.accentForeground)
        }
    },
    centered = false,
    verticallyCentered = true,
) { slot ->
    row(
        horizontalArrangement = Arrangement.spacedBy(8f.dp),
        verticalAlignment = UiAlignment.Vertical.Center,
        modifier = Modifier.width(Dimension.Fixed(slot.width.px)).height(Dimension.Fixed(slot.height.px)),
    ) {
        box(
            modifier = Modifier.width(COMBOBOX_LEADING_SLOT).height(Dimension.FillMax),
            contentAlignment = UiAlignment.Center,
        ) {
            if (selected) {
                val dotSlot = claimModifiedSlot(Modifier.width(COMBOBOX_DOT_SIZE).height(COMBOBOX_DOT_SIZE))
                emitFillAndBorder(
                    slot = dotSlot,
                    fillColor = resolvedTheme.colors.primary,
                    radiusPx = dotSlot.width / 2f,
                    borderWidth = UiShape.none,
                    borderColor = Color.Transparent,
                )
            }
        }
        shadcnText(label, modifier = Modifier.weight(1f), maxLines = 1, overflow = UiTextOverflow.Ellipsis)
    }
}
