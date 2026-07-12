// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.core.math.Camera
import io.github.ronjunevaldoz.awake.core.math.Vec3
import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.Light
import io.github.ronjunevaldoz.awake.scene.components.Camera as SceneCameraComponent
import io.github.ronjunevaldoz.awake.scene.components.Name
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import io.github.ronjunevaldoz.awake.scene.components.Transform
import kotlinx.serialization.json.Json
import kotlin.math.PI

data class SceneInstance(
    val world: World,
    val roots: List<SceneNodeInstance>,
    val renderableRequests: List<SceneRenderableRequest>
)

data class SceneNodeInstance(
    val name: String?,
    val entity: Entity,
    val children: List<SceneNodeInstance>
)

data class SceneRenderableRequest(
    val entity: Entity,
    val meshRenderer: SceneMeshRenderer
)

private val SceneJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    explicitNulls = false
}

object SceneLoader {
    fun encode(document: SceneDocument, json: Json = SceneJson): String {
        return json.encodeToString(document)
    }

    fun decode(text: String, json: Json = SceneJson): SceneDocument {
        return json.decodeFromString(text)
    }

    suspend fun loadFromResource(path: String, json: Json = SceneJson): SceneDocument {
        return decode(readResourceBytes(path).decodeToString(), json)
    }

    /** [flipYForClipSpace] comes from the active `Renderer` (see its own doc comment) --
     * a scene document itself carries no opinion on backend clip-space convention. */
    fun instantiate(document: SceneDocument, flipYForClipSpace: Boolean, world: World = World()): SceneInstance {
        val renderableRequests = mutableListOf<SceneRenderableRequest>()
        val roots = document.nodes.map { node ->
            instantiateNode(world, node, null, flipYForClipSpace, renderableRequests)
        }
        return SceneInstance(world, roots, renderableRequests)
    }

    private fun instantiateNode(
        world: World,
        node: SceneNode,
        parent: Entity?,
        flipYForClipSpace: Boolean,
        renderableRequests: MutableList<SceneRenderableRequest>
    ): SceneNodeInstance {
        val entity = world.create()
        world.add(entity, node.transform.toTransform(parent))
        node.name?.takeIf { it.isNotBlank() }?.let { world.add(entity, Name(it)) }
        node.camera?.let { world.add(entity, it.toComponent(flipYForClipSpace)) }
        node.light?.let { world.add(entity, it.toComponent()) }
        node.meshRenderer?.let { renderableRequests += SceneRenderableRequest(entity, it) }

        val children = node.children.map { child ->
            instantiateNode(world, child, entity, flipYForClipSpace, renderableRequests)
        }
        return SceneNodeInstance(node.name, entity, children)
    }

    private fun SceneTransform.toTransform(parent: Entity?): Transform {
        return Transform(
            position = position.toVec3(),
            rotation = rotation.toVec3(),
            scale = scale.toVec3(),
            parent = parent
        )
    }

    private fun SceneCamera.toComponent(flipYForClipSpace: Boolean): SceneCameraComponent {
        return SceneCameraComponent(
            camera = Camera(
                eye = eye.toVec3(),
                center = center.toVec3(),
                up = up.toVec3(),
                fovYRadians = degreesToRadians(fovYDegrees),
                near = near,
                far = far,
                flipYForClipSpace = flipYForClipSpace
            ),
            isPrimary = primary
        )
    }

    private fun SceneLight.toComponent(): Light {
        return Light(
            color = color.toVec3(),
            intensity = intensity,
            type = when (type) {
                SceneLight.Type.Directional -> Light.Type.Directional
                SceneLight.Type.Point -> Light.Type.Point
            }
        )
    }

    private fun SceneVec3.toVec3(): Vec3 {
        return Vec3(x, y, z)
    }

    private fun degreesToRadians(degrees: Float): Float {
        return degrees * (PI.toFloat() / 180f)
    }
}

fun SceneDocument.instantiate(flipYForClipSpace: Boolean, world: World = World()): SceneInstance {
    return SceneLoader.instantiate(this, flipYForClipSpace, world)
}

fun SceneInstance.attachRenderableComponents(
    factory: (SceneRenderableRequest) -> MeshRenderer
) {
    renderableRequests.forEach { request ->
        world.add(request.entity, factory(request))
    }
}
