// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.context.UiLocal
import io.github.ronjunevaldoz.awake.ui.context.uiLocalOf
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnMetrics

/** Complete design-system theme supplied by [shadcnTheme] to a UI subtree. */
data class ShadcnThemeValues(
    val core: UiThemeValues,
    val metrics: ShadcnMetrics,
) : UiThemeValues by core

internal val LocalShadcnTheme: UiLocal<ShadcnThemeValues> = uiLocalOf(
    ShadcnThemeValues(core = ShadcnTheme, metrics = ShadcnTheme.metrics),
)
