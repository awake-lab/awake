// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.core.colors.Color
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.core.components.Name
import io.github.ronjunevaldoz.awake.ui.UiScope
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebarMenu
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.dp
import io.github.ronjunevaldoz.awake.ui.layout.Dimension
import io.github.ronjunevaldoz.awake.ui.modifier.Modifier
import io.github.ronjunevaldoz.awake.ui.modifier.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.modifier.height
import io.github.ronjunevaldoz.awake.ui.modifier.width
import io.github.ronjunevaldoz.awake.ui.style.Style
import io.github.ronjunevaldoz.awake.ui.theme

/** Flat scene outliner. Parent links do not exist in Studio yet, so nesting would be decorative. */
internal fun UiScope.drawHierarchyPanel(
    world: World,
    selectedEntityId: Int?,
    onSelectEntity: (Int) -> Unit,
) {
    shadcnSidebar(
        id = "studio-hierarchy",
        modifier = Modifier.width(Dimension.FillMax).height(Dimension.FillMax),
        style = Style { shape(0f.dp); borderWidth(0f.dp) },
        header = { shadcnText("Scene") },
    ) {
        shadcnSidebarMenu {
            world.queryEach<Name> { entity, name ->
                val selected = entity.id == selectedEntityId
                shadcnButton(
                    id = "studio-hierarchy-entity-${entity.id}",
                    modifier = Modifier.fillMaxWidth().height(32f.dp),
                    variant = if (selected) ShadcnButtonVariant.Primary else ShadcnButtonVariant.Ghost,
                    style = Style {
                        background(if (selected) theme.colors.accent else Color.Transparent)
                        foreground(if (selected) theme.colors.accentForeground else theme.colors.foreground)
                    },
                    centered = false,
                    onClick = { onSelectEntity(entity.id) },
                ) { shadcnText(name.value) }
            }
        }
    }
}
