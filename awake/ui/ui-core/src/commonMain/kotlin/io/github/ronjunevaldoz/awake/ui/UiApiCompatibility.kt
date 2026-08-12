// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("DEPRECATION")

package io.github.ronjunevaldoz.awake.ui

/** Temporary source bridge while core implementation files migrate to ui-api contracts. */
@Deprecated("Import io.github.ronjunevaldoz.awake.ui.api.Dp instead.")
typealias Dp = io.github.ronjunevaldoz.awake.ui.api.Dp

/** Temporary source bridge while core implementation files migrate to ui-api contracts. */
@Deprecated("Import io.github.ronjunevaldoz.awake.ui.api.Sp instead.")
typealias Sp = io.github.ronjunevaldoz.awake.ui.api.Sp

@Deprecated("Import io.github.ronjunevaldoz.awake.ui.api.dp instead.")
val Float.dp: Dp get() = io.github.ronjunevaldoz.awake.ui.api.Dp(this)

@Deprecated("Import io.github.ronjunevaldoz.awake.ui.api.dp instead.")
val Int.dp: Dp get() = io.github.ronjunevaldoz.awake.ui.api.Dp(toFloat())

@Deprecated("Import io.github.ronjunevaldoz.awake.ui.api.sp instead.")
val Float.sp: Sp get() = io.github.ronjunevaldoz.awake.ui.api.Sp(this)

@Deprecated("Import io.github.ronjunevaldoz.awake.ui.api.sp instead.")
val Int.sp: Sp get() = io.github.ronjunevaldoz.awake.ui.api.Sp(toFloat())
