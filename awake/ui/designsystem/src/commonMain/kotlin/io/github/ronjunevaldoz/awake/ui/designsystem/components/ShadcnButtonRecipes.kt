// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.visuals
import io.github.ronjunevaldoz.awake.ui.headless.BoxScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.button
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.heightOrDefault
import io.github.ronjunevaldoz.awake.ui.style.Style

fun UiScope.shadcnButton(
    id: String,
    label: String,
    modifier: Modifier = Modifier,
    variant: ShadcnButtonVariant = ShadcnButtonVariant.Primary,
    size: ShadcnButtonSize = ShadcnButtonSize.Md,
    style: Style = Style.Empty,
    centered: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
): Boolean {
    val groupCtx = currentLocal(LocalShadcnButtonGroup)
    val groupStyle = if (groupCtx != null) Style { shape(0f.dp) } else Style.Empty
    val groupModifier = when (groupCtx?.orientation) {
        ShadcnButtonGroupOrientation.Vertical -> modifier.fillMaxWidth()
        ShadcnButtonGroupOrientation.Horizontal -> modifier.fillMaxHeight()
        null -> modifier
    }
    return button(
        id = id,
        label = label,
        modifier = groupModifier.heightOrDefault(size.heightDp),
        style = variant.visuals(themeValues, size) then groupStyle then style,
        centered = centered,
        enabled = enabled,
    ).also { if (it) onClick?.invoke() }
}

fun UiScope.shadcnButton(
    id: String,
    label: String = "",
    modifier: Modifier = Modifier,
    variant: ShadcnButtonVariant = ShadcnButtonVariant.Primary,
    size: ShadcnButtonSize = ShadcnButtonSize.Md,
    style: Style = Style.Empty,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: BoxScope.(slot: UiBounds) -> Unit,
): Boolean {
    val groupCtx = currentLocal(LocalShadcnButtonGroup)
    val groupStyle = if (groupCtx != null) Style { shape(0f.dp) } else Style.Empty
    val groupModifier = when (groupCtx?.orientation) {
        ShadcnButtonGroupOrientation.Vertical -> modifier.fillMaxWidth()
        ShadcnButtonGroupOrientation.Horizontal -> modifier.fillMaxHeight()
        null -> modifier
    }
    return button(
        id = id,
        modifier = groupModifier.heightOrDefault(size.heightDp),
        style = variant.visuals(themeValues, size) then groupStyle then style,
        enabled = enabled,
        content = content,
    ).also { if (it) onClick?.invoke() }
}
