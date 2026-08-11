// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("LongParameterList", "TooManyFunctions", "UnusedParameter")

package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.RowScope
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiTabItem
import io.github.ronjunevaldoz.awake.ui.headless.collapsible
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.tabs
import io.github.ronjunevaldoz.awake.ui.headless.text

fun ColumnScope.shadcnCollapsible(
    id: String,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    onExpandedChange: (Boolean) -> Unit = {},
    trigger: ColumnScope.(Boolean, () -> Unit) -> Unit,
    content: ColumnScope.() -> Unit,
): Boolean = collapsible(id, expanded, modifier, onExpandedChange, trigger, content)

fun ColumnScope.shadcnCollapsible(
    id: String,
    title: String,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    onExpandedChange: (Boolean) -> Unit = {},
    content: ColumnScope.() -> Unit,
): Boolean = shadcnCollapsible(id, expanded, modifier, onExpandedChange, trigger = { open, toggle ->
    shadcnButton(
        id = "$id.trigger",
        label = title,
        modifier = Modifier.fillMaxWidth().height(36f.dp),
        variant = ShadcnButtonVariant.Ghost,
        onClick = toggle,
    )
}, content = content)

fun ColumnScope.shadcnCollapsibleCard(
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
        style = SurfaceStyle(
            background = themeValues.colors.card,
            foreground = themeValues.colors.foreground,
            border = SurfaceBorder(1f.dp, themeValues.colors.border),
            cornerRadius = themeValues.shapes.lg,
        ),
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

fun <T> ColumnScope.shadcnAccordion(
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
                style = SurfaceStyle(contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(bottom = 16f.dp)),
            ) { content(item) }
        }
    }
}

fun ColumnScope.shadcnTabs(
    id: String,
    items: List<UiTabItem>,
    selected: String,
    modifier: Modifier = Modifier,
    height: Dp = 36f.dp,
): String {
    val track = SurfaceStyle(
        background = themeValues.colors.muted,
        cornerRadius = themeValues.shapes.md,
        contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(3f.dp),
    )
    var resolved = selected
    surface(id = "$id.track", modifier = modifier, style = track) {
        row(horizontalArrangement = Arrangement.Start, modifier = Modifier.height(height)) {
            items.forEach { item ->
                val active = item.value == selected
                val clicked = shadcnButton(
                    id = "$id.${item.value}",
                    label = item.label,
                    modifier = Modifier.height(height),
                    variant = if (active) ShadcnButtonVariant.Secondary else ShadcnButtonVariant.Ghost,
                    size = io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize.Xs,
                    onClick = { resolved = item.value },
                )
                if (clicked) resolved = item.value
            }
        }
    }
    return resolved
}

fun ColumnScope.shadcnTabs(
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
).let { value -> tabs.indexOf(value).takeIf { it >= 0 } ?: selectedIndex }

fun ColumnScope.shadcnBreadcrumb(
    items: List<String>,
    modifier: Modifier = Modifier,
    separator: String = "/",
): UiBounds = row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6f.dp)) {
    items.forEachIndexed { index, label ->
        text(
            label = label,
            visuals = SurfaceStyle(
                foreground = if (index == items.lastIndex) themeValues.colors.foreground else themeValues.colors.mutedForeground,
                textSize = themeValues.typography.caption,
            ),
        )
        if (index != items.lastIndex) text(separator, visuals = SurfaceStyle(foreground = themeValues.colors.mutedForeground))
    }
}

fun ColumnScope.shadcnBreadcrumb(
    modifier: Modifier = Modifier,
    content: ColumnScope.() -> Unit,
): UiBounds = surface(id = "breadcrumb", modifier = modifier) { content() }

fun UiScope.shadcnBreadcrumb(
    id: String,
    items: List<String>,
    modifier: Modifier = Modifier,
    separator: String = "/",
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = SurfaceStyle(contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(0f.dp)),
) {
    row(horizontalArrangement = Arrangement.spacedBy(6f.dp)) {
        items.forEachIndexed { index, label ->
            text(
                label,
                visuals = SurfaceStyle(
                    foreground = if (index == items.lastIndex) themeValues.colors.foreground else themeValues.colors.mutedForeground,
                    textSize = themeValues.typography.caption,
                ),
            )
            if (index != items.lastIndex) text(separator, visuals = SurfaceStyle(foreground = themeValues.colors.mutedForeground))
        }
    }
}

fun RowScope.shadcnBreadcrumbLink(
    id: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
): Boolean = shadcnButton(
    id = id,
    label = label,
    modifier = modifier,
    variant = ShadcnButtonVariant.Ghost,
    size = io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize.Xs,
    onClick = onClick,
)

fun RowScope.shadcnBreadcrumbPage(label: String, modifier: Modifier = Modifier): UiBounds = text(
    label,
    modifier = modifier,
    visuals = SurfaceStyle(foreground = themeValues.colors.foreground, textSize = themeValues.typography.caption),
)

fun RowScope.shadcnBreadcrumbSeparator(label: String = "/", modifier: Modifier = Modifier): UiBounds = text(
    label,
    modifier = modifier,
    visuals = SurfaceStyle(foreground = themeValues.colors.mutedForeground, textSize = themeValues.typography.caption),
)

fun RowScope.shadcnBreadcrumbEllipsis(modifier: Modifier = Modifier): UiBounds = shadcnBreadcrumbSeparator("...", modifier)
