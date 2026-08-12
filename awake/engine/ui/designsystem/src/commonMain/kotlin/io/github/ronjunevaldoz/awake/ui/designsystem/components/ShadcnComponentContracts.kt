// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.Sp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnTheme

internal val SHADCN_CHECKBOX_CORNER_RADIUS = 4f.dp

/**
 * Measurements from `new-york-v4/ui/radio-group.tsx` in the pinned shadcn-ui checkout.
 *
 * `RadioGroupItem` is `size-4`; its indicator icon is `size-2`; an authored label row is
 * `flex items-center gap-2`; and the group root is `grid gap-3`. Keep these named so a recipe
 * cannot silently turn a radio into a checkbox-shaped 24dp control again.
 */
internal object ShadcnRadioMetrics {
    val itemSize: Dp = 16f.dp
    val indicatorSize: Dp = 8f.dp
    val labelGap: Dp = 8f.dp
    val groupGap: Dp = 12f.dp
}

/**
 * Measurements from `new-york-v4/ui/sidebar.tsx` in the pinned shadcn-ui checkout.
 *
 * These are component-recipe values, not generic layout defaults. The showcase may choose its
 * own outer width and content, but a shadcn Sidebar's internal padding and menu rhythm must use
 * this one contract.
 */
internal object ShadcnSidebarMetrics {
    val contentPadding: Dp = 8f.dp
    val groupGap: Dp = 4f.dp
    val menuGap: Dp = 4f.dp
    val menuButtonHeight: Dp = 32f.dp
    val submenuIndent: Dp = 14f.dp
    val submenuGap: Dp = 2f.dp
    val submenuButtonHeight: Dp = 28f.dp
}

/** Branded avatar dimensions; the structure and drawing remain Headless-owned. */
enum class ShadcnAvatarSize(val boxSize: Dp, val textSize: Sp, val badgeSize: Dp) {
    Sm(24f.dp, ShadcnTheme.typography.caption, 8f.dp),
    Default(32f.dp, ShadcnTheme.typography.label, 10f.dp),
    Lg(40f.dp, ShadcnTheme.typography.body, 12f.dp),
}

/** Table column metadata is policy, while row/cell layout is Headless behavior. */
data class ShadcnTableColumn(
    val header: String,
    val weight: Float = 1f,
    val align: ShadcnTableCellAlign = ShadcnTableCellAlign.Start,
)

enum class ShadcnTableCellAlign { Start, End }
