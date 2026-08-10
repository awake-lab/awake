// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.tailwind.Tw

enum class ShadcnButtonVariant {
    Primary,
    Secondary,
    Outline,
    Ghost,
    Danger,
    Link,
}

// Mirrors real shadcn's ButtonSize axis (Xs/Sm/Md/Lg/Icon). Height only -- width still
// comes from the caller's modifier or content, same as every other Awake button call site.
// Md/Lg/Icon match shadcn's own h-9/h-10/size-9 (36/40/36px) -- Xs/Sm have no direct shadcn
// counterpart and are kept as this module's existing custom scale.
enum class ShadcnButtonSize(val heightDp: Dp) {
    // Xs has no direct shadcn counterpart -- this module's own custom scale step, not part of
    // Tw's generated Tailwind scale.
    Xs(28f.dp),
    Sm(Tw.Spacing.s8), // h-8
    Md(Tw.Spacing.s9), // h-9
    Lg(Tw.Spacing.s10), // h-10
    Icon(Tw.Spacing.s9), // size-9
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
