// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.context.UiLocal
import io.github.ronjunevaldoz.awake.ui.context.uiLocalOf
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnMetrics

/**
 * Design-system-owned customization supplied once by [shadcnTheme].
 *
 * This is intentionally separate from [io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues]:
 * Core owns neutral theme contracts, while named shadcn component policy belongs here.
 */
data class ShadcnThemeExtension(
    /** Optional density override; [shadcnTheme] supplies the selected preset's metrics by default. */
    val metrics: ShadcnMetrics? = null,
    val text: ShadcnTextTheme = ShadcnTextTheme(),
)

/** Named color roles used by shadcn typography recipes. Null retains the installed theme value. */
data class ShadcnTextTheme(
    val default: Color? = null,
    val muted: Color? = null,
    val destructive: Color? = null,
)

/** Internal CompositionLocal-like carrier; only [ShadcnThemeExtension] is public policy. */
internal val LocalShadcnThemeExtension: UiLocal<ShadcnThemeExtension> = uiLocalOf(ShadcnThemeExtension())
