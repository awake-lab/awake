// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.RowScope
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.column
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.icon
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.headless.width
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.ui.heroicons.icon.HeroIcons

private val RailButtonSize = ShadcnButtonSize.Icon.heightDp
private val RailPadding = 4f.dp
private val RailWidth = RailButtonSize + RailPadding * 2f

/**
 * A rail card: [shadcnSurface]'s look, but padded by [RailPadding] instead of the theme's panel
 * padding.
 *
 * `shadcnSurface` is a PANEL -- 16dp of content padding on every side. A rail is [RailWidth]
 * (= one 32dp icon button plus 4dp either side) wide, so that padding left an 8dp interior for a
 * 32dp button: every icon overflowed to the right, half-clipped by the card, and the card stood
 * ~60dp taller than its two buttons. Same class of bug as `barBand` in StudioToolbar.kt, whose
 * doc comment spells out the panel-vs-band distinction -- a card's declared width must fit its
 * content.
 */
private fun UiScope.railCard(id: String, content: ColumnScope.() -> Unit): UiBounds = surface(
    id = id,
    modifier = Modifier.width(RailWidth),
    style = Style {
        background(StudioTheme.colors.card)
        foreground(StudioTheme.colors.cardForeground)
        border(1f.dp, StudioTheme.colors.border)
        shape(StudioTheme.shapes.lg)
        contentPadding(RailPadding)
    },
) { content() }

/**
 * Display toggles, floating at the viewport's right edge.
 *
 * The only floating rail left. Transform tools are modal (one at a time) and now live in the
 * viewport header as a labelled toggle group; these are independent booleans, and mixing the two
 * interaction models in one strip served neither. Blender splits them the same way.
 *
 * They lived in the top bar before, which was a scoping error: wireframe and shadows govern how
 * THIS VIEWPORT draws, not the document. Grid and gizmo visibility belong here too when they
 * exist.
 */
internal fun RowScope.drawDisplayRail(
    wireframe: Boolean,
    shadows: Boolean,
    onWireframeChange: (Boolean) -> Unit,
    onShadowsChange: (Boolean) -> Unit,
) {
    column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxHeight(),
    ) {
        railCard(id = "studio-display-rail-card") {
            railButton(
                id = "studio-display-wireframe",
                glyph = HeroIcons.Solid20Mini.squares2x2,
                active = wireframe,
                onClick = { onWireframeChange(!wireframe) },
            )
            railButton(
                id = "studio-display-shadows",
                glyph = HeroIcons.Solid20Mini.eye,
                active = shadows,
                onClick = { onShadowsChange(!shadows) },
            )
        }
    }
}

private fun ColumnScope.railButton(
    id: String,
    glyph: UiImageVector,
    active: Boolean,
    onClick: (() -> Unit)?,
) {
    shadcnButton(
        id = id,
        modifier = Modifier.width(RailButtonSize),
        variant = if (active) ShadcnButtonVariant.Primary else ShadcnButtonVariant.Ghost,
        size = ShadcnButtonSize.Icon,
        onClick = onClick,
    ) { icon(glyph) }
}
