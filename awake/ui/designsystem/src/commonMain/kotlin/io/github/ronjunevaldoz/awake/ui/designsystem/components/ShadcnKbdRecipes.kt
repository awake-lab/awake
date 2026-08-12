// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.text

/**
 * Keyboard shortcut pill component (`⌘K`, `Ctrl+C`).
 *
 * Matches real shadcn/ui's `kbd.tsx`.
 */
fun UiScope.shadcnKbd(
    text: String,
    modifier: Modifier = Modifier,
    id: String = "kbd.$text",
): UiBounds = surface(
    id = id,
    modifier = modifier,
    style = SurfaceStyle(
        background = themeValues.colors.muted,
        foreground = themeValues.colors.mutedForeground,
        border = SurfaceBorder(width = 1f.dp, color = themeValues.colors.border),
        cornerRadius = themeValues.shapes.sm,
        contentPadding = UiInsets(horizontal = 6f.dp, vertical = 2f.dp),
        textSize = themeValues.typography.caption,
    ),
) {
    text(label = text)
}
