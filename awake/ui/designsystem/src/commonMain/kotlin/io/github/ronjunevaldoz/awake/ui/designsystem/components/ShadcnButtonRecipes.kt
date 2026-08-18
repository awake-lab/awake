// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.visuals
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiModifier
import io.github.ronjunevaldoz.awake.ui.headless.RowScope
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.button
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.heightOrDefault
import io.github.ronjunevaldoz.awake.ui.headless.size
import io.github.ronjunevaldoz.awake.ui.headless.styleable
import io.github.ronjunevaldoz.awake.ui.style.Style

/**
 * TODO this must be converted to
 *     return shadcnButton(
 *         id = id,
 *         modifier = modifier,
 *         variant = variant,
 *         size = size,
 *         enabled = enabled,
 *         onClick = onClick
 *     ) {
 *         shadcnText(
 *             label = label,
 *             centered = centered
 *         )
 *     }
 */
fun UiScope.shadcnButton(
    id: String,
    label: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnButtonVariant = ShadcnButtonVariant.Primary,
    size: ShadcnButtonSize = ShadcnButtonSize.Md,
    centered: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
): Boolean {
    val groupCtx = currentLocal(LocalShadcnButtonGroup)
    val groupStyle = groupCtx?.let { cornerStyle(it) } ?: Style.Empty
    // Only vertical groups stretch members -- the group's resolved width isn't a real bound
    // until every member's intrinsic width is known, so an explicit fillMaxWidth() is needed to
    // make them share it. Horizontal groups don't need the height equivalent: every member
    // already carries its own heightOrDefault(size.heightDp) below, so uniform-size groups (the
    // common case, matching shadcn's own reference) size correctly without it -- and forcing
    // fillMaxHeight() on every member left the row with no non-FillMax child to hug, so its own
    // WrapContent-height sizing trial fell back to the trial's placeholder bound instead.
    val groupModifier = if (groupCtx?.orientation == ShadcnButtonGroupOrientation.Vertical) {
        modifier.fillMaxWidth()
    } else {
        modifier
    }
    return button(
        id = id,
        label = label,
        modifier = groupModifier.heightOrDefault(size.heightDp),
        style = variant.visuals(themeValues, size) then groupStyle,
        centered = centered,
        enabled = enabled,
    ).also { if (it) onClick?.invoke() }
}

fun UiScope.shadcnButton(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnButtonVariant = ShadcnButtonVariant.Primary,
    size: ShadcnButtonSize = ShadcnButtonSize.Md,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: RowScope.(slot: UiBounds) -> Unit,
): Boolean {
    val groupCtx = currentLocal(LocalShadcnButtonGroup)
    val groupStyle = groupCtx?.let { cornerStyle(it) } ?: Style.Empty
    // Content-lambda buttons (icons, in practice) have no label to derive an intrinsic width
    // from, so buttonSlotInternal's own fallback resolves straight to FillMax -- an icon button
    // dropped into a row without an explicit width stretched to fill it. shadcn's `size="icon"`
    // is always exactly `size-9`, no caller override point either -- square unconditionally.
    val sizedModifier =
        if (size == ShadcnButtonSize.Icon) modifier.size(size.heightDp) else modifier.heightOrDefault(
            size.heightDp
        )
    return button(
        id = id,
        modifier = sizedModifier.styleable(variant.visuals(themeValues, size) then groupStyle),
        enabled = enabled,
        content = content,
    ).also { if (it) onClick?.invoke() }
}
