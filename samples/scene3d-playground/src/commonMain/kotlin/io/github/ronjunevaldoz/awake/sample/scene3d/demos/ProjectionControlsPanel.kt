// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.rendering.components.Camera
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnCollapsibleCard
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnFieldSliderWithValue
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnText
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import kotlin.math.PI

/**
 * Reusable projection controls. The redundant camera pose sliders (Orbit, Pitch, Zoom)
 * have been removed in favor of direct mouse/keyboard input to avoid source-of-truth conflicts.
 */
internal fun ColumnScope.renderProjectionControls(
    world: World,
    cameraEntity: Entity,
    idPrefix: String,
) {
    val camera = world.get<Camera>(cameraEntity) ?: return
    val core = camera.camera

    shadcnCollapsibleCard(
        id = "$idPrefix-controls-projection",
        expanded = true, // Simplified
        onExpandedChange = { },
        header = { _, _ -> shadcnText("Projection") },
    ) {
        core.near = shadcnFieldSliderWithValue(
            id = "$idPrefix-near",
            label = "Near",
            min = 0.01f,
            max = 5f,
            value = core.near,
        )
        core.far = shadcnFieldSliderWithValue(
            id = "$idPrefix-far",
            label = "Far",
            min = 10f,
            max = 1000f,
            value = core.far,
        )

        val fovDeg = core.fovYRadians * (180.0 / PI).toFloat()
        val newFovDeg = shadcnFieldSliderWithValue(
            id = "$idPrefix-fov",
            label = "FOV",
            min = 10f,
            max = 120f,
            value = fovDeg,
        )
        core.fovYRadians = newFovDeg * (PI / 180.0).toFloat()
    }
}
