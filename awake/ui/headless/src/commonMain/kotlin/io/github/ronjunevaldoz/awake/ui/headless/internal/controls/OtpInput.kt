// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless.internal.controls

/**
 * Filters raw input down to at most [length] digit characters -- the shape any OTP/PIN input
 * needs regardless of skin (letters, symbols, and overflow past the slot count are rejected
 * before a caller's `onValueChange` ever sees them).
 */
fun otpDigitsOnly(rawInput: String, length: Int): String =
    rawInput.filter { it.isDigit() }.take(length)

/** Whether a group separator should render before slot [index] ([groupSize] `0` = no grouping). */
fun otpShowsSeparatorBefore(index: Int, groupSize: Int): Boolean =
    groupSize > 0 && index > 0 && index % groupSize == 0
