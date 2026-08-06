// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.controls

import io.github.ronjunevaldoz.awake.core.math.OrbitCameraController
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Transform

/**
 * Bundles [PrimaryOrbitCamera] with a [Transform]-only "placement" entity a demo can attach
 * its own mesh-facing components to ([io.github.ronjunevaldoz.awake.scene.components
 * .MeshRenderer], `SkinnedPose`) via [entity] -- `GltfViewerDemo`/`SkinnedMeshDemo` hand-rolled
 * this exact `placementEntity: Entity?` + create/add(Transform())/destroy triple identically
 * before this existed, back when they drew directly (`Renderer.drawTexturedMesh`/
 * `drawSkinnedMesh`) instead of through `RenderSystem`. Not for
 * [io.github.ronjunevaldoz.awake.sample.scene3d.demos.RotatingCubeDemo]-shaped demos that spawn
 * their own entity by hand -- there [PrimaryOrbitCamera] alone is the right level, wrapping
 * this rig around an already-real entity would just add a second, unrelated one.
 */
class OrbitCameraDemoRig(val camera: OrbitCameraController) {
    val primaryCamera = PrimaryOrbitCamera(camera)

    /** Exposed read-only so a demo can attach extra mesh-facing components to this same
     * entity (matching [PrimaryOrbitCamera.entity]'s own reason for being public) -- `null`
     * before the first [spawn]. */
    var entity: Entity? = null
        private set

    /** Creates the placement entity (if it doesn't already exist) and the camera entity --
     * safe to call every frame alongside a demo's own idempotent mesh spawn, same as
     * [PrimaryOrbitCamera.spawn]. */
    fun spawn(world: World, target: Vec3) {
        if (entity == null) {
            val created = world.create()
            world.add(created, Transform())
            entity = created
        }
        primaryCamera.spawn(world, target)
    }

    /** Re-projects [camera] onto the existing camera entity -- call once per frame after
     * [spawn], same as [PrimaryOrbitCamera.refresh]. */
    fun refresh(world: World, target: Vec3) = primaryCamera.refresh(world, target)

    /** Destroys both the placement and camera entities -- call from a demo's `onDeactivate`. */
    fun destroy(world: World) {
        entity?.let { world.destroy(it) }
        entity = null
        primaryCamera.destroy(world)
    }
}
