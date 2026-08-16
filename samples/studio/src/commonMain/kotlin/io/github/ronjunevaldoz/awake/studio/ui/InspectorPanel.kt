// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.ui

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.core.components.Name
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.api.layout.UiAlignment
import io.github.ronjunevaldoz.awake.ui.designsystem.components.ShadcnTextTone
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnButton
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsible
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnEmpty
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnInput
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnSidebar
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonSize
import io.github.ronjunevaldoz.awake.ui.designsystem.styles.ShadcnButtonVariant
import io.github.ronjunevaldoz.awake.ui.headless.Arrangement
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.RowScope
import io.github.ronjunevaldoz.awake.ui.headless.UiScope
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxHeight
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height
import io.github.ronjunevaldoz.awake.ui.headless.padding
import io.github.ronjunevaldoz.awake.ui.headless.rememberBooleanState
import io.github.ronjunevaldoz.awake.ui.headless.rememberStateValue
import io.github.ronjunevaldoz.awake.ui.headless.row
import io.github.ronjunevaldoz.awake.ui.headless.weight
import io.github.ronjunevaldoz.awake.ui.headless.width
import kotlin.math.round

// Wide enough for "Entity" without wrapping -- the only row still laid out label-beside-value.
private val InspectorFieldLabelWidth = 96f.dp

/** Tall enough to clear the field row above it -- see [vec3Field]. */
private val FIELD_LABEL_HEIGHT = 26f.dp

/**
 * Docked "Inspector" panel -- flush to the window edge, no card border/radius of its own; the
 * resizable group's own handle is the seam against the viewport, and [shadcnSidebar]'s `header`
 * slot supplies the hairline below the title row. The trailing header icon collapses the entity list only -- panel
 * width stays the resizable group's job.
 *
 * Shows the hierarchy's selected entity only, and an empty state when nothing is selected -- a
 * blank panel otherwise reads as a broken inspector rather than an empty one.
 *
 * The selected entity is a [shadcnCollapsible] section: its id/name, and -- when the entity has
 * a [Transform] -- editable position/rotation/scale rows writing into the live component. No
 * Animation section: nothing this
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
            if (selectedEntityId == null) {
                // Short strings on purpose: shadcnEmpty does not wrap, and the inspector panel
                // is ~280px wide, so a sentence runs off the panel edge.
                shadcnEmpty(title = "No selection", description = "Pick an entity")
            }
            world.queryEach<Name> { entity, name ->
                if (entity.id != selectedEntityId) return@queryEach
                // Expanded by default: only the selected entity is listed, so collapsing it by
                // default hid every property behind a click for no gain.
                val rowExpanded =
                    rememberBooleanState(id = "studio-inspector-row-${entity.id}", initial = true)
                shadcnCollapsible(
                    id = "studio-inspector-row-${entity.id}",
                    title = name.value,
                    expanded = rowExpanded.value,
                    onExpandedChange = { rowExpanded.value = it },
                    modifier = Modifier.fillMaxWidth().padding(4f.dp),
                ) {
                    inspectorField("Entity", "#${entity.id}")
                    world.get<Transform>(entity)?.let { transform ->
                        vec3Field("studio-inspector-${entity.id}-position", "Position", transform.position)
                        vec3Field("studio-inspector-${entity.id}-rotation", "Rotation", transform.rotation)
                        vec3Field("studio-inspector-${entity.id}-scale", "Scale", transform.scale)
                    }
                }
            }
        }
    }
}

/**
 * An editable X/Y/Z group, writing straight into [value] -- the live component, so a committed
 * edit is visible in the viewport on the next frame with no copy-back step.
 *
 * Label above the axes rather than beside them: the panel is ~288px, and a label column wide
 * enough for "Scale" left the three inputs too narrow to show "0.0" without clipping.
 *
 * Plain text inputs rather than a scrub/drag field: the design system has no numeric drag field,
 * and three [shadcnInput]s that already parse and commit correctly beat a new component built to
 * make the same edit prettier. Drag-to-scrub is the upgrade, not a prerequisite.
 */
private fun ColumnScope.vec3Field(id: String, label: String, value: Vec3) {
    // The label needs its own vertical room: a bare text row and the field row below it were
    // laid out tight enough that descenders sat on the boxes.
    shadcnText(
        label,
        tone = ShadcnTextTone.Muted,
        modifier = Modifier.height(FIELD_LABEL_HEIGHT).padding(top = 8f.dp),
    )
    row(
        horizontalArrangement = Arrangement.spacedBy(4f.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 4f.dp),
    ) {
        axisInput("$id-x", value.x) { value.x = it }
        axisInput("$id-y", value.y) { value.y = it }
        axisInput("$id-z", value.z) { value.z = it }
    }
}

/**
 * One axis. The text is remembered rather than re-derived from [current] every frame, so a
 * half-typed value ("-", "1.") survives until it parses.
 *
 * The remembered text is dropped whenever [current] changes underneath it: a system (a spin
 * control, the camera) owns the value too, and a stale field would otherwise write its old
 * number back over the system's every frame, freezing the animation.
 */
private fun RowScope.axisInput(id: String, current: Float, onCommit: (Float) -> Unit) {
    val text = rememberStateValue(id, "text") { current.rounded().toString() }
    val lastExternal = rememberStateValue(id, "external") { current }
    if (lastExternal.value != current) {
        text.value = current.rounded().toString()
        lastExternal.value = current
    }
    val edited = shadcnInput(id = id, value = text.value, modifier = Modifier.weight(1f))
    if (edited == text.value) return
    text.value = edited
    val parsed = edited.toFloatOrNull() ?: return
    onCommit(parsed)
    lastExternal.value = parsed
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

// 2 decimal places -- readable without being a wall of float noise, same precision the shadcn
// field-slider inspector controls elsewhere in this design system display at.
private const val GRID_DECIMAL_FACTOR = 100f

private fun Float.rounded(): Float = round(this * GRID_DECIMAL_FACTOR) / GRID_DECIMAL_FACTOR
