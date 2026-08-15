// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.designsystem.LocalShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnMetrics
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.theme

/** Design-system-local ambient theme read, analogous to MaterialTheme's value accessors. */
internal val UiScope.themeValues: UiThemeValues
    get() = primitive.theme

/** Complete design-system-local theme installed by [io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme]. */
internal val UiScope.shadcnTheme: ShadcnThemeValues
    get() = primitive.context.current(LocalShadcnTheme)

/** Metrics resolved at the design-system provider boundary, preserving the selected preset. */
internal val UiScope.shadcnMetrics: ShadcnMetrics
    get() = shadcnTheme.metrics
