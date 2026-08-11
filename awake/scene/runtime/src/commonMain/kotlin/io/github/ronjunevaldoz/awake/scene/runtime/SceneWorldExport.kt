// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.core.components.Name
import io.github.ronjunevaldoz.awake.scene.core.components.SpinControl
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.rendering.components.Light
import io.github.ronjunevaldoz.awake.scene.rendering.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.rendering.components.PbrMaterial
import kotlin.math.PI
import io.github.ronjunevaldoz.awake.scene.rendering.components.Camera as SceneCameraComponent

/**
 * Rebuilds an authored [SceneDocument] from the serializable part of a live [World].
 *
 * Only entities with [Transform] become nodes: a SceneNode always has a local transform, while
 * runtime-only entities such as input state have no place in a scene file. Children are rebuilt
 * from [Transform.parent], and local TRS values are exported rather than the derived world
 * matrix that TransformSystem recalculates each frame.
 *
 * A live [MeshRenderer] deliberately needs [meshRenderer] to recover its authored mesh and
 * material asset IDs. Its component stores GPU handles, which cannot be serialized back to a
 * document. Requiring a resolver prevents a save from silently stripping visible geometry.
 */
fun SceneLoader.fromWorld(
    world: World,
    name: String? = null,
    meshRenderer: ((Entity, MeshRenderer) -> SceneMeshRenderer)? = null,
): SceneDocument {
    val transforms = linkedMapOf<Entity, Transform>()
    world.queryEach(Transform::class) { entity, transform -> transforms[entity] = transform }

    transforms.forEach { (entity, transform) ->
        val parent = transform.parent
        require(parent == null || parent in transforms) {
            "Cannot export $entity: parent $parent has no Transform and cannot be a SceneNode."
        }
    }

    val childrenByParent = linkedMapOf<Entity?, MutableList<Entity>>()
    transforms.forEach { (entity, transform) ->
        childrenByParent.getOrPut(transform.parent) { mutableListOf() }.add(entity)
    }

    val visiting = mutableSetOf<Entity>()
    val exported = mutableSetOf<Entity>()
    fun exportNode(entity: Entity): SceneNode {
        check(visiting.add(entity)) { "Cannot export World: Transform parent links contain a cycle at $entity." }
        val transform = checkNotNull(transforms[entity])
        val node = SceneNode(
            name = world.get<Name>(entity)?.value,
            transform = transform.toSceneTransform(),
            components = world.sceneComponents(entity, meshRenderer),
            children = childrenByParent[entity].orEmpty().map(::exportNode),
        )
        visiting.remove(entity)
        exported += entity
        return node
    }

    val roots = childrenByParent[null].orEmpty().map(::exportNode)
    check(exported.size == transforms.size) {
        "Cannot export World: Transform parent links contain a cycle with no root."
    }
    return SceneDocument(name = name, nodes = roots)
}

private fun World.sceneComponents(
    entity: Entity,
    meshRenderer: ((Entity, MeshRenderer) -> SceneMeshRenderer)?,
): List<SceneComponent> = buildList {
    get<SceneCameraComponent>(entity)?.let { add(it.toSceneComponent()) }
    get<Light>(entity)?.let { add(it.toSceneComponent()) }
    get<PbrMaterial>(entity)?.let { add(it.toSceneComponent()) }
    get<SpinControl>(entity)?.let { add(it.toSceneComponent()) }
    get<MeshRenderer>(entity)?.let { renderer ->
        val resolved = requireNotNull(meshRenderer?.invoke(entity, renderer)) {
            "Cannot export $entity: MeshRenderer needs a meshRenderer resolver for its asset IDs."
        }
        add(resolved)
    }
}

private fun Transform.toSceneTransform(): SceneTransform = SceneTransform(
    position = position.toSceneVec3(),
    rotation = rotation.toSceneVec3(),
    scale = scale.toSceneVec3(),
)

private fun SceneCameraComponent.toSceneComponent(): SceneCamera = SceneCamera(
    eye = camera.eye.toSceneVec3(),
    center = camera.center.toSceneVec3(),
    up = camera.up.toSceneVec3(),
    fovYDegrees = camera.fovYRadians * (180f / PI.toFloat()),
    near = camera.near,
    far = camera.far,
    primary = isPrimary,
)

private fun Light.toSceneComponent(): SceneLight = SceneLight(
    color = color.toSceneVec3(),
    intensity = intensity,
    type = when (type) {
        Light.Type.Directional -> SceneLight.Type.Directional
        Light.Type.Point -> SceneLight.Type.Point
    },
)

private fun PbrMaterial.toSceneComponent(): ScenePbrMaterial = ScenePbrMaterial(
    metallic = metallic,
    roughness = roughness,
)

private fun SpinControl.toSceneComponent(): SceneSpinControl = SceneSpinControl(
    radians = radians,
    speed = speed,
)

private fun Vec3.toSceneVec3(): SceneVec3 = SceneVec3(x, y, z)
