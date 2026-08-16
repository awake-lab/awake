// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.designsystem.LocalShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnMetrics
import io.github.ronjunevaldoz.awake.ui.headless.UiScope

/** Design-system-local ambient theme read, analogous to MaterialTheme's value accessors. */
internal val UiScope.themeValues: ShadcnThemeValues
    get() = shadcnTheme

/** Complete design-system-local theme installed by [io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme]. */
internal val UiScope.shadcnTheme: ShadcnThemeValues
    get() = requireNotNull(primitive.context.current(LocalShadcnTheme)) {
        "Shadcn components require a surrounding shadcnTheme { ... } scope."
    }

/** Metrics resolved at the design-system provider boundary, preserving the selected preset. */
internal val UiScope.shadcnMetrics: ShadcnMetrics
    get() = shadcnTheme.metrics
