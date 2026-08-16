// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.context.UiLocal
import io.github.ronjunevaldoz.awake.ui.context.uiLocalOf
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.RowScope
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.UiSeparatorOrientation
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.widthIn
import io.github.ronjunevaldoz.awake.ui.headless.wrapContentWidthOrDefault
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.tailwind.Tw

enum class ShadcnButtonGroupOrientation {
    Horizontal,
    Vertical,
}

internal data class ShadcnButtonGroupContext(
    val orientation: ShadcnButtonGroupOrientation,
)

internal val LocalShadcnButtonGroup: UiLocal<ShadcnButtonGroupContext?> = uiLocalOf(null)

internal fun UiScope.pushLocal(
    local: UiLocal<ShadcnButtonGroupContext?>,
    value: ShadcnButtonGroupContext,
    content: () -> Unit,
) {
    primitive.context.pushLocal(local, value)
    try {
        content()
    } finally {
        primitive.context.popLocal(local)
    }
}

internal fun UiScope.currentLocal(
    local: UiLocal<ShadcnButtonGroupContext?>,
): ShadcnButtonGroupContext? = primitive.context.current(local)

enum class ShadcnButtonGroupPosition {
    First,
    Middle,
    Last,
    Single,
}

fun UiScope.buttonGroupItemStyle(
    position: ShadcnButtonGroupPosition = ShadcnButtonGroupPosition.Middle,
): Style = Style {
    // Buttons inside a joined group have 0dp radius so their active/hover fills
    // cleanly occupy their segment without rounded inner gaps against adjacent buttons.
    shape(0f.dp)
}

/**
 * shadcn's `ButtonGroup`: buttons joined into one control, sharing a single border and outer
 * radius.
 *
 * Ported from `registry/new-york-v4/ui/button-group.tsx` in the pinned shadcn checkout.
 * Supports both [ShadcnButtonGroupOrientation.Horizontal] and
 * [ShadcnButtonGroupOrientation.Vertical] orientations.
 */
fun UiScope.shadcnButtonGroup(
    id: String,
    modifier: Modifier = Modifier,
    orientation: ShadcnButtonGroupOrientation = ShadcnButtonGroupOrientation.Horizontal,
    minWidth: Dp? = if (orientation == ShadcnButtonGroupOrientation.Vertical) 36f.dp else null,
    content: UiScope.() -> Unit,
): UiBounds {
    val effectiveModifier = if (minWidth != null) modifier.widthIn(min = minWidth) else modifier
    return groupSurface(id, effectiveModifier) {
        pushLocal(LocalShadcnButtonGroup, ShadcnButtonGroupContext(orientation)) {
            when (orientation) {
                ShadcnButtonGroupOrientation.Horizontal -> row(
                    horizontalArrangement = Arrangement.spacedBy(0f.dp),
                    verticalAlignment = UiAlignment.Vertical.Center,
                    modifier = Modifier.wrapContentWidthOrDefault(),
                ) { content() }
                ShadcnButtonGroupOrientation.Vertical -> column(
                    verticalArrangement = Arrangement.spacedBy(0f.dp),
                    modifier = Modifier.wrapContentWidthOrDefault(),
                ) { content() }
            }
        }
    }
}

/** Convenience alias for vertical orientation button group. */
fun UiScope.shadcnButtonGroupColumn(
    id: String,
    modifier: Modifier = Modifier,
    content: UiScope.() -> Unit,
): UiBounds = shadcnButtonGroup(
    id = id,
    modifier = modifier,
    orientation = ShadcnButtonGroupOrientation.Vertical,
    content = content,
)

private fun UiScope.groupSurface(
    id: String,
    modifier: Modifier,
    content: ColumnScope.() -> Unit,
): UiBounds = surface(
    id = id,
    modifier = modifier.wrapContentWidthOrDefault(),
    style = Style {
        background(themeValues.colors.card)
        foreground(themeValues.colors.cardForeground)
        border(1f.dp, themeValues.colors.border)
        shape(themeValues.shapes.md)
        // No padding: the children ARE the control's edges, which is what makes the group read
        // as one button rather than a card with buttons in it.
        contentPadding(0f.dp)
    },
) { content() }

/**
 * Hairline divider between members of a group.
 * Automatically reads [LocalShadcnButtonGroup] to pick [UiSeparatorOrientation.Vertical]
 * for horizontal groups and [UiSeparatorOrientation.Horizontal] for vertical groups.
 */
fun UiScope.shadcnButtonGroupSeparator(
    id: String? = null,
    modifier: Modifier = Modifier,
): UiBounds {
    val groupCtx = currentLocal(LocalShadcnButtonGroup)
    val separatorOrientation = when (groupCtx?.orientation) {
        ShadcnButtonGroupOrientation.Vertical -> UiSeparatorOrientation.Horizontal
        else -> UiSeparatorOrientation.Vertical
    }
    return shadcnSeparator(
        id = id,
        modifier = modifier,
        orientation = separatorOrientation,
    )
}
