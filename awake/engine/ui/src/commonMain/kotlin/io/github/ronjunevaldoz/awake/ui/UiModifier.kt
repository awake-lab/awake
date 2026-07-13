// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui

/**
 * Per-widget-call size override -- `null` means "use whatever the enclosing [UiScope]
 * (e.g. a [ColumnScope]'s configured width) provides by default". Deliberately minimal:
 * `align`/`padding`/`background` are real extension points this shape supports, but nothing
 * in this repo needs them yet.
 */
data class UiModifier(val width: Float? = null, val height: Float? = null)

fun UiModifier.size(width: Float, height: Float): UiModifier = copy(width = width, height = height)
