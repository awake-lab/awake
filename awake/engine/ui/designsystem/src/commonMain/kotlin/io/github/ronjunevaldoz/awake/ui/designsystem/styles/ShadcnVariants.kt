// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

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
enum class ShadcnButtonSize(val heightDp: Float) {
    Xs(28f),
    Sm(32f),
    Md(36f),
    Lg(40f),
    Icon(36f),
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
