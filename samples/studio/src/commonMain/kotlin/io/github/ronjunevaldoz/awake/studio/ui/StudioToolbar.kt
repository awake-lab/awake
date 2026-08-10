// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.UiSemanticRole
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnBadge
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnBadgeVariant
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.scope.recordSemantic
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme
import io.github.ronjunevaldoz.awake.ui.unstyled.HeroIcons
import io.github.ronjunevaldoz.awake.ui.unstyled.components.icon

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
internal fun UiScope.drawStudioTopBar(renderer: Renderer, onPlay: () -> Unit) {
    val bounds = row(
        id = "studio-top-bar",
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = UiAlignment.Vertical.Center,
        modifier = Modifier.width(Dimension.FillMax).height(TOP_BAR_HEIGHT).padding(BAR_INSET, 0f.dp),
    ) {
        shadcnText("Awake Studio")
        shadcnButton(
            id = "studio-top-bar-play",
            modifier = Modifier.width(ShadcnButtonSize.Icon.heightDp),
            variant = ShadcnButtonVariant.Ghost,
            size = ShadcnButtonSize.Icon,
            onClick = onPlay,
        ) {
            icon(HeroIcons.Solid20Mini.play)
        }
        row(horizontalArrangement = Arrangement.spacedBy(16f.dp)) {
            renderer.wireframe = shadcnSwitch(id = "studio-wireframe", checked = renderer.wireframe, label = "Wireframe")
            renderer.shadowsEnabled = shadcnSwitch(id = "studio-shadows", checked = renderer.shadowsEnabled, label = "Shadows")
        }
    }
    // row() itself never records a semantic node (only column()'s ColumnScope/RowScope/BoxScope
    // overloads do, and only when they resolve to a visual surface) -- record this one directly
    // so layout tests can find the top bar's own bounds by id.
    recordSemantic(role = UiSemanticRole.Panel, id = "studio-top-bar", bounds = bounds)
}

/** Full-width status bar: edit-mode label at the left, the active render backend at the right --
 * muted caption typography, matching a docked IDE's bottom strip. The backend is a fixed label,
 * not read live from the running window -- nothing in this `ui` package can reach that without
 * depending on `app`. */
internal fun UiScope.drawStudioStatusBar() {
    val bounds = row(
        id = "studio-status-bar",
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = UiAlignment.Vertical.Center,
        modifier = Modifier.width(Dimension.FillMax).height(STATUS_BAR_HEIGHT).padding(BAR_INSET, 0f.dp),
    ) {
        shadcnText("Edit mode", muted = true, style = Style { textSize(theme.typography.caption) })
        shadcnBadge("Vulkan", variant = ShadcnBadgeVariant.Outline)
    }
    // See drawStudioTopBar's matching comment -- row() records no semantic node on its own.
    recordSemantic(role = UiSemanticRole.Panel, id = "studio-status-bar", bounds = bounds)
}
