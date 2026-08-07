// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components.status

import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnStyles
import io.github.ronjunevaldoz.awake.ui.headless.input.progress
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.style.*
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.theme.UiTheme

private fun shadcnProgressTrackStyle(theme: UiTheme, style: Style): Style =
    ShadcnStyles.slider(theme.asShadcnTheme()) then style

/** Real shadcn's `Progress`: a filled track showing [value] (0..1), sharing its track/fill
 * chrome with [shadcnSlider]. Delegates entirely to [progress]. */
fun UiScope.shadcnProgress(
    id: String,
    value: Float,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
): Unit = progress(
    id = id,
    value = value,
    modifier = modifier,
    style = shadcnProgressTrackStyle(theme, style),
)
