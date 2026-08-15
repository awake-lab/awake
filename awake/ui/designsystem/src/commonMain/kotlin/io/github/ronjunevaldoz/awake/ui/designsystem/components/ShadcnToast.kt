// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnToastStyle
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.toast

/** Shadcn toast recipe backed by Headless self-dismissal behavior. */
fun UiScope.shadcnToast(
    id: String,
    message: String,
    modifier: Modifier = Modifier,
    durationMs: Float = 3000f,
): Boolean = toast(
    id = id,
    message = message,
    modifier = modifier,
    durationMs = durationMs,
    style = shadcnToastStyle(themeValues),
)
