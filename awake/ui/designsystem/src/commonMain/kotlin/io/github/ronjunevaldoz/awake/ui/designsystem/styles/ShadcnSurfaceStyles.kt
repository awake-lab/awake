// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.styles

import io.github.ronjunevaldoz.awake.ui.UiSpacing
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnThemeValues
import io.github.ronjunevaldoz.awake.ui.designsystem.theme.ShadcnMetrics
import io.github.ronjunevaldoz.awake.ui.style.Style

/**
 * Reproduces exactly what ui-core's now-deleted ambient `theme.components.surface` used to
 * resolve to for a bare, unstyled `surface()` call under [ShadcnTheme] -- NOT a recommended look
 * for new code (see [shadcnCardStyle]/[shadcnSurfaceStyle] for the intentional, named variants).
 * Exists only so [io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnInputOTP]'s slot
 * wrapper (the one remaining pre-existing caller that never supplied its own style) keeps
 * rendering pixel-identically now that the ambient fallback it was accidentally relying on is
 * gone.
 *
 * Deliberately does NOT set `shape`/`borderWidth`/`borderColor` -- `surface()`'s own base style
 * (`Style { shape(UiShape.md); borderWidth(UiShape.none) }`, see `layouts/Surface.kt`) is applied
 * as this call's `style`, not as a `defaults` the ambient value could ever have overridden, so
 * the true previous render already had `shape = UiShape.md` (Core's neutral 6dp, not Shadcn's
 * `shapes.xl`) and no visible border (`borderWidth = 0`) despite the ambient default nominally
 * setting both -- verified by rendering both and comparing (see the parent task's probe-test
 * discipline). Setting either here would be a real, novel visual change, not a reproduction.
 */
internal fun shadcnLegacyAmbientSurfaceStyle(values: ShadcnThemeValues): Style = Style {
    background(values.colors.card, "card")
    foreground(values.colors.cardForeground, "card-foreground")
    contentPadding(UiSpacing.sm)
}

internal fun shadcnSurfaceStyle(values: ShadcnThemeValues, metrics: ShadcnMetrics, variant: ShadcnSurfaceVariant?): Style =
    when (variant) {
        ShadcnSurfaceVariant.Muted -> Style {
            background(values.colors.muted)
            foreground(values.colors.foreground)
            shape(values.shapes.lg)
            contentPadding(metrics.surfacePadding)
        }

        else -> Style {
            background(values.colors.card)
            foreground(values.colors.cardForeground)
            border(1f.dp, values.colors.border)
            shape(values.shapes.lg)
            contentPadding(metrics.panelPadding)
        }
    }

internal fun shadcnPopoverContentStyle(values: ShadcnThemeValues, metrics: ShadcnMetrics): Style = Style {
    background(values.colors.popover)
    foreground(values.colors.popoverForeground)
    border(1f.dp, values.colors.border)
    shape(values.shapes.md)
    contentPadding(metrics.surfacePadding)
}
