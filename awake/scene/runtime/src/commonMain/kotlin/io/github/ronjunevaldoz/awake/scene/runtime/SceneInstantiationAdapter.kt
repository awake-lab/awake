// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.core.components.Name
import io.github.ronjunevaldoz.awake.scene.core.components.SpinControl
import io.github.ronjunevaldoz.awake.scene.core.components.Transform
import io.github.ronjunevaldoz.awake.scene.rendering.components.Light
import io.github.ronjunevaldoz.awake.scene.rendering.components.PbrMaterial
import kotlin.math.PI
import io.github.ronjunevaldoz.awake.scene.rendering.components.Camera as SceneCameraComponent

data class SceneNodeHandle<T>(
    val name: String?,
    val value: T,
    val children: List<SceneNodeHandle<T>>,
)

interface SceneInstantiationAdapter<Node, Instance> {
    fun createNode(node: SceneNode, parent: Node?): Node
    fun attachName(node: Node, name: String)
    fun attachTransform(node: Node, transform: SceneTransform, parent: Node?)
    fun attachComponent(node: Node, component: SceneComponent)
    fun complete(roots: List<SceneNodeHandle<Node>>): Instance
}

class AwakeWorldSceneAdapter(
    private val world: World = World(),
) : SceneInstantiationAdapter<Entity, Scene> {
    private val renderableRequests = ArrayList<SceneRenderableRequest>()

    override fun createNode(node: SceneNode, parent: Entity?): Entity = world.create()

    override fun attachName(node: Entity, name: String) {
        world.add(node, Name(name))
    }

    override fun attachTransform(node: Entity, transform: SceneTransform, parent: Entity?) {
        world.add(node, transform.toComponent(parent))
    }

    // Exhaustive over the sealed SceneComponent: a new authored component stops this compiling
    // until it is mapped, instead of round-tripping through JSON and being dropped on load.
    override fun attachComponent(node: Entity, component: SceneComponent) {
        when (component) {
            is SceneCamera -> world.add(node, component.toComponent())
            is SceneLight -> world.add(node, component.toComponent())
            is ScenePbrMaterial -> world.add(node, component.toComponent())
            is SceneSpinControl -> world.add(node, component.toComponent())
            // Mesh and material are live GPU handles the loader can't build; the caller resolves
            // these against its own renderer (see SceneRenderableRequest).
            is SceneMeshRenderer -> {
                renderableRequests += SceneRenderableRequest(node, component)
            }
        }
    }

    override fun complete(roots: List<SceneNodeHandle<Entity>>): Scene = Scene(
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

internal fun ScenePbrMaterial.toComponent(): PbrMaterial = PbrMaterial(
    metallic = metallic,
    roughness = roughness,
)

internal fun SceneSpinControl.toComponent(): SpinControl = SpinControl().also {
    it.radians = radians
    it.speed = speed
}

internal fun SceneVec3.toVec3(): Vec3 = Vec3(x, y, z)

private fun degreesToRadians(degrees: Float): Float = degrees * (PI.toFloat() / 180f)

private fun SceneNodeHandle<Entity>.toSceneNodeInstance(): SceneNodeInstance = SceneNodeInstance(
    name = name,
    entity = value,
    children = children.map { it.toSceneNodeInstance() },
)
