// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.designsystem.LocalShadcnThemeExtension
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnThemeExtension
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnMetrics
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.theme

/** Design-system-local ambient theme read, analogous to MaterialTheme's value accessors. */
internal val UiScope.themeValues: UiThemeValues
    get() = primitive.theme

/** Design-system-local extension installed by [io.github.ronjunevaldoz.awake.ui.designsystem.shadcnTheme]. */
internal val UiScope.shadcnThemeExtension: ShadcnThemeExtension
    get() = primitive.context.current(LocalShadcnThemeExtension)

/** Metrics resolved at the design-system provider boundary, preserving the selected preset. */
internal val UiScope.shadcnMetrics: ShadcnMetrics
    get() = requireNotNull(shadcnThemeExtension.metrics) { "shadcnMetrics requires UiScope.shadcnTheme" }
