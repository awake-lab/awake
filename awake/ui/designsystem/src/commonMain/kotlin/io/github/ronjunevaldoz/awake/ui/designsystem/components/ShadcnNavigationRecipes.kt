// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("LongParameterList", "TooManyFunctions", "UnusedParameter")

package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.sp
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.font.FontWeight
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceShadow
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiTabItem
import io.github.ronjunevaldoz.awake.ui.headless.buttonSlot
import io.github.ronjunevaldoz.awake.ui.headless.collapsible
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
        val clicked = buttonSlot(
            id = "$id.trigger",
            modifier = Modifier.fillMaxWidth().height(36f.dp),
            visuals = io.github.ronjunevaldoz.awake.ui.headless.SurfaceVisuals(
                rest = SurfaceStyle(
                    background = io.github.ronjunevaldoz.awake.core.colors.Color.Transparent,
                    foreground = themeValues.colors.foreground,
                    cornerRadius = themeValues.shapes.md,
                ),
                hovered = SurfaceStyle(
                    background = themeValues.colors.accent,
                    foreground = themeValues.colors.accentForeground,
                ),
            ),
        ) {
            row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = UiAlignment.Vertical.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                text(title, visuals = SurfaceStyle(textSize = themeValues.typography.body))
                icon(if (isOpen) ShadcnIcons.chevronDown else ShadcnIcons.chevronRight)
            }
        }
        if (clicked.clicked) toggle()
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
                style = SurfaceStyle(contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(bottom = 16f.dp)),
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
    val track = SurfaceStyle(
        background = themeValues.colors.muted,
        cornerRadius = themeValues.shapes.md,
        contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(3f.dp),
    )
    var resolved = selected
    surface(id = "$id.track", modifier = modifier.wrapContentWidth().height(height), style = track) {
        row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = UiAlignment.Vertical.Center,
            modifier = Modifier.wrapContentWidth().height(height - 6f.dp),
        ) {
            items.forEach { item ->
                val active = item.value == selected
                val clicked = buttonSlot(
                    id = "$id.${item.value}",
                    label = item.label,
                    modifier = Modifier.height(height - 6f.dp),
                    visuals = io.github.ronjunevaldoz.awake.ui.headless.SurfaceVisuals(
                        rest = SurfaceStyle(
                            background = if (active) themeValues.colors.background else io.github.ronjunevaldoz.awake.core.colors.Color.Transparent,
                            foreground = if (active) themeValues.colors.foreground else themeValues.colors.foreground.withAlpha(0.6f),
                            border = SurfaceBorder(1f.dp, io.github.ronjunevaldoz.awake.core.colors.Color.Transparent),
                            cornerRadius = themeValues.shapes.sm,
                            contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(8f.dp, 4f.dp),
                            shadow = if (active) {
                                SurfaceShadow(
                                    color = io.github.ronjunevaldoz.awake.core.colors.Color.Black.withAlpha(0.12f),
                                    offsetY = 1f.dp,
                                    blurRadius = 2f.dp,
                                )
                            } else {
                                null
                            },
                            textSize = 14f.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    ),
                )
                if (clicked.clicked) resolved = item.value
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
    style = SurfaceStyle(contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(0f.dp)),
) {
    row(
        horizontalArrangement = Arrangement.spacedBy(6f.dp),
        verticalAlignment = UiAlignment.Vertical.Center,
    ) {
        items.forEachIndexed { index, label ->
            text(
                label,
                visuals = SurfaceStyle(
                    foreground = if (index == items.lastIndex) themeValues.colors.foreground else themeValues.colors.mutedForeground,
                    textSize = themeValues.typography.caption,
                ),
            )
            if (index != items.lastIndex) {
                text(
                    separator,
                    visuals = SurfaceStyle(
                        foreground = themeValues.colors.mutedForeground,
                        textSize = themeValues.typography.caption,
                    ),
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
    visuals = SurfaceStyle(
        foreground = themeValues.colors.foreground,
        textSize = themeValues.typography.caption,
    ),
)

fun UiScope.shadcnBreadcrumbSeparator(
    label: String = "/",
    modifier: Modifier = Modifier,
): UiBounds = text(
    label,
    modifier = modifier,
    visuals = SurfaceStyle(
        foreground = themeValues.colors.mutedForeground,
        textSize = themeValues.typography.caption,
    ),
)

fun UiScope.shadcnBreadcrumbEllipsis(modifier: Modifier = Modifier): UiBounds =
    shadcnBreadcrumbSeparator("...", modifier)
