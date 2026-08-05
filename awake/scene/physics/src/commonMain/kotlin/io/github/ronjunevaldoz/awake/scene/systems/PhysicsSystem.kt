// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.systems

import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.System
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.physics.BodyHandle
import io.github.ronjunevaldoz.awake.physics.BodyTransform
import io.github.ronjunevaldoz.awake.physics.PhysicsWorld
import io.github.ronjunevaldoz.awake.scene.components.PhysicsBody
import io.github.ronjunevaldoz.awake.scene.components.Transform

/**
 * Bridges [PhysicsBody]/[Transform] ECS components to a live [physicsWorld]. This is
 * constructed and `update()`-called by game/sample code, never wired into `SceneRuntime`
 * itself, since only the caller knows which concrete [PhysicsWorld] backend (jolt-jni today)
 * to construct.
 *
 * Body creation is lazy and one-shot per entity: [PhysicsBody.handle] starts `null` (it's
 * constructed before any [PhysicsWorld] exists, e.g. straight out of `SceneLoader`), so the
 * first [update] that sees a `null` handle creates the backing body from that entity's
 * paired [Transform] and stores the returned [BodyHandle] both on the component and in
 * [handleToEntity] (needed because [PhysicsWorld.syncTransforms] hands back handles, not
 * entities, and this system is the only place able to bridge that back to ECS state).
 *
 * [PhysicsWorld.step]/[PhysicsWorld.syncTransforms] are each called exactly once per
 * [update] -- never once per body -- matching [PhysicsWorld.syncTransforms]'s own "batched
 * readback" contract.
 */
class PhysicsSystem(
    private val physicsWorld: PhysicsWorld
) : System {
    private val handleToEntity = HashMap<BodyHandle, Entity>()

    override fun update(world: World, delta: Float) {
        val family = world.family<Transform, PhysicsBody>()
        family.forEach { entity, transform, physicsBody ->
            if (physicsBody.handle == null) {
                val handle = physicsWorld.createBody(
                    physicsBody.shape,
                    transform.position,
                    transform.rotation,
                    physicsBody.motionType
                )
                physicsBody.handle = handle
                handleToEntity[handle] = entity
            }
        }

        physicsWorld.step(delta)

        val bodyTransforms = physicsWorld.syncTransforms()
        for (bodyTransform in bodyTransforms) {
            syncBodyTransform(world, bodyTransform)
        }
    }

    private fun syncBodyTransform(world: World, bodyTransform: BodyTransform) {
        val entity = handleToEntity[bodyTransform.handle] ?: return
        val transform = world.get<Transform>(entity) ?: return
        transform.position.x = bodyTransform.position.x
        transform.position.y = bodyTransform.position.y
        transform.position.z = bodyTransform.position.z
        transform.rotation.x = bodyTransform.rotation.x
        transform.rotation.y = bodyTransform.rotation.y
        transform.rotation.z = bodyTransform.rotation.z
    }
}
