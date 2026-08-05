// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.controls

import io.github.ronjunevaldoz.awake.core.math.OrbitCameraController
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Transform

/**
 * Bundles [PrimaryOrbitCamera] with the empty [Transform]-only "placement" entity a demo that
 * draws its own mesh directly (bypassing [io.github.ronjunevaldoz.awake.scene.components
 * .MeshRenderer]/[io.github.ronjunevaldoz.awake.scene.systems.RenderSystem], e.g. via
 * `Renderer.drawTexturedMesh`/`drawSkinnedMesh`) still spawns so its world-space placement stays
 * inspectable the same way every other demo's is -- `GltfViewerDemo`/`SkinnedMeshDemo` hand-rolled
 * this exact `placementEntity: Entity?` + create/add(Transform())/destroy triple identically
 * before this existed. Not for [io.github.ronjunevaldoz.awake.sample.scene3d.demos
 * .RotatingCubeDemo]-shaped demos, whose main entity already carries real gameplay components
 * ([Transform]/`SpinControl`/`MeshRenderer`) -- there [PrimaryOrbitCamera] alone is the right
 * level, wrapping this rig around a real entity would just add a second, unrelated one.
 */
class OrbitCameraDemoRig(val camera: OrbitCameraController) {
    val primaryCamera = PrimaryOrbitCamera(camera)
    private var placementEntity: Entity? = null

    /** Creates the placement entity (if it doesn't already exist) and the camera entity --
     * safe to call every frame alongside a demo's own idempotent mesh spawn, same as
     * [PrimaryOrbitCamera.spawn]. */
    fun spawn(world: World, target: Vec3) {
        if (placementEntity == null) {
            val entity = world.create()
            world.add(entity, Transform())
            placementEntity = entity
        }
        primaryCamera.spawn(world, target)
    }

    /** Re-projects [camera] onto the existing camera entity -- call once per frame after
     * [spawn], same as [PrimaryOrbitCamera.refresh]. */
    fun refresh(world: World, target: Vec3) = primaryCamera.refresh(world, target)

    /** Destroys both the placement and camera entities -- call from a demo's `onDeactivate`. */
    fun destroy(world: World) {
        placementEntity?.let { world.destroy(it) }
        placementEntity = null
        primaryCamera.destroy(world)
    }
}
