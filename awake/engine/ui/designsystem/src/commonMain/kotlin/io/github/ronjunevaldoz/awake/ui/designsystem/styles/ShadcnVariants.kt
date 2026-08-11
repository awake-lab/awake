// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.tailwind.Tw

enum class ShadcnButtonVariant {
    Primary,
    Secondary,
    Outline,
    Ghost,
    Danger,
    Link,
}

// Mirrors real shadcn's ButtonSize axis. Height AND horizontal padding both come from
// button.tsx's own buttonVariants.size entries -- verified 2026-08-10 against the pinned
// reference, which is also where the `xs` height came from (this previously read 28dp with a
// comment claiming xs had "no direct shadcn counterpart"; current source defines it as h-6).
//   xs      -> "h-6 gap-1 rounded-md px-2 text-xs"      -> 24dp / px-2
//   sm      -> "h-8 gap-1.5 rounded-md px-3"            -> 32dp / px-3
//   default -> "h-9 px-4 py-2"                          -> 36dp / px-4
//   lg      -> "h-10 rounded-md px-6"                   -> 40dp / px-6
//   icon    -> "size-9"                                 -> 36dp / no horizontal padding
enum class ShadcnButtonSize(val heightDp: Dp, val paddingX: Dp) {
    Xs(Tw.Spacing.s6, Tw.Spacing.s2),
    Sm(Tw.Spacing.s8, Tw.Spacing.s3),
    Md(Tw.Spacing.s9, Tw.Spacing.s4),
    Lg(Tw.Spacing.s10, Tw.Spacing.s6),
    Icon(Tw.Spacing.s9, Tw.Spacing.s0),
}

enum class ShadcnBadgeVariant {
    Primary,
    Secondary,
    Outline,
    Danger,
    Ghost,
}

enum class ShadcnSurfaceVariant {
    Muted,
}

enum class ShadcnTextFieldVariant {
    Default,
    Filled,
    Ghost,
}

enum class ShadcnAlertVariant {
    Default,
    Destructive,
}

enum class ShadcnCardVariant {
    Default,
    Elevated,
}

/** Mirrors shadcn-compose's `CardSize` -- controls header/footer divider spacing only,
 * everything else about a card's own layout is unaffected. */
enum class ShadcnCardSize(val dividerGapDp: Float) {
    Compact(2f),
    Default(4f),
}

// Mirrors real shadcn's `ToggleVariant` (Default/Outline) -- Outline reuses the same bordered
// treatment as ShadcnButtonVariant.Outline (see UiButtonVariant.Outline in shadcnToggle()).
enum class ShadcnToggleVariant {
    Default,
    Outline,
}
