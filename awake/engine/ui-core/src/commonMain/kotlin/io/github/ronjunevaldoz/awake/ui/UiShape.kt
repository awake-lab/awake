// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/**
 * Corner-radius scale derived from one tunable base -- shadcn/ui's `--radius` convention:
 * sm/md are offset down from base, lg IS base, xl is offset up. A consumer retuning the whole
 * app's roundness changes one number, not four independent constants to keep in sync.
 */
object UiShape {
    var base: Dp = 8f.dp
    val sm: Dp get() = (base.value - 4f).coerceAtLeast(0f).dp
    val md: Dp get() = (base.value - 2f).coerceAtLeast(0f).dp
    val lg: Dp get() = base
    val xl: Dp get() = (base.value + 4f).dp
    val pill: Dp = 9999f.dp
    val none: Dp = 0f.dp
}
