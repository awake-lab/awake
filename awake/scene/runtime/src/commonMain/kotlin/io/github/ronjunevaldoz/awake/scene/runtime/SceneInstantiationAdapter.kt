// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Light
import io.github.ronjunevaldoz.awake.scene.components.Name
import io.github.ronjunevaldoz.awake.scene.components.Transform
import kotlin.math.PI
import io.github.ronjunevaldoz.awake.scene.components.Camera as SceneCameraComponent

data class SceneNodeHandle<T>(
    val name: String?,
    val value: T,
    val children: List<SceneNodeHandle<T>>,
)

interface SceneInstantiationAdapter<Node, Instance> {
    fun createNode(node: SceneNode, parent: Node?): Node
    fun attachName(node: Node, name: String)
    fun attachTransform(node: Node, transform: SceneTransform, parent: Node?)
    fun attachCamera(node: Node, camera: SceneCamera)
    fun attachLight(node: Node, light: SceneLight)
    fun queueMeshRenderer(node: Node, meshRenderer: SceneMeshRenderer)
    fun complete(roots: List<SceneNodeHandle<Node>>): Instance
}

class AwakeWorldSceneAdapter(
    private val world: World = World(),
) : SceneInstantiationAdapter<Entity, SceneInstance> {
    private val renderableRequests = ArrayList<SceneRenderableRequest>()

    override fun createNode(node: SceneNode, parent: Entity?): Entity = world.create()

    override fun attachName(node: Entity, name: String) {
        world.add(node, Name(name))
    }

    override fun attachTransform(node: Entity, transform: SceneTransform, parent: Entity?) {
        world.add(node, transform.toComponent(parent))
    }

    override fun attachCamera(node: Entity, camera: SceneCamera) {
        world.add(node, camera.toComponent())
    }

    override fun attachLight(node: Entity, light: SceneLight) {
        world.add(node, light.toComponent())
    }

    override fun queueMeshRenderer(node: Entity, meshRenderer: SceneMeshRenderer) {
        renderableRequests += SceneRenderableRequest(node, meshRenderer)
    }

    override fun complete(roots: List<SceneNodeHandle<Entity>>): SceneInstance = SceneInstance(
        world = world,
        roots = roots.map { it.toSceneNodeInstance() },
        renderableRequests = renderableRequests.toList(),
    )
}

internal fun SceneTransform.toComponent(parent: Entity?): Transform = Transform(
    position = position.toVec3(),
    rotation = rotation.toVec3(),
    scale = scale.toVec3(),
    parent = parent,
)

internal fun SceneCamera.toComponent(): SceneCameraComponent = SceneCameraComponent(
    camera = Camera(
        eye = eye.toVec3(),
        center = center.toVec3(),
        up = up.toVec3(),
        fovYRadians = degreesToRadians(fovYDegrees),
        near = near,
        far = far,
    ),
    isPrimary = primary,
)

internal fun SceneLight.toComponent(): Light = Light(
    color = color.toVec3(),
    intensity = intensity,
    type = when (type) {
        SceneLight.Type.Directional -> Light.Type.Directional
        SceneLight.Type.Point -> Light.Type.Point
    },
)

internal fun SceneVec3.toVec3(): Vec3 = Vec3(x, y, z)

private fun degreesToRadians(degrees: Float): Float = degrees * (PI.toFloat() / 180f)

private fun SceneNodeHandle<Entity>.toSceneNodeInstance(): SceneNodeInstance = SceneNodeInstance(
    name = name,
    entity = value,
    children = children.map { it.toSceneNodeInstance() },
)
