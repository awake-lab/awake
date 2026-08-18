// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.style.Style

internal fun shadcnFieldLabelStyle(values: ShadcnThemeValues, disabled: Boolean): Style =
    shadcnTextStyle(if (disabled) values.colors.mutedForeground else values.colors.foreground, values.typography.label)
internal fun shadcnFieldDescriptionStyle(values: ShadcnThemeValues): Style =
    shadcnTextStyle(values.colors.mutedForeground, values.typography.caption)
internal fun shadcnFieldErrorStyle(values: ShadcnThemeValues): Style =
    shadcnTextStyle(values.colors.destructive, values.typography.caption)
internal fun shadcnFieldSeparatorStyle(values: ShadcnThemeValues): Style = Style { background(values.colors.border) }
