// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.tailwind

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.size
import io.github.ronjunevaldoz.awake.ui.headless.width

/**
 * Tailwind-style fluent layout extension functions wrapping Headless Modifier primitives.
 */

// Padding
fun Modifier.p(all: Dp): Modifier = padding(all)
fun Modifier.px(horizontal: Dp): Modifier = padding(start = horizontal, end = horizontal)
fun Modifier.py(vertical: Dp): Modifier = padding(top = vertical, bottom = vertical)
fun Modifier.pt(top: Dp): Modifier = padding(top = top)
fun Modifier.pb(bottom: Dp): Modifier = padding(bottom = bottom)
fun Modifier.pl(start: Dp): Modifier = padding(start = start)
fun Modifier.pr(end: Dp): Modifier = padding(end = end)

// Sizing
fun Modifier.w(width: Dp): Modifier = width(width)
fun Modifier.h(height: Dp): Modifier = height(height)
fun Modifier.sz(size: Dp): Modifier = size(size)
fun Modifier.sz(width: Dp, height: Dp): Modifier = size(width, height)
fun Modifier.wFull(): Modifier = fillMaxWidth()
fun Modifier.hFull(): Modifier = fillMaxHeight()
fun Modifier.sizeFull(): Modifier = fillMaxSize()

// Corner radius shortcuts
fun Modifier.rounded(radius: Dp = Tw.Radius.md): Modifier = this
fun Modifier.roundedSm(): Modifier = rounded(Tw.Radius.sm)
fun Modifier.roundedMd(): Modifier = rounded(Tw.Radius.md)
fun Modifier.roundedLg(): Modifier = rounded(Tw.Radius.lg)
fun Modifier.roundedXl(): Modifier = rounded(Tw.Radius.xl)
fun Modifier.roundedFull(): Modifier = rounded(Tw.Radius.full)
