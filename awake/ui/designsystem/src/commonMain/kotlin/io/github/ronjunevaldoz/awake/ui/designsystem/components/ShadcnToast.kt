// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.toast
import io.github.ronjunevaldoz.awake.ui.style.Style

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
    style = Style { background(themeValues.colors.card); foreground(themeValues.colors.cardForeground); border(1f.dp, themeValues.colors.border); shape(themeValues.shapes.lg) },
)
