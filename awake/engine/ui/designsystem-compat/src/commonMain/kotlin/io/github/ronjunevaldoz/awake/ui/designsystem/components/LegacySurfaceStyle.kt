// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.ShadcnResolvedTheme
import io.github.ronjunevaldoz.awake.ui.style.Style

/**
 * Core-only surface styling used by the deprecated receiver bridge.
 *
 * The public design-system artifact maps these same values through Headless recipes. Keeping this
 * adapter in the compatibility source set preserves token metadata for applications that still
 * use the old Core receiver without leaking Core types back into the public artifact.
 */
internal fun legacyShadcnSurfaceStyle(theme: ShadcnResolvedTheme): Style = Style {
    background(theme.palette.card, tokenId = "card")
    foreground(theme.palette.cardForeground, tokenId = "card-foreground")
    borderWidth(1f.dp)
    borderColor(theme.palette.border, tokenId = "border")
    shape(theme.radii.xl)
    contentPadding(theme.metrics.panelPadding)
}

internal fun legacyShadcnCardStyle(
    theme: ShadcnResolvedTheme,
    hasHeaderOrFooter: Boolean,
): Style = legacyShadcnSurfaceStyle(theme) then if (hasHeaderOrFooter) {
    Style.Empty
} else {
    // Preserve the long-standing body-only card measurement contract: the compact form hugs
    // its body with an 8dp inset on each axis, while full header/body/footer cards use Vega's
    // p-6 panel inset.
    Style { contentPadding(8f.dp) }
}
