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
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceShadow
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.buttonSlot
import io.github.ronjunevaldoz.awake.ui.headless.UiTabItem
import io.github.ronjunevaldoz.awake.ui.headless.collapsible
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.wrapContentWidth
import io.github.ronjunevaldoz.awake.ui.api.theme.FontWeight
import io.github.ronjunevaldoz.awake.ui.api.sp

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
        centered = false,
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
    // Tailwind's `h-9 p-[3px]` is border-box sizing: the 36dp track includes its
    // three-pixel inset on each side. Keep the outer track at the requested height and
    // give the row only the content-box height; letting the surface size from its child
    // made the public Headless recipe grow to 42dp and overflow the parity crop.
    surface(id = "$id.track", modifier = modifier.wrapContentWidth().height(height), style = track) {
        row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment.Vertical.Center,
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
                            // tabs.tsx uses `text-foreground/60` for an inactive trigger. The
                            // muted token is a different semantic role and is visibly lighter.
                            foreground = if (active) themeValues.colors.foreground else themeValues.colors.foreground.withAlpha(0.6f),
                            // The base trigger has a transparent border; a light-mode active
                            // trigger does not acquire a visible border (dark mode's input
                            // override is a separate theme concern).
                            border = SurfaceBorder(1f.dp, io.github.ronjunevaldoz.awake.core.colors.Color.Transparent),
                            cornerRadius = themeValues.shapes.sm,
                            contentPadding = io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets(8f.dp, 4f.dp),
                            shadow = if (active) {
                                SurfaceShadow(
                                    color = io.github.ronjunevaldoz.awake.core.colors.Color.Black.withAlpha(0.12f),
                                    offsetY = 1f.dp,
                                    blurRadius = 2f.dp,
                                )
                            } else null,
                            // TabsTrigger is Tailwind `text-sm` (14px), independent of the
                            // theme's body tier. The reference also applies font-medium.
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
): UiBounds = row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(6f.dp),
    verticalAlignment = UiAlignment.Vertical.Center,
) {
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
