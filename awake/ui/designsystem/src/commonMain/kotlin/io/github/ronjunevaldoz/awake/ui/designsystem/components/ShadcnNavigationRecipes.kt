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
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnBreadcrumbItemStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnBreadcrumbContainerStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnBreadcrumbMutedStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnCollapsibleCardStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnCollapsibleTitleStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnCollapsibleTriggerStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnTabsTrackStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnTabStyle
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiTabItem
import io.github.ronjunevaldoz.awake.ui.headless.button
import io.github.ronjunevaldoz.awake.ui.headless.collapsible
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.icon
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.wrapContentWidth

fun UiScope.shadcnCollapsible(
    id: String,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    onExpandedChange: (Boolean) -> Unit = {},
    trigger: ColumnScope.(Boolean, () -> Unit) -> Unit,
    content: ColumnScope.() -> Unit,
): Boolean = collapsible(id, expanded, modifier, onExpandedChange, trigger, content)

fun UiScope.shadcnCollapsible(
    id: String,
    title: String,
    expanded: Boolean,
    modifier: Modifier = Modifier,
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
                icon(if (isOpen) ShadcnIcons.chevronDown else ShadcnIcons.chevronRight)
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
    modifier: Modifier = Modifier,
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
    modifier: Modifier = Modifier,
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

fun UiScope.shadcnTabs(
    id: String,
    items: List<UiTabItem>,
    selected: String,
    modifier: Modifier = Modifier,
    height: Dp = 36f.dp,
): String {
    val track = shadcnTabsTrackStyle(themeValues)
    var resolved = selected
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
    return resolved
}

fun UiScope.shadcnTabs(
    id: String,
    tabs: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
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
    modifier: Modifier = Modifier,
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
): Boolean = shadcnButton(
    id = id,
    label = label,
    modifier = modifier,
    variant = ShadcnButtonVariant.Ghost,
    size = ShadcnButtonSize.Xs,
    onClick = onClick,
)

fun UiScope.shadcnBreadcrumbPage(label: String, modifier: Modifier = Modifier): UiBounds = text(
    label,
    modifier = modifier,
    style = shadcnBreadcrumbItemStyle(themeValues, current = true),
)

fun UiScope.shadcnBreadcrumbSeparator(
    label: String = "/",
    modifier: Modifier = Modifier,
): UiBounds = text(
    label,
    modifier = modifier,
    style = shadcnBreadcrumbMutedStyle(themeValues),
)

fun UiScope.shadcnBreadcrumbEllipsis(modifier: Modifier = Modifier): UiBounds =
    shadcnBreadcrumbSeparator("...", modifier)
