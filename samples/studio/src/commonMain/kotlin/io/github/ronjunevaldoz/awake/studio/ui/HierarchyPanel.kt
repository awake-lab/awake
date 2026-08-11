// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.core.components.Name
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.height

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
                shadcnButton(
                    id = "studio-hierarchy-entity-${entity.id}",
                    modifier = Modifier.fillMaxWidth().height(32f.dp),
                    variant = if (selected) ShadcnButtonVariant.Primary else ShadcnButtonVariant.Ghost,
                    label = name.value,
                    onClick = { onSelectEntity(entity.id) },
                )
            }
        }
    }
}
