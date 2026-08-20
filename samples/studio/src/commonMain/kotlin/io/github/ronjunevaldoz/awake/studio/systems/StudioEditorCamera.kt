// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.systems

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.math.Camera as CoreCamera
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.camera
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.entity
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.transform
import io.github.ronjunevaldoz.awake.scene.controls.components.ActiveCamera
import io.github.ronjunevaldoz.awake.scene.controls.components.CameraMode

private const val DEFAULT_EDITOR_CAMERA_FOV_RADIANS = 0.7853982f // 45 degrees
private const val DEFAULT_EDITOR_CAMERA_NEAR = 0.1f
private const val DEFAULT_EDITOR_CAMERA_FAR = 100f
private const val EDITOR_CAMERA_MAX_DISTANCE = 100f

/** Holds the one persistent editor (Scene-view) camera entity -- created once in `onReady`,
 * survives every `LoadExample` (unlike a scene's own authored camera, which is recreated on
 * every load). Threaded from `studioModule()`'s `onReady` block (which creates the entity) into
 * [StudioExampleDriverSystem] (which drives it and flips `Camera.isPrimary` by `StudioContract
 * .Mode`) -- these run at different times relative to `gameModule { }` construction, so a plain
 * `val Entity` can't be shared between them directly. */
internal class StudioEditorCamera {
    var entity: Entity? = null

    /** The scene's own authored camera entity, last seen -- identity changing means a new
     * example just loaded (its camera entity is recreated by `ExampleLoader` every
     * `LoadExample`), and the editor camera's orbit should re-seed from its pose. */
    var authoredCameraEntity: Entity? = null
}

/** Spawns the persistent editor camera entity in [world] using the scene authoring DSL. */
internal fun createStudioEditorCameraEntity(world: World): Entity = world.entity {
    camera(
        mode = CameraMode.FirstPerson,
        target = entity,
        lens = CoreCamera(
            eye = Vec3(6f, 4f, 8f),
            center = Vec3.ZERO,
            fovYRadians = DEFAULT_EDITOR_CAMERA_FOV_RADIANS,
            near = DEFAULT_EDITOR_CAMERA_NEAR,
            far = DEFAULT_EDITOR_CAMERA_FAR,
        ),
        primary = true,
    ) {
        maxDistance = EDITOR_CAMERA_MAX_DISTANCE
    }
    transform()
    with(ActiveCamera())
}
