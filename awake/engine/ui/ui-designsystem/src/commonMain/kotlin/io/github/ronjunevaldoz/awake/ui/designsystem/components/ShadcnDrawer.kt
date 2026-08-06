// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.Dp
import io.github.ronjunevaldoz.awake.ui.UiPopupResult
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.UiDialogProperties
import io.github.ronjunevaldoz.awake.ui.designsystem.components.popup.shadcnDialog
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier

enum class ShadcnDrawerPosition {
    Bottom, Top, Left, Right
}

/**
 * Real shadcn's `Drawer` (built on vaul/sheet primitives): a slide-over modal overlay panel
 * anchored to the bottom, top, left, or right viewport edge.
 */
fun UiScope.shadcnDrawer(
    id: String,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: UiModifier = Modifier,
    position: ShadcnDrawerPosition = ShadcnDrawerPosition.Bottom,
    sizeDp: Dp = 320f.dp,
    showCloseButton: Boolean = true,
    header: (ColumnScope.(slot: UiBounds) -> Unit)? = null,
    actions: (RowScope.(slot: UiBounds) -> Unit)? = null,
    content: ColumnScope.(slot: UiBounds) -> Unit
): UiPopupResult {
    val panelWidth = when (position) {
        ShadcnDrawerPosition.Bottom, ShadcnDrawerPosition.Top -> Dimension.FillMax
        ShadcnDrawerPosition.Left, ShadcnDrawerPosition.Right -> Dimension.Fixed(sizeDp)
    }
    val panelHeight = when (position) {
        ShadcnDrawerPosition.Bottom, ShadcnDrawerPosition.Top -> Dimension.Fixed(sizeDp)
        ShadcnDrawerPosition.Left, ShadcnDrawerPosition.Right -> Dimension.FillMax
    }

    val result = shadcnDialog(
        id = id,
        expanded = expanded,
        width = panelWidth,
        height = panelHeight,
        properties = UiDialogProperties(dismissOnClickOutside = true),
        showCloseButton = showCloseButton,
        header = header,
        actions = actions,
        content = content
    )

    if (result.dismissed) {
        onDismissRequest()
    }

    return result
}
