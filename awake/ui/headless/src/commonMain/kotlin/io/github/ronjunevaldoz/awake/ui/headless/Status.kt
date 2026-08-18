// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.progress as primitiveProgress
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.skeleton as primitiveSkeleton
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.spinner as primitiveSpinner
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.toast as primitiveToast

/** Neutral progress behavior with caller-provided track/fill visuals. */
fun UiScope.progress(
    id: String,
    value: Float,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
) {
    primitive.primitiveProgress(
        id = id,
        value = value,
        modifier = modifier,
        style = style,
    )
}

/** Neutral loading placeholder with caller-provided surface visuals. */
fun UiScope.skeleton(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    shimmer: Boolean = false,
) {
    primitive.primitiveSkeleton(
        id = id,
        modifier = modifier,
        style = style,
        shimmer = shimmer,
    )
}

/** Neutral animated loading indicator with caller-provided foreground visual. */
fun UiScope.spinner(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
) {
    primitive.primitiveSpinner(
        id = id,
        modifier = modifier,
        style = style,
    )
}

/** Neutral self-dismissing toast behavior with caller-provided surface visuals. */
fun UiScope.toast(
    id: String,
    message: String,
    modifier: UiModifier = Modifier,
    durationMs: Float = 3000f,
    style: Style = Style.Empty,
): Boolean = primitive.primitiveToast(
    id = id,
    message = message,
    modifier = modifier,
    durationMs = durationMs,
    style = style,
)
