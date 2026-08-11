// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.Sp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnTheme

internal val SHADCN_CHECKBOX_CORNER_RADIUS = 4f.dp

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
