// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Name
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.headless.input.text.text
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.layouts.RowScope
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.padding
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.rememberBooleanState

/** Read-only for v1 -- no selection state to track yet, so this owns nothing; it re-queries
 * [World] fresh every frame, the same "no state to hold" shape any pure-display panel has in
 * an immediate-mode UI. */
internal fun RowScope.drawInspectorPanel(world: World) {
    shadcnSidebar(
        id = "studio-inspector",
        modifier = Modifier.width(280f.dp).height(Dimension.FillMax),
    ) {
        world.queryEach<Name> { entity, name ->
            val rowExpanded = rememberBooleanState(id = "studio-inspector-${entity.id}", initial = false)
            shadcnCollapsible(
                id = "studio-inspector-row-${entity.id}",
                title = name.value,
                expanded = rowExpanded.value,
                onExpandedChange = { rowExpanded.value = it },
                modifier = Modifier.width(Dimension.FillMax).padding(4f.dp),
            ) {
                text(label = "Entity #${entity.id}")
            }
        }
    }
}
