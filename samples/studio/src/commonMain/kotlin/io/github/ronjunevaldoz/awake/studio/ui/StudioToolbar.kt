// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.render.renderer.Renderer
import io.github.ronjunevaldoz.awake.ui.designsystem.components.selection.shadcnSwitch
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layouts.Arrangement
import io.github.ronjunevaldoz.awake.ui.layouts.ColumnScope
import io.github.ronjunevaldoz.awake.ui.layouts.row
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.width

/** No state of its own -- [Renderer.wireframe]/[Renderer.shadowsEnabled] are already the real
 * source of truth, so the switch's checked value reads/writes them directly. */
internal fun ColumnScope.drawStudioToolbar(renderer: Renderer) {
    row(
        id = "studio-toolbar",
        horizontalArrangement = Arrangement.spacedBy(16f.dp),
        modifier = Modifier.width(Dimension.FillMax).height(40f.dp).padding(8f.dp),
    ) {
        renderer.wireframe = shadcnSwitch(id = "studio-wireframe", checked = renderer.wireframe, label = "Wireframe")
        renderer.shadowsEnabled = shadcnSwitch(id = "studio-shadows", checked = renderer.shadowsEnabled, label = "Shadows")
    }
}
