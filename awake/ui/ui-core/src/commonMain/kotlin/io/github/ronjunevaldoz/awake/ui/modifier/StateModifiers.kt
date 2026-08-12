// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.modifier

fun UiModifier.forceHover(value: Boolean = true): UiModifier = copy(forceHover = value)
fun UiModifier.forceActive(value: Boolean = true): UiModifier = copy(forceActive = value)
fun UiModifier.forceFocus(value: Boolean = true): UiModifier = copy(forceFocus = value)
fun UiModifier.testTag(tag: String): UiModifier = copy(testTag = tag)
