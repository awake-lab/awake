// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.ui.context.UiLocal
import io.github.ronjunevaldoz.awake.ui.context.uiLocalOf
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnMetrics

/** Complete design-system theme supplied by [shadcnTheme] to a UI subtree. */
data class ShadcnThemeValues(
    val resolved: ShadcnResolvedTheme,
) : ShadcnResolvedTheme by resolved {
    /** Neutral Core contract installed by [shadcnTheme]; it is never reconstructed from Core. */
    val core get() = resolved
    override val metrics: ShadcnMetrics get() = resolved.metrics
}

/** Null outside [shadcnTheme]; Shadcn recipes require this local. */
internal val LocalShadcnTheme: UiLocal<ShadcnThemeValues?> = uiLocalOf(null)
