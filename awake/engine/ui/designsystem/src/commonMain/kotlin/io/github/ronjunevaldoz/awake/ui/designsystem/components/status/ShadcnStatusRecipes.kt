// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.status

import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.RowScope
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.progress
import io.github.ronjunevaldoz.awake.ui.headless.skeleton
import io.github.ronjunevaldoz.awake.ui.headless.spinner
import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp

private fun progressVisuals(scope: UiScope): SurfaceStyle = SurfaceStyle(
    // shadcn Progress is a muted track with a primary indicator. The headless primitive paints
    // `background` first and `foreground` over the value fraction, so swapping these values
    // produces the canonical `bg-primary/20` + `bg-primary` treatment rather than a white fill.
    background = scope.themeValues.colors.primary.withAlpha(0.2f),
    foreground = scope.themeValues.colors.primary,
    // Progress is not a slider: upstream uses only `h-2 ... rounded-full bg-primary/20`, with
    // no input border. Explicitly clearing the generic slider fallback is therefore required.
    border = SurfaceBorder(0f.dp, Color.Transparent),
    cornerRadius = scope.themeValues.shapes.full,
)

private fun progressVisuals(scope: ColumnScope): SurfaceStyle = SurfaceStyle(
    background = scope.themeValues.colors.primary.withAlpha(0.2f),
    foreground = scope.themeValues.colors.primary,
    border = SurfaceBorder(0f.dp, Color.Transparent),
    cornerRadius = scope.themeValues.shapes.full,
)

private fun progressVisuals(scope: RowScope): SurfaceStyle = SurfaceStyle(
    background = scope.themeValues.colors.primary.withAlpha(0.2f),
    foreground = scope.themeValues.colors.primary,
    border = SurfaceBorder(0f.dp, Color.Transparent),
    cornerRadius = scope.themeValues.shapes.full,
)

fun UiScope.shadcnProgress(id: String, value: Float, modifier: Modifier = Modifier) =
    progress(id, value, modifier, progressVisuals(this))

fun ColumnScope.shadcnProgress(id: String, value: Float, modifier: Modifier = Modifier) =
    progress(id, value, modifier, progressVisuals(this))

fun RowScope.shadcnProgress(id: String, value: Float, modifier: Modifier = Modifier) =
    progress(id, value, modifier, progressVisuals(this))

fun ColumnScope.shadcnSkeleton(id: String, modifier: Modifier = Modifier) = skeleton(
    id = id,
    modifier = modifier,
    visuals = SurfaceStyle(background = themeValues.colors.muted, cornerRadius = themeValues.shapes.md),
    shimmer = true,
)

fun RowScope.shadcnSkeleton(id: String, modifier: Modifier = Modifier) = skeleton(
    id = id,
    modifier = modifier,
    visuals = SurfaceStyle(background = themeValues.colors.muted, cornerRadius = themeValues.shapes.md),
    shimmer = true,
)

fun ColumnScope.shadcnSpinner(id: String, modifier: Modifier = Modifier) = spinner(
    id = id,
    modifier = modifier,
    visuals = SurfaceStyle(foreground = themeValues.colors.primary),
)

fun RowScope.shadcnSpinner(id: String, modifier: Modifier = Modifier) = spinner(
    id = id,
    modifier = modifier,
    visuals = SurfaceStyle(foreground = themeValues.colors.primary),
)
