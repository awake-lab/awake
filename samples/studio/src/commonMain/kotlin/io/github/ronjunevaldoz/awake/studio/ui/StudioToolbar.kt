// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.ui.api.Dp
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnTextTone
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSurface
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnSurfaceVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.surface
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.row

// internal, not private -- StudioShell.kt reads both to size the panels group's own height
// explicitly (see its doc comment for why that's computed rather than a weight(1f) fill).
internal val TOP_BAR_HEIGHT = 44f.dp
internal val STATUS_BAR_HEIGHT = 26f.dp
private val BAR_INSET = 12f.dp

/** Full-width top bar: title area at the left, a play/replay action centered, the wireframe/
 * shadow toggles at the right -- a plain row, not a card, since it already sits directly on the
 * shell's own background and a hairline (drawn by the caller, see `drawStudioShellBody`) is the
 * bottom border. Replaces the old floating [shadcnCard] toolbar that used to sit inside the
 * viewport. */
internal fun UiScope.drawStudioTopBar(onPlay: () -> Unit) {
    barBand(id = "studio-top-bar", height = TOP_BAR_HEIGHT) {
        row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = UiAlignment.Vertical.Center,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        ) {
            shadcnText("Awake Studio")
            shadcnButton(
                id = "studio-top-bar-play",
                label = "Play",
                modifier = Modifier.height(ShadcnButtonSize.Icon.heightDp),
                variant = ShadcnButtonVariant.Ghost,
                size = ShadcnButtonSize.Icon,
                onClick = onPlay,
            )
        }
    }
}

/** Full-width status bar: edit-mode label at the left, the active render backend at the right --
 * muted caption typography, matching a docked IDE's bottom strip. The backend is a fixed label,
 * not read live from the running window -- nothing in this `ui` package can reach that without
 * depending on `app`. */
internal fun UiScope.drawStudioStatusBar() {
    barBand(id = "studio-status-bar", height = STATUS_BAR_HEIGHT) {
        row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = UiAlignment.Vertical.Center,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        ) {
            shadcnText("Edit mode", tone = ShadcnTextTone.Muted)
            shadcnBadge(id = "studio-status-backend", label = "Vulkan", variant = ShadcnBadgeVariant.Outline)
        }
    }
}

/**
 * A full-bleed shell band: exactly [height] tall, muted, square-cornered, inset horizontally
 * only.
 *
 * Not `shadcnSurface`: that is a CARD (rounded lg, 16dp padding on every side), and wrapping a
 * fixed-height row in one made each band 32px taller than the constant that names it --
 * `drawStudioShellBody` subtracts those constants to size the workspace, so the shell overflowed
 * its frame by exactly the two bands' padding (64px) and pushed the status bar off-screen.
 * A band's declared height must BE its rendered height.
 */
private fun UiScope.barBand(id: String, height: Dp, content: ColumnScope.() -> Unit) {
    surface(
        id = id,
        modifier = Modifier.fillMaxWidth().height(height),
        style = Style {
            background(StudioTheme.colors.muted)
            shape(0f.dp)
            contentPadding(BAR_INSET, 0f.dp, BAR_INSET, 0f.dp)
        },
    ) { content() }
}
