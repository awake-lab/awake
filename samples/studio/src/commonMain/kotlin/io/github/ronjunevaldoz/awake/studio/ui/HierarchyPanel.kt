// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.core.components.Name
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenuItem
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth

/** Flat scene outliner. Parent links do not exist in Studio yet, so nesting would be decorative. */
internal fun UiScope.drawHierarchyPanel(
    world: World,
    selectedEntityId: Int?,
    onSelectEntity: (Int) -> Unit,
) {
    shadcnSidebar(
        id = "studio-hierarchy",
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        header = { shadcnText("Scene") },
    ) {
        shadcnSidebarMenu {
            world.queryEach<Name> { entity, name ->
                val selected = entity.id == selectedEntityId
                shadcnSidebarMenuItem(
                    id = "studio-hierarchy-entity-${entity.id}",
                    label = name.value,
                    active = selected,
                    onClick = { onSelectEntity(entity.id) },
                )
            }
        }
    }
}
