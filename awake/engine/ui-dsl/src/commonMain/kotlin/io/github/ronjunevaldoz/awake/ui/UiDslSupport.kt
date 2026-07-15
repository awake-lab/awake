// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

@DslMarker
annotation class AwakeUiDsl

internal val DslTransparentColor = floatArrayOf(0f, 0f, 0f, 0f)

internal fun UiInsets.dslHorizontalPx(): Float = start.toPx() + end.toPx()

internal fun UiInsets.dslVerticalPx(): Float = top.toPx() + bottom.toPx()
