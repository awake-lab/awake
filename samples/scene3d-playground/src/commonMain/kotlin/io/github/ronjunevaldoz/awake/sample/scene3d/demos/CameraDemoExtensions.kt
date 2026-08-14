// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.sample.scene3d.demos

import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.ecs.ensure
import io.github.ronjunevaldoz.awake.scene.controls.components.ActiveCamera
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraComponent
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode
import io.github.ronjunevaldoz.awake.scene.controls.components.MovementControl
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.ui.api.dp
import io.github.ronjunevaldoz.awake.ui.designsystem.components.shadcnToggleGroup
import io.github.ronjunevaldoz.awake.ui.headless.ColumnScope
import io.github.ronjunevaldoz.awake.ui.headless.Modifier
import io.github.ronjunevaldoz.awake.ui.headless.fillMaxWidth
import io.github.ronjunevaldoz.awake.ui.headless.height

internal fun ColumnScope.renderCameraModeToggle(
    mode: CameraMode,
    onModeChange: (CameraMode) -> Unit,
) {
    shadcnToggleGroup(
        id = "camera-mode-toggle",
        options = listOf("1st Person", "3rd Person", "Cinematic", "Top-Down"),
        selectedIndex = mode.ordinal,
        modifier = Modifier.fillMaxWidth().height(32f.dp),
        onIndexChange = { onModeChange(CameraMode.entries[it]) },
    )
}

/**
 * Standard camera dispatch for Awake archetypes.
 */
internal fun updateDemoCamera(
    world: World,
    cameraEntity: Entity,
    targetEntity: Entity, // The character/object being followed
    panningEntity: Entity?, // Entity used for Top-Down panning
    onPanningEntityCreated: (Entity) -> Unit,
    defaultMode: CameraMode = CameraMode.ThirdPerson,
) {
    // Ensure this camera entity has the ActiveCamera tag
    if (!world.has(cameraEntity, ActiveCamera::class)) {
        // Collect first: removing a component while iterating its own family cache mutates
        // the array that iteration is indexing into.
        val previouslyActive = world.query(ActiveCamera::class)
        previouslyActive.forEach { world.remove<ActiveCamera>(it) }
        world.add(cameraEntity, ActiveCamera())
    }

    val config = world.ensure(cameraEntity) { CameraComponent().apply { mode = defaultMode } }

    // Which entity WASD drives depends on the mode: top-down pans a dedicated proxy, every
    // other mode moves the subject itself.
    val driver = if (config.mode == CameraMode.TopDown) {
        panningEntity ?: world.create().also { created ->
            world.add(
                created,
                Transform().apply {
                    world.get<Transform>(targetEntity)?.let { position.set(it.position) }
                },
            )
            onPanningEntityCreated(created)
        }
    } else {
        targetEntity
    }

    // Only touch MovementControl when the driver actually changes. Removing and re-adding it
    // every frame invalidates the whole query cache twice per frame and allocates a fresh
    // component, purely to zero two floats that PlayerInputSystem overwrites moments later.
    if (driver != targetEntity) world.remove<MovementControl>(targetEntity)
    panningEntity?.takeIf { it != driver }?.let { world.remove<MovementControl>(it) }
    world.ensure(driver, ::MovementControl)

    config.targetEntity = driver
}
