// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnMetrics
import io.github.ronjunevaldoz.awake.ui.style.Style

internal fun shadcnSheetSurfaceStyle(values: ShadcnThemeValues, metrics: ShadcnMetrics): Style = Style { background(values.colors.background); foreground(values.colors.foreground); border(1f.dp, values.colors.border); contentPadding(metrics.surfacePadding) }
internal fun shadcnDrawerSurfaceStyle(values: ShadcnThemeValues, metrics: ShadcnMetrics): Style = shadcnSheetSurfaceStyle(values, metrics)
internal fun shadcnDialogSurfaceStyle(values: ShadcnThemeValues, metrics: ShadcnMetrics): Style = Style { background(values.colors.card); foreground(values.colors.cardForeground); border(1f.dp, values.colors.border); shape(values.shapes.lg); contentPadding(metrics.panelPadding) }
