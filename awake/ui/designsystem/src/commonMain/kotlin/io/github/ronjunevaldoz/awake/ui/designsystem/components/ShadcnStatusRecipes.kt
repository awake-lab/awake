// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnAlertVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnAlertStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnBadgeStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnKbdStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnProgressStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnSkeletonStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnSpinnerStyle
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.shadcnStatusEmptyStyle
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiModifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiSeparatorOrientation
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.progress
import io.github.ronjunevaldoz.awake.ui.headless.separator
import io.github.ronjunevaldoz.awake.ui.headless.skeleton
import io.github.ronjunevaldoz.awake.ui.headless.spinner
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.text
import io.github.ronjunevaldoz.awake.ui.headless.withIntrinsicLabelSize

/** Branded status pill. Behavior and layout remain owned by ui-headless. */
fun UiScope.shadcnBadge(
    id: String,
    label: String,
    variant: ShadcnBadgeVariant = ShadcnBadgeVariant.Secondary,
): UiBounds {
    val style = themeValues.shadcnBadgeStyle(variant)
    return surface(
        id = id,
        modifier = withIntrinsicLabelSize(label = label, style = style),
        style = style,
    ) { _ ->
        text(label = label, centered = true)
    }
}

/** Branded key-cap pill matching official shadcn `kbd.tsx` (`h-5 text-[10px] py-0`). */
fun UiScope.shadcnKbd(
    id: String,
    label: String,
    modifier: UiModifier = Modifier,
): UiBounds {
    val style = shadcnKbdStyle(themeValues)
    return surface(
        id = id,
        verticalArrangement = Arrangement.Center,
        modifier = withIntrinsicLabelSize(label = label, modifier = modifier.height(20f.dp), style = style),
        style = style,
    ) { _ ->
        text(label = label, centered = true)
    }
}

fun UiScope.shadcnSeparator(
    modifier: UiModifier = Modifier,
    thickness: io.github.ronjunevaldoz.awake.ui.api.Dp = 1f.dp,
    orientation: UiSeparatorOrientation = UiSeparatorOrientation.Horizontal,
    id: String? = null,
): UiBounds = separator(
    modifier = modifier,
    thickness = thickness,
    orientation = orientation,
    color = themeValues.colors.border,
    id = id,
)

fun UiScope.shadcnProgress(
    id: String,
    value: Float,
    modifier: UiModifier = Modifier,
): Unit = progress(
    id = id,
    value = value,
    modifier = modifier,
    style = shadcnProgressStyle(themeValues),
)

fun UiScope.shadcnSkeleton(
    id: String,
    modifier: UiModifier = Modifier,
    shimmer: Boolean = false,
): Unit = skeleton(
    id = id,
    modifier = modifier,
    shimmer = shimmer,
    style = shadcnSkeletonStyle(themeValues),
)

fun UiScope.shadcnSpinner(
    id: String,
    modifier: UiModifier = Modifier,
): Unit = spinner(
    id = id,
    modifier = modifier,
    style = shadcnSpinnerStyle(themeValues),
)

fun UiScope.shadcnAlert(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnAlertVariant = ShadcnAlertVariant.Default,
    content: ColumnScope.() -> Unit,
): UiBounds = surface(
    id = id,
    modifier = modifier.fillMaxWidth(),
    style = shadcnAlertStyle(themeValues, variant),
) {
    column(verticalArrangement = Arrangement.spacedBy(4f.dp)) {
        content()
    }
}

fun UiScope.shadcnAlert(
    id: String,
    title: String,
    description: String? = null,
    modifier: UiModifier = Modifier,
    variant: ShadcnAlertVariant = ShadcnAlertVariant.Default,
): UiBounds = shadcnAlert(
    id = id,
    modifier = modifier,
    variant = variant,
) {
    shadcnText(title, emphasis = ShadcnTextEmphasis.Medium)
    if (description != null) {
        shadcnText(description, style = ShadcnTextStyle.Caption)
    }
}

fun UiScope.shadcnEmpty(
    id: String,
    title: String,
    description: String? = null,
    modifier: UiModifier = Modifier,
    action: (ColumnScope.() -> Unit)? = null,
): UiBounds = surface(
    id = id,
    modifier = modifier.fillMaxWidth(),
    style = shadcnStatusEmptyStyle(),
) {
    column(
        horizontalAlignment = io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment.Horizontal.Center,
        verticalArrangement = Arrangement.spacedBy(8f.dp),
    ) {
        shadcnText(
            title,
            style = ShadcnTextStyle.Body,
            centered = true,
            emphasis = ShadcnTextEmphasis.Medium,
        )
        if (description != null) {
            shadcnText(
                description,
                centered = true,
                style = ShadcnTextStyle.Caption,
            )
        }
        if (action != null) {
            column(
                horizontalAlignment = io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment.Horizontal.Center,
                verticalArrangement = Arrangement.spacedBy(4f.dp),
            ) {
                action()
            }
        }
    }
}
