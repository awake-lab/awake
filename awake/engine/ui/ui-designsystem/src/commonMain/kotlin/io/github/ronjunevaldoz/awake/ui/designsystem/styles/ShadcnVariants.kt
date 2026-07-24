// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

enum class ShadcnButtonVariant {
    Primary,
    Secondary,
    Outline,
    Ghost,
    Danger,
    Link
}

// Mirrors real shadcn's ButtonSize axis (Xs/Sm/Md/Lg/Icon). Height only -- width still
// comes from the caller's modifier or content, same as every other Awake button call site.
enum class ShadcnButtonSize(val heightDp: Float) {
    Xs(28f),
    Sm(32f),
    Md(40f),
    Lg(44f),
    Icon(40f)
}

enum class ShadcnBadgeVariant {
    Primary,
    Secondary,
    Outline,
    Danger,
    Ghost
}

enum class ShadcnSurfaceVariant {
    Card,
    Sidebar,
    Popover,
    Muted
}

enum class ShadcnTextFieldVariant {
    Default,
    Filled,
    Ghost
}

enum class ShadcnAlertVariant {
    Default,
    Destructive
}
