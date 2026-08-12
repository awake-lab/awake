// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.ui.api.theme.UiThemeValues
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceVisuals

/**
 * Applies standard muted foreground disabled styling to [SurfaceVisuals].
 */
fun SurfaceVisuals.withDisabledDim(theme: UiThemeValues): SurfaceVisuals = copy(
    disabled = (disabled ?: SurfaceStyle()).copy(
        foreground = theme.colors.mutedForeground,
    ),
)
