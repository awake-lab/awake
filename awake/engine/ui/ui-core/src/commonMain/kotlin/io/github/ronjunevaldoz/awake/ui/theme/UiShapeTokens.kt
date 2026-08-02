// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.theme

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.dp

/**
 * Semantic corner-radius roles, the third theme pillar alongside [UiColorTokens] and
 * [UiTypography] -- mirrors Jetpack Compose's `MaterialTheme.shapes` (`ColorScheme`/
 * `Typography`/`Shapes` are Compose's three swappable theme pillars; [UiTheme] previously only
 * had two). [full] is meant for pill/circular shapes (e.g. a switch track), not a literal
 * radius value a caller should read as a `Dp` to reason about.
 */
interface UiShapeTokens {
    val xs: Dp
    val sm: Dp
    val md: Dp
    val lg: Dp
    val xl: Dp
    val full: Dp
}

/** Neutral fallback shape scale -- same role as [CoreUiTheme]'s* generic color tokens, a
 * reasonable default with no branded opinion. Named authored radius scales (e.g. shadcn's
 * `ShadcnRadiusScale`) belong in `ui-designsystem`, same placement rule as themes/tokens.
 *
 * *[UiFallbackTheme], see that file for the "why ui-core, not ui-designsystem" rationale --
 * applies identically here.
 */
object UiFallbackShapeTokens : UiShapeTokens {
    override val xs: Dp = 2f.dp
    override val sm: Dp = 4f.dp
    override val md: Dp = 6f.dp
    override val lg: Dp = 8f.dp
    override val xl: Dp = 12f.dp
    override val full: Dp = 9999f.dp
}
