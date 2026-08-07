// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.core.utils.readResourceBytes
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer
import kotlinx.serialization.json.Json

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

    fun instantiate(document: SceneDocument, world: World = World()): SceneInstance {
        return instantiate(document, AwakeWorldSceneAdapter(world))
    }

    /** Adapter-driven instantiation path: the scene DSL/model stays Awake-owned, while the
     * execution target can vary as long as it implements [SceneInstantiationAdapter]. */
    fun <Node, Instance> instantiate(
        document: SceneDocument,
        adapter: SceneInstantiationAdapter<Node, Instance>
    ): Instance {
        SceneValidator.requireValid(document)
        val roots = document.nodes.mapIndexed { index, node ->
            instantiateNode(adapter, node, parent = null, path = node.name?.takeIf { it.isNotBlank() } ?: "#$index")
        }
        return adapter.complete(roots)
    }

    private fun <Node> instantiateNode(
        adapter: SceneInstantiationAdapter<Node, *>,
        node: SceneNode,
        parent: Node?,
        path: String
    ): SceneNodeHandle<Node> {
        val created = adapter.createNode(node, parent)
        adapter.attachTransform(created, node.transform, parent)
        node.name?.takeIf { it.isNotBlank() }?.let { adapter.attachName(created, it) }
        node.camera?.let { adapter.attachCamera(created, it) }
        node.light?.let { adapter.attachLight(created, it) }
        node.meshRenderer?.let { adapter.queueMeshRenderer(created, it) }

        val children = node.children.mapIndexed { index, child ->
            instantiateNode(
                adapter = adapter,
                node = child,
                parent = created,
                path = child.name?.takeIf { it.isNotBlank() } ?: "$path/#$index"
            )
        }
        return SceneNodeHandle(node.name, created, children)
    }
}

fun SceneDocument.instantiate(world: World = World()): SceneInstance {
    return SceneLoader.instantiate(this, world)
}

fun SceneInstance.attachRenderableComponents(
    factory: (SceneRenderableRequest) -> MeshRenderer
) {
    renderableRequests.forEach { request ->
        world.add(request.entity, factory(request))
    }
}
