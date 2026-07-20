// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

import io.github.ronjunevaldoz.awake.core.colors.Color

internal val DslTransparentColor = Color.Transparent

internal fun UiInsets.dslHorizontalPx(): Float = start.toPx() + end.toPx()

internal fun UiInsets.dslVerticalPx(): Float = top.toPx() + bottom.toPx()
