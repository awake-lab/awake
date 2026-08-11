// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("UnusedParameter")

package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnAlertVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.BoxScope
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.headless.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.headless.box
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.icon
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.width

fun ColumnScope.shadcnEmpty(
    id: String,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: (BoxScope.() -> Unit)? = null,
    action: (ColumnScope.() -> Unit)? = null,
): UiBounds = column(
    modifier = modifier,
    horizontalAlignment = UiAlignment.Horizontal.Center,
    verticalArrangement = Arrangement.spacedBy(16f.dp),
) {
    if (icon != null) {
        box(
            modifier = Modifier.width(40f.dp).height(40f.dp),
            contentAlignment = UiAlignment.Center,
        ) { icon() }
    }
    text(
        label = title,
        visuals = SurfaceStyle(
            foreground = themeValues.colors.foreground,
            textSize = themeValues.typography.title,
        ),
    )
    description?.takeIf(String::isNotBlank)?.let {
        text(
            label = it,
            modifier = Modifier.fillMaxWidth(),
            visuals = SurfaceStyle(
                foreground = themeValues.colors.mutedForeground,
                textSize = themeValues.typography.caption,
            ),
            centered = true,
            wrap = UiTextWrap.Word,
            overflow = UiTextOverflow.Ellipsis,
        )
    }
    action?.invoke(this)
}

fun ColumnScope.shadcnAlert(
    id: String,
    modifier: Modifier = Modifier,
    variant: ShadcnAlertVariant = ShadcnAlertVariant.Default,
    content: ColumnScope.() -> Unit,
): UiBounds {
    val colors = themeValues.colors
    val foreground = if (variant == ShadcnAlertVariant.Destructive) colors.destructive else colors.foreground
    return surface(
        id = id,
        modifier = modifier,
        style = SurfaceStyle(
            background = colors.card,
            foreground = foreground,
            border = SurfaceBorder(1f.dp, if (variant == ShadcnAlertVariant.Destructive) colors.destructive else colors.border),
            cornerRadius = themeValues.shapes.md,
            contentPadding = UiInsets(16f.dp),
        ),
        content = { content() },
    )
}

fun ColumnScope.shadcnAlert(
    id: String,
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    variant: ShadcnAlertVariant = ShadcnAlertVariant.Default,
): UiBounds = shadcnAlert(id = id, modifier = modifier, variant = variant) {
    text(label = title, visuals = SurfaceStyle(textSize = themeValues.typography.label))
    description?.let { text(label = it, visuals = SurfaceStyle(textSize = themeValues.typography.caption), wrap = UiTextWrap.Word) }
}
