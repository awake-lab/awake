// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.api.layout.UiInsets
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceBorder
import io.github.ronjunevaldoz.awake.ui.headless.SurfaceStyle
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.surface

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
    // Identified so the shell's full-bleed invariant stays checkable. A bare row() threads
    // testTag for layout debugging but never records a semantic node, so StudioShellLayoutTest
    // could not see this bar at all and asserted on null. Transparent, zero-padding, zero-border:
    // the row inside still owns every inset, so this adds an identity and nothing visual.
    surface(
        id = "studio-top-bar",
        modifier = Modifier.fillMaxWidth(),
        style = SurfaceStyle(
            background = Color.Transparent,
            border = SurfaceBorder(0f.dp, Color.Transparent),
            contentPadding = UiInsets.Zero,
            cornerRadius = 0f.dp,
        ),
    ) {
        row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = UiAlignment.Vertical.Center,
            modifier = Modifier.fillMaxWidth().height(TOP_BAR_HEIGHT)
                .padding(BAR_INSET, 0f.dp),
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
    // Identified so the shell's full-bleed invariant stays checkable. A bare row() threads
    // testTag for layout debugging but never records a semantic node, so StudioShellLayoutTest
    // could not see this bar at all and asserted on null. Transparent, zero-padding, zero-border:
    // the row inside still owns every inset, so this adds an identity and nothing visual.
    surface(
        id = "studio-status-bar",
        modifier = Modifier.fillMaxWidth(),
        style = SurfaceStyle(
            background = Color.Transparent,
            border = SurfaceBorder(0f.dp, Color.Transparent),
            contentPadding = UiInsets.Zero,
            cornerRadius = 0f.dp,
        ),
    ) {
        row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = UiAlignment.Vertical.Center,
            modifier = Modifier.fillMaxWidth().height(STATUS_BAR_HEIGHT)
                .padding(BAR_INSET, 0f.dp),
        ) {
            shadcnText("Edit mode", muted = true)
            shadcnBadge(id = "studio-status-backend", label = "Vulkan", variant = ShadcnBadgeVariant.Outline)
        }
    }
}
