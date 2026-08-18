// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("LongParameterList", "TooManyFunctions", "UnusedParameter")

package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnAccordionContentStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnBreadcrumbContainerStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnBreadcrumbItemStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnBreadcrumbMutedStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnCollapsibleCardStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnCollapsibleTitleStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnCollapsibleTriggerStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnTabStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnTabsTrackStyle
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiModifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiTabItem
import io.github.ronjunevaldoz.awake.ui.headless.button
import io.github.ronjunevaldoz.awake.ui.headless.collapsible
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.icon
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.wrapContentWidth
import io.github.ronjunevaldoz.ui.heroicons.icon.HeroIcons

fun UiScope.shadcnCollapsible(
    id: String,
    expanded: Boolean,
    modifier: UiModifier = Modifier,
    onExpandedChange: (Boolean) -> Unit = {},
    trigger: ColumnScope.(Boolean, () -> Unit) -> Unit,
    content: ColumnScope.() -> Unit,
): Boolean = collapsible(id, expanded, modifier, onExpandedChange, trigger, content)

fun UiScope.shadcnCollapsible(
    id: String,
    title: String,
    expanded: Boolean,
    modifier: UiModifier = Modifier,
    onExpandedChange: (Boolean) -> Unit = {},
    bordered: Boolean = false,
    content: ColumnScope.() -> Unit,
): Boolean {
    val trigger: ColumnScope.(Boolean, () -> Unit) -> Unit = { isOpen, toggle ->
        val clicked = button(
            id = "$id.trigger",
            modifier = Modifier.fillMaxWidth().height(36f.dp),
            style = shadcnCollapsibleTriggerStyle(themeValues),
        ) {
            row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = UiAlignment.Vertical.Center,
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            ) {
                text(title, style = shadcnCollapsibleTitleStyle(themeValues))
                icon(if (isOpen) HeroIcons.Solid20Mini.chevronDown else HeroIcons.Solid20Mini.chevronRight)
            }
        }
        if (clicked) toggle()
    }

    if (!bordered) {
        return shadcnCollapsible(id, expanded, modifier, onExpandedChange, trigger, content)
    }

    var resolved = expanded
    shadcnCard(id = "$id.panel", modifier = modifier.fillMaxWidth()) {
        resolved = shadcnCollapsible(id, expanded, modifier = Modifier, onExpandedChange, trigger, content)
    }
    return resolved
}

fun UiScope.shadcnCollapsibleCard(
    id: String,
    expanded: Boolean,
    modifier: UiModifier = Modifier,
    onExpandedChange: (Boolean) -> Unit = {},
    header: ColumnScope.(Boolean, () -> Unit) -> Unit,
    content: ColumnScope.() -> Unit,
): Boolean {
    var resolved = expanded
    surface(
        id = "$id.card",
        modifier = modifier,
        style = shadcnCollapsibleCardStyle(themeValues),
    ) {
        resolved = shadcnCollapsible(
            id = id,
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            trigger = header,
            content = content,
        )
    }
    return resolved
}

fun <T> UiScope.shadcnAccordion(
    items: List<T>,
    selectedId: String?,
    onSelectId: (String?) -> Unit,
    idProvider: (T) -> String,
    titleProvider: (T) -> String,
    modifier: UiModifier = Modifier,
    content: ColumnScope.(T) -> Unit,
) {
    items.forEach { item ->
        val itemId = idProvider(item)
        shadcnCollapsible(
            id = itemId,
            title = titleProvider(item),
            expanded = selectedId == itemId,
            modifier = modifier,
            onExpandedChange = { onSelectId(if (it) itemId else null) },
        ) {
            surface(
                id = "$itemId.content",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8f.dp, vertical = 0f.dp),
                style = shadcnAccordionContentStyle(),
            ) { content(item) }
        }
    }
}

/**
 * Tab track plus its content panel -- the track alone (`content = {}`, the default) renders the
 * old track-only look for previews that don't need a panel; real usages should supply [content]
 * instead of switching on the returned value outside the call, which is what every prior caller
 * had to do because no panel slot existed here.
 */
fun UiScope.shadcnTabs(
    id: String,
    items: List<UiTabItem>,
    selected: String,
    modifier: UiModifier = Modifier,
    height: Dp = 36f.dp,
    content: ColumnScope.(String) -> Unit = {},
): String {
    val track = shadcnTabsTrackStyle(themeValues)
    var resolved = selected
    // [modifier] stays on the track surface, exactly as it did before this had a content slot --
    // existing track-only callers (and their goldens) keep sizing the track itself, not a new
    // wrapping node. The panel column below only wraps to group track + content under one id.
    column(id = id) {
        surface(id = "$id.track", modifier = modifier.wrapContentWidth().height(height), style = track) {
            row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = UiAlignment.Vertical.Center,
                modifier = Modifier.wrapContentWidth().height(height - 6f.dp),
            ) {
                items.forEach { item ->
                    val active = item.value == selected
                    val clicked = button(
                        id = "$id.${item.value}",
                        label = item.label,
                        modifier = Modifier.height(height - 6f.dp),
                        style = shadcnTabStyle(themeValues, active),
                    )
                    if (clicked) resolved = item.value
                }
            }
        }
        // Panel renders against [selected] -- this frame's stable input -- not [resolved]. A
        // click flips [resolved] mid-frame, and content() can pick a structurally different
        // branch (a different child count) per selection; rendering it from a value that can
        // change between this same call's measure and paint pass is exactly the
        // claimSlot()-index-mismatch crash class this module has shipped before (see
        // StudioBottomDock's drawStudioBottomDock comment). [resolved] is still returned so the
        // caller applies the click starting next frame, same contract every other immediate-mode
        // widget here already follows.
        content(selected)
    }
    return resolved
}

/**
 * Convenience overload keyed by label instead of a stable [UiTabItem.value] -- duplicate labels
 * collapse to the same trigger id and the same round-tripped index, so callers with
 * non-unique tab names must use the [items]-based overload above instead. No content slot: a
 * label list can't express per-tab panel content, so this stays track-only.
 */
fun UiScope.shadcnTabs(
    id: String,
    tabs: List<String>,
    selectedIndex: Int,
    modifier: UiModifier = Modifier,
    height: Dp = 36f.dp,
): Int = shadcnTabs(
    id = id,
    items = tabs.map { UiTabItem(it, it) },
    selected = tabs.getOrNull(selectedIndex) ?: tabs.firstOrNull().orEmpty(),
    modifier = modifier,
    height = height,
).let { value -> tabs.indexOf(value).takeIf { it >= 0 } ?: selectedIndex }

fun UiScope.shadcnBreadcrumb(
    id: String,
    items: List<String>,
    modifier: UiModifier = Modifier,
    separator: String = "/",
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = shadcnBreadcrumbContainerStyle(),
) {
    row(
        horizontalArrangement = Arrangement.spacedBy(6f.dp),
        verticalAlignment = UiAlignment.Vertical.Center,
    ) {
        items.forEachIndexed { index, label ->
            text(
                label,
                style = shadcnBreadcrumbItemStyle(themeValues, current = index == items.lastIndex),
            )
            if (index != items.lastIndex) {
                text(
                    separator,
                    style = shadcnBreadcrumbMutedStyle(themeValues),
                )
            }
        }
    }
}

fun UiScope.shadcnBreadcrumbLink(
    id: String,
    label: String,
    modifier: UiModifier = Modifier,
    onClick: () -> Unit = {},
): Boolean = shadcnButton(
    id = id,
    label = label,
    modifier = modifier,
    variant = ShadcnButtonVariant.Ghost,
    size = ShadcnButtonSize.Xs,
    onClick = onClick,
)

fun UiScope.shadcnBreadcrumbPage(label: String, modifier: UiModifier = Modifier): UiBounds = text(
    label,
    modifier = modifier,
    style = shadcnBreadcrumbItemStyle(themeValues, current = true),
)

fun UiScope.shadcnBreadcrumbSeparator(
    label: String = "/",
    modifier: UiModifier = Modifier,
): UiBounds = text(
    label,
    modifier = modifier,
    style = shadcnBreadcrumbMutedStyle(themeValues),
)

fun UiScope.shadcnBreadcrumbEllipsis(modifier: UiModifier = Modifier): UiBounds =
    shadcnBreadcrumbSeparator("...", modifier)
