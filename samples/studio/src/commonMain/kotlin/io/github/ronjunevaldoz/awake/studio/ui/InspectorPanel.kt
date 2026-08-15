// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.core.components.Name
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnTextTone
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.rememberBooleanState
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.weight
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.math.round

private val InspectorFieldLabelWidth = 72f.dp

/**
 * Docked "Inspector" panel -- flush to the window edge, no card border/radius of its own (same
 * reasoning as `drawExampleRail`'s doc comment); [shadcnSidebar]'s `header` slot supplies the
 * hairline below the title row. The trailing header icon collapses the entity list only -- panel
 * width stays the resizable group's job.
 *
 * Still read-only for v1 -- no selection state, so nothing here is editable. Each entity is a
 * [shadcnCollapsible] section: its id/name, and -- when the entity has a [Transform] -- a
 * two-column label/field grid (fixed label-column width, so Position/Rotation/Scale values all
 * start at the same x) showing its position/rotation/scale. No Animation section: nothing this
 * module reads exposes per-entity animation state yet (only `skinned-mesh` even has one, driven
 * by [io.github.ronjunevaldoz.awake.studio.examples.SkinnedExampleDriver] outside the ECS world) --
 * add one once that state is actually queryable, rather than fabricate a section with nothing
 * real to show.
 */
internal fun UiScope.drawInspectorPanel(world: World, selectedEntityId: Int?) {
    val expanded = rememberBooleanState(id = "studio-inspector-expanded", initial = true)
    shadcnSidebar(
        id = "studio-inspector",
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        header = {
            row(
                horizontalArrangement = Arrangement.spacedBy(8f.dp),
                verticalAlignment = UiAlignment.Vertical.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                shadcnText("Inspector", modifier = Modifier.weight(1f))
                shadcnButton(
                    id = "studio-inspector-toggle",
                    label = if (expanded.value) "⌃" else "⌄",
                    modifier = Modifier.width(ShadcnButtonSize.Icon.heightDp),
                    variant = ShadcnButtonVariant.Ghost,
                    size = ShadcnButtonSize.Icon,
                    onClick = { expanded.value = !expanded.value },
                )
            }
        },
    ) {
        if (expanded.value) {
            world.queryEach<Name> { entity, name ->
                if (entity.id != selectedEntityId) return@queryEach
                val rowExpanded =
                    rememberBooleanState(id = "studio-inspector-row-${entity.id}", initial = false)
                shadcnCollapsible(
                    id = "studio-inspector-row-${entity.id}",
                    title = name.value,
                    expanded = rowExpanded.value,
                    onExpandedChange = { rowExpanded.value = it },
                    modifier = Modifier.fillMaxWidth().padding(4f.dp),
                ) {
                    inspectorField("Entity", "#${entity.id}")
                    world.get<Transform>(entity)?.let { transform ->
                        inspectorField("Position", transform.position.grid())
                        inspectorField("Rotation", transform.rotation.grid())
                        inspectorField("Scale", transform.scale.grid())
                    }
                }
            }
        }
    }
}

private fun ColumnScope.inspectorField(label: String, value: String) {
    row(
        horizontalArrangement = Arrangement.spacedBy(8f.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        shadcnText(label, modifier = Modifier.width(InspectorFieldLabelWidth), tone = ShadcnTextTone.Muted)
        shadcnText(value, modifier = Modifier.weight(1f))
    }
}

private fun Vec3.grid(): String = "${x.rounded()}, ${y.rounded()}, ${z.rounded()}"

// 2 decimal places -- readable without being a wall of float noise, same precision the shadcn
// field-slider inspector controls elsewhere in this design system display at.
private const val GRID_DECIMAL_FACTOR = 100f

private fun Float.rounded(): Float = round(this * GRID_DECIMAL_FACTOR) / GRID_DECIMAL_FACTOR
