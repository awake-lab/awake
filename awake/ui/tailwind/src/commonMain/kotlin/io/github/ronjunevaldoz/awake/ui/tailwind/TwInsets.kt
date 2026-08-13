// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.tailwind

import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.api.layout.tw

/**
 * Grid-based construction helpers for [UiInsets] avoiding ambiguous parameter names.
 */
fun UiInsets.Companion.grid(horizontal: Number = 0, vertical: Number = 0): UiInsets = UiInsets(
    horizontal = horizontal.tw,
    vertical = vertical.tw,
)

fun UiInsets.Companion.grid(all: Number): UiInsets = UiInsets(all = all.tw)
