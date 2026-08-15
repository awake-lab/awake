// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.Sp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.font.FontWeight
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.style.StyleScope

/**
 * Compatibility-only visual DTO. New Headless APIs use [Style] directly.
 *
 * Kept temporarily so existing consumers can migrate without a binary-breaking flag day.
 */
@Deprecated("Use Style directly", ReplaceWith("Style { /* rules */ }"))
data class SurfaceStyle(
    val background: Color? = null,
    val foreground: Color? = null,
    val border: SurfaceBorder? = null,
    val cornerRadius: Dp? = null,
    val contentPadding: UiInsets = UiInsets.Zero,
    val textSize: Sp? = null,
    val lineHeight: Sp? = null,
    val fontWeight: FontWeight? = null,
    val shadow: SurfaceShadow? = null,
    val scrollThumbColor: Color? = null,
)

@Deprecated("Use Style.shadow")
data class SurfaceShadow(val color: Color, val offsetX: Dp = Dp(0f), val offsetY: Dp = Dp(0f), val blurRadius: Dp = Dp(0f), val spread: Dp = Dp(0f))

@Deprecated("Use Style.border")
data class SurfaceBorder(val width: Dp, val color: Color)

@Deprecated("Use Style directly")
data class SurfaceVisuals(
    val rest: SurfaceStyle = SurfaceStyle(),
    val hovered: SurfaceStyle? = null,
    val pressed: SurfaceStyle? = null,
    val disabled: SurfaceStyle? = null,
)

@Deprecated("Pass Style directly")
internal fun SurfaceStyle.asPrimitiveStyle(): Style = Style { applyLegacy(this@asPrimitiveStyle) }

@Deprecated("Pass Style directly")
internal fun SurfaceVisuals.asPrimitiveStyle(): Style = rest.asPrimitiveStyle() then Style {
    hovered?.let { hovered { applyLegacy(it) } }
    pressed?.let { active { applyLegacy(it) } }
    disabled?.let { disabled { applyLegacy(it) } }
}

private fun StyleScope.applyLegacy(surface: SurfaceStyle) {
    surface.background?.let(::background)
    surface.foreground?.let(::foreground)
    surface.border?.let { border(it.width, it.color) }
    surface.cornerRadius?.let(::shape)
    contentPadding(surface.contentPadding.start, surface.contentPadding.top, surface.contentPadding.end, surface.contentPadding.bottom)
    surface.textSize?.let(::textSize)
    surface.lineHeight?.let(::lineHeight)
    surface.fontWeight?.let(::fontWeight)
    surface.shadow?.let { shadow(it.color, it.offsetX, it.offsetY, it.blurRadius, it.spread) }
}
