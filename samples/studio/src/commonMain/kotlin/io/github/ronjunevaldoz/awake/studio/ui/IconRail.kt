// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode
import io.github.ronjunevaldoz.awake.studio.state.StudioContract
import io.github.ronjunevaldoz.awake.ui.UiImageVector
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiBounds
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnIcons
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
 * Floating action rail (Modly-style): a rounded card hugging its icon stack, vertically
 * centered with a margin from the window edge rather than a full-height docked strip.
 * Reset reloads the active example, camera opens the mode/projection menu.
 *
 * It used to carry five tool buttons above these (Layers/Grid/Environment/History/Panels) whose
 * only effect was to look pressed -- nothing read the selected tool. Real transform tools belong
 * here once a viewport gizmo exists to drive; until then this rail holds only actions that do
 * something.
 */
internal fun RowScope.drawIconRail(
    onResetExample: () -> Unit,
    onSelectCameraMode: (CameraMode) -> Unit,
    onSelectCameraProjection: (StudioContract.Projection) -> Unit,
) {
    column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxHeight(),
    ) {
        railCard(id = "studio-tool-rail") {
            railButton(
                id = "studio-tool-reset",
                glyph = ShadcnIcons.arrowPath,
                active = false,
                onClick = onResetExample,
            )
            cameraRailButton(
                onSelectMode = onSelectCameraMode,
                onSelectProjection = onSelectCameraProjection,
            )
        }
    }
}

/** Opens the camera-mode/projection menu anchored to this button -- the same list the
 * viewport's right-click menu shows (see CameraMenu.kt). */
private fun ColumnScope.cameraRailButton(
    onSelectMode: (CameraMode) -> Unit,
    onSelectProjection: (StudioContract.Projection) -> Unit,
) {
    var requestOpen = false
    // Wrapped so the menu has a slot to anchor to -- shadcnButton reports whether it was clicked,
    // not where it landed.
    val bounds = column(modifier = Modifier.width(RailButtonSize)) {
        shadcnButton(
            id = "studio-tool-camera",
            modifier = Modifier.width(RailButtonSize),
            variant = ShadcnButtonVariant.Ghost,
            size = ShadcnButtonSize.Icon,
            onClick = { requestOpen = true },
        ) { icon(ShadcnIcons.camera) }
    }
    // The button carried an empty onClick, so the rail's camera menu never existed -- the id the
    // test looks for was produced by no code at all. Anchored to the button's own bounds so the
    // menu opens beside the rail rather than at the pointer.
    railCameraMenu(
        id = "studio-tool-camera-menu",
        anchor = bounds,
        requestOpen = requestOpen,
        onPick = { index ->
            dispatchCameraMenuPick(
                index,
                onSelectMode = onSelectMode,
                onSelectProjection = onSelectProjection,
            )
        },
    )
}

/** Primary filled square when [active], ghost (transparent, accent on hover) otherwise. */
/**
 * Display toggles, floating at the viewport's RIGHT edge opposite the tool rail.
 *
 * Separate from the tool rail on purpose: that rail is modal (one tool at a time), these are
 * independent booleans, and mixing the two interaction models in one strip serves neither.
 * Blender splits the same way -- tools in the `T` toolbar, shading and overlay toggles floating
 * at the viewport's top right.
 *
 * They lived in the top bar before, which was a scoping error: wireframe and shadows govern how
 * THIS VIEWPORT draws, not the document. Grid and gizmo visibility belong here too when they
 * exist, which is the other reason they are not crammed into the header.
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
                glyph = ShadcnIcons.squares2x2,
                active = wireframe,
                onClick = { onWireframeChange(!wireframe) },
            )
            railButton(
                id = "studio-display-shadows",
                glyph = ShadcnIcons.eye,
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
