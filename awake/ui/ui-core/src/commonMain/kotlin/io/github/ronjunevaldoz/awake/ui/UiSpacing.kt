// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/** Named spacing scale, in [Dp] -- not wired into [io.github.ronjunevaldoz.awake.ui.modifier.UiModifier] yet (that grows only when a
 * real padding/margin need shows up), just replaces bare gap literals with a named scale.
 * */
object UiSpacing {
    val xs: Dp = 4f.dp
    val sm: Dp = 8f.dp
    val md: Dp = 16f.dp
    val lg: Dp = 24f.dp
    val xl: Dp = 32f.dp
}
