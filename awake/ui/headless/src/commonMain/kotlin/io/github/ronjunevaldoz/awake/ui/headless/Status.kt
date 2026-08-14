// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.headless

import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.progress as primitiveProgress
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.skeleton as primitiveSkeleton
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.spinner as primitiveSpinner
import io.github.ronjunevaldoz.awake.ui.headless.internal.controls.toast as primitiveToast

/** Neutral progress behavior with caller-provided track/fill visuals. */
fun UiScope.progress(
    id: String,
    value: Float,
    modifier: Modifier = Modifier,
    visuals: SurfaceStyle = SurfaceStyle(),
) {
    primitive.primitiveProgress(
        id = id,
        value = value,
        modifier = modifier.asPrimitiveModifier(),
        style = visuals.asPrimitiveStyle(),
    )
}

/** Neutral loading placeholder with caller-provided surface visuals. */
fun UiScope.skeleton(
    id: String,
    modifier: Modifier = Modifier,
    visuals: SurfaceStyle = SurfaceStyle(),
    shimmer: Boolean = false,
) {
    primitive.primitiveSkeleton(
        id = id,
        modifier = modifier.asPrimitiveModifier(),
        style = visuals.asPrimitiveStyle(),
        shimmer = shimmer,
    )
}

/** Neutral animated loading indicator with caller-provided foreground visual. */
fun UiScope.spinner(
    id: String,
    modifier: Modifier = Modifier,
    visuals: SurfaceStyle = SurfaceStyle(),
) {
    primitive.primitiveSpinner(
        id = id,
        modifier = modifier.asPrimitiveModifier(),
        style = visuals.asPrimitiveStyle(),
    )
}

/** Neutral self-dismissing toast behavior with caller-provided surface visuals. */
fun UiScope.toast(
    id: String,
    message: String,
    modifier: Modifier = Modifier,
    durationMs: Float = 3000f,
    visuals: SurfaceStyle = SurfaceStyle(),
): Boolean = primitive.primitiveToast(
    id = id,
    message = message,
    modifier = modifier.asPrimitiveModifier(),
    durationMs = durationMs,
    style = visuals.asPrimitiveStyle(),
)
