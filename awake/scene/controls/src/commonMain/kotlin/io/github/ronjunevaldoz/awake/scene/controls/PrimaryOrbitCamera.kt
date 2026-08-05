// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.controls

import io.github.ronjunevaldoz.awake.core.math.OrbitCameraController
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Camera

/**
 * Owns the one ECS entity a UI-slider-driven [OrbitCameraController] projects onto -- not an ECS
 * `System` ([io.github.ronjunevaldoz.awake.scene.systems.OrbitCameraSystem]'s job for live-input
 * gameplay cameras): there's no per-entity data to iterate over here, just one demo's one debug
 * camera, so a plain lifecycle wrapper is the right level. [spawn]/[refresh]/[destroy] replace
 * the identical `cameraEntity: Entity?` + create/add/destroy boilerplate every orbit-camera demo
 * used to hand-roll.
 */
class PrimaryOrbitCamera(private val controller: OrbitCameraController) {
    /** Exposed read-only so a demo can attach extra components (e.g. `FollowControl`/
     * `LookAtControl`) to the SAME camera entity -- e.g. to switch which system drives this
     * entity's [Camera] between frames without spawning a second camera entity. `null` before
     * the first [spawn]. */
    var entity: Entity? = null
        private set

    /** Creates the camera entity if it doesn't already exist -- safe to call every frame
     * alongside a demo's own idempotent mesh/entity spawn. */
    fun spawn(world: World, target: Vec3) {
        if (entity != null) return
        val created = world.create()
        world.add(created, Camera(controller.computeCamera(target), isPrimary = true))
        entity = created
    }

    /** Re-projects [controller] onto the existing entity -- call once per frame after [spawn]
     * so slider changes and orbit motion actually reach the render camera. No-ops before the
     * first [spawn]. */
    fun refresh(world: World, target: Vec3) {
        entity?.let { world.add(it, Camera(controller.computeCamera(target), isPrimary = true)) }
    }

    /** Destroys the camera entity -- call from a demo's `onDeactivate` so switching playground
     * pages never leaves a stale camera behind. */
    fun destroy(world: World) {
        entity?.let { world.destroy(it) }
        entity = null
    }
}
