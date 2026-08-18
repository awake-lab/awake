// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.tailwind

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.headless.size
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxSize
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.paddingBottom
import io.github.ronjunevaldoz.awake.ui.modifier.paddingTop
import io.github.ronjunevaldoz.awake.ui.modifier.size
import io.github.ronjunevaldoz.awake.ui.modifier.width

/**
 * Tailwind-style fluent layout extension functions wrapping Headless UiModifier primitives.
 */

// Padding
fun UiModifier.p(all: Dp): UiModifier = padding(all)
fun UiModifier.px(horizontal: Dp): UiModifier = padding(horizontal, 0f.dp, horizontal, 0f.dp)
fun UiModifier.py(vertical: Dp): UiModifier = padding(0f.dp, vertical, 0f.dp, vertical)
fun UiModifier.pt(top: Dp): UiModifier = paddingTop(top)
fun UiModifier.pb(bottom: Dp): UiModifier = paddingBottom(bottom)
fun UiModifier.pl(start: Dp): UiModifier = padding(start, 0f.dp, 0f.dp, 0f.dp)
fun UiModifier.pr(end: Dp): UiModifier = padding(0f.dp, 0f.dp, end, 0f.dp)

// Sizing
fun UiModifier.w(width: Dp): UiModifier = width(width)
fun UiModifier.h(height: Dp): UiModifier = height(height)
fun UiModifier.sz(size: Dp): UiModifier = size(size)
fun UiModifier.sz(width: Dp, height: Dp): UiModifier = size(width, height)
fun UiModifier.wFull(): UiModifier = fillMaxWidth()
fun UiModifier.hFull(): UiModifier = fillMaxHeight()
fun UiModifier.sizeFull(): UiModifier = fillMaxSize()

// Corner radius shortcuts
fun UiModifier.rounded(radius: Dp = Tw.Radius.md): UiModifier = this
fun UiModifier.roundedSm(): UiModifier = rounded(Tw.Radius.sm)
fun UiModifier.roundedMd(): UiModifier = rounded(Tw.Radius.md)
fun UiModifier.roundedLg(): UiModifier = rounded(Tw.Radius.lg)
fun UiModifier.roundedXl(): UiModifier = rounded(Tw.Radius.xl)
fun UiModifier.roundedFull(): UiModifier = rounded(Tw.Radius.full)
