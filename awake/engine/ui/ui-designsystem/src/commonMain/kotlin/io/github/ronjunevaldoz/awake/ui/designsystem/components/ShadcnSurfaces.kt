// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.ui.designsystem.components

import io.github.ronjunevaldoz.awake.ui.modifier.UiModifier
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.scope.UiSlot
import io.github.ronjunevaldoz.awake.ui.designsystem.asShadcnTheme
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnStyles
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.layouts.BoxScope
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.layouts.spacer
import io.github.ronjunevaldoz.awake.ui.layouts.surface
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.separator
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.*
import io.github.ronjunevaldoz.awake.ui.style.*

/** Inserts the header/footer separator convention shared with [DropdownMenu]'s
 * item separator: a thin border-colored rule with a small gap on both sides. */
private fun ColumnScope.shadcnCardDivider() {
    spacer(Modifier.height(4f.dp))
    separator(color = theme.tokens.border.withAlpha(0.72f))
    spacer(Modifier.height(4f.dp))
}

/** Composes the shared header -> body -> footer structure on top of an already-styled
 * [surface] content slot. Header/footer are optional; body is required. Matches real
 * shadcn's `CardHeader`/`CardContent`/`CardFooter` composition. */
private fun ColumnScope.shadcnCardContent(
    slot: UiSlot,
    header: (ColumnScope.() -> Unit)?,
    footer: (ColumnScope.() -> Unit)?,
    body: ColumnScope.(slot: UiSlot) -> Unit
) {
    if (header != null) {
        header()
        shadcnCardDivider()
    }
    body(slot)
    if (footer != null) {
        shadcnCardDivider()
        footer()
    }
}

/** Real shadcn's `Surface`: a contained region (Card, Popover, Dialog) that owns its
 * background, border, and content padding. Composed from the [surface] primitive. */
fun UiScope.shadcnSurface(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnSurfaceVariant? = null,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = surface(
    id = id,
    modifier = modifier,
    style = (if (variant == null) theme.asShadcnTheme().components.surface else ShadcnStyles.surface(theme.asShadcnTheme(), variant)) then style,
    content = content
)

/** [shadcnSurface] override for [ColumnScope]. */
fun ColumnScope.shadcnSurface(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnSurfaceVariant? = null,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = surface(
    id = id,
    modifier = modifier,
    style = (if (variant == null) theme.asShadcnTheme().components.surface else ShadcnStyles.surface(theme.asShadcnTheme(), variant)) then style,
    content = content
)

/** [shadcnSurface] override for [RowScope]. */
fun RowScope.shadcnSurface(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnSurfaceVariant? = null,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = surface(
    id = id,
    modifier = modifier,
    style = (if (variant == null) theme.asShadcnTheme().components.surface else ShadcnStyles.surface(theme.asShadcnTheme(), variant)) then style,
    content = content
)

/** [shadcnSurface] override for [BoxScope]. */
fun BoxScope.shadcnSurface(
    id: String,
    modifier: UiModifier = Modifier,
    variant: ShadcnSurfaceVariant? = null,
    style: Style = Style.Empty,
    content: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = surface(
    id = id,
    modifier = modifier,
    style = (if (variant == null) theme.asShadcnTheme().components.surface else ShadcnStyles.surface(theme.asShadcnTheme(), variant)) then style,
    content = content
)

/** Real shadcn's `Card`: a dedicated header/body/footer composition, not just a
 * background/border flavor of [shadcnSurface]. Header and footer are optional slots
 * separated from the body by the shared divider convention (see [DropdownMenu]'s item
 * separator); body is required. Uses the base theme surface style directly -- it isn't
 * a [ShadcnSurfaceVariant] flavor, just a plain surface with header/body/footer structure. */
fun UiScope.shadcnCard(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    body: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = surface(
    id = id,
    modifier = modifier,
    style = theme.asShadcnTheme().components.surface then style,
    content = { slot -> shadcnCardContent(slot, header, footer, body) }
)

/** [shadcnCard] override for [ColumnScope]. */
fun ColumnScope.shadcnCard(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    body: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = surface(
    id = id,
    modifier = modifier,
    style = theme.asShadcnTheme().components.surface then style,
    content = { slot -> shadcnCardContent(slot, header, footer, body) }
)

/** [shadcnCard] override for [RowScope]. */
fun RowScope.shadcnCard(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    body: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = surface(
    id = id,
    modifier = modifier,
    style = theme.asShadcnTheme().components.surface then style,
    content = { slot -> shadcnCardContent(slot, header, footer, body) }
)

/** [shadcnCard] override for [BoxScope]. */
fun BoxScope.shadcnCard(
    id: String,
    modifier: UiModifier = Modifier,
    style: Style = Style.Empty,
    header: (ColumnScope.() -> Unit)? = null,
    footer: (ColumnScope.() -> Unit)? = null,
    body: ColumnScope.(slot: UiSlot) -> Unit
): UiSlot = surface(
    id = id,
    modifier = modifier,
    style = theme.asShadcnTheme().components.surface then style,
    content = { slot -> shadcnCardContent(slot, header, footer, body) }
)
