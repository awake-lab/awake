package io.github.ronjunevaldoz.awake.ui.layouts

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.dp

/** Named spacing scale, in [io.github.ronjunevaldoz.awake.ui.Dp] -- not wired into [io.github.ronjunevaldoz.awake.ui.UiModifier] yet (that grows only when a
 * real padding/margin need shows up), just replaces bare gap literals with a named scale.
 * TODO this is a token
 * */
object UiSpacing {
    val xs: Dp = 4f.dp
    val sm: Dp = 8f.dp
    val md: Dp = 16f.dp
    val lg: Dp = 24f.dp
    val xl: Dp = 32f.dp
}