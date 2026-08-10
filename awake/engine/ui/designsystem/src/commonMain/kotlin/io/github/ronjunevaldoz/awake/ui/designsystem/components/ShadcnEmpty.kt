// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.childBox
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnSpacing
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.graphics.emitFillAndBorder
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.column
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.tailwind.Tw
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.toPx
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextOverflow
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.UiTextWrap
import io.github.ronjunevaldoz.awake.ui.unstyled.input.text.text

/** shadcn's `EmptyMedia variant="icon"` -- a `size-10` muted rounded-square housing the icon,
 * not [shadcnAvatar] ([io.github.ronjunevaldoz.awake.ui.unstyled.avatarFallback] hardcodes a
 * circle, real Empty media is `rounded-lg`). */
private val EmptyMediaSize = 40f.dp

private fun UiScope.shadcnEmptyMedia(icon: BoxScope.() -> Unit) {
    val shadcnTheme = theme.asShadcnTheme()
    val slot = claimSlot(Dimension.Fixed(EmptyMediaSize), Dimension.Fixed(EmptyMediaSize))
    emitFillAndBorder(
        slot = slot,
        fillColor = shadcnTheme.palette.muted,
        radiusPx = shadcnTheme.radii.lg.toPx(),
        borderWidth = 0f.dp,
        borderColor = Color.Transparent,
    )
    childBox(slot, contentAlignment = UiAlignment.Center).icon()
}

/**
 * Real shadcn's `Empty`: a centered placeholder for a list/page with nothing in it yet -- an
 * optional icon, a title, an optional description, and an optional action slot (buttons). Purely
 * a composition of existing primitives (no new behaviour), so this only exists as a [ColumnScope]
 * recipe -- the natural place an empty state gets embedded, same as real shadcn's own usage.
 */
fun ColumnScope.shadcnEmpty(
    id: String,
    title: String,
    modifier: UiModifier = Modifier,
    description: String? = null,
    icon: (BoxScope.() -> Unit)? = null,
    action: (ColumnScope.() -> Unit)? = null,
): UiBounds {
    val shadcnTheme = theme.asShadcnTheme()
    return column(
        id = id,
        modifier = modifier.fillMaxWidth().padding(shadcnTheme.metrics.panelPadding),
        horizontalAlignment = UiAlignment.Horizontal.Center,
        verticalArrangement = Arrangement.spacedBy(ShadcnSpacing.lg),
    ) {
        column(
            horizontalAlignment = UiAlignment.Horizontal.Center,
            verticalArrangement = Arrangement.spacedBy(ShadcnSpacing.sm),
        ) {
            if (icon != null) shadcnEmptyMedia(icon)
            shadcnSectionTitle(title)
            if (!description.isNullOrBlank()) {
                text(
                    label = description,
                    modifier = Modifier.fillMaxWidth(),
                    centered = true,
                    wrap = UiTextWrap.Word,
                    overflow = UiTextOverflow.Ellipsis,
                    maxLines = Int.MAX_VALUE,
                    style = Style {
                        foreground(shadcnTheme.colors.mutedForeground)
                        textSize(shadcnTheme.typography.caption)
                    },
                )
            }
        }
        if (action != null) {
            column(
                horizontalAlignment = UiAlignment.Horizontal.Center,
                // Real EmptyContent is `gap-4`(16dp), not gap-2.
                verticalArrangement = Arrangement.spacedBy(Tw.Spacing.s4),
            ) { action() }
        }
    }
}
