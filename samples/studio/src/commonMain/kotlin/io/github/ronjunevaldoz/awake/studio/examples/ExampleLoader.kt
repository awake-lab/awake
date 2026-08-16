// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.examples

import io.github.ronjunevaldoz.awake.core.math.Aabb
import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.runtime.SceneDocument
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.SceneLoader
import io.github.ronjunevaldoz.awake.scene.runtime.SceneMeshRenderer
import io.github.ronjunevaldoz.awake.scene.runtime.fromWorld

/**
 * Replaces three demos' hand-rolled onActivate/onDeactivate with one generic routine. Split
 * into a suspend [preload] and a non-suspend [activate]: `System.update` is not suspend and
 * [SceneGameRuntime] exposes no `CoroutineScope` to launch from inside one, so every document
 * is resolved once up front, mirroring how every other suspend load in this codebase
 * (`GltfViewerDemo.preload()`, `SkinnedMeshDemo.preload()`) runs once in `onReady`.
 */
internal class ExampleLoader {
    private val documents = mutableMapOf<String, SceneDocument>()
    private var activeRoots: List<Entity> = emptyList()

    /**
     * The authored mesh/material asset IDs of every renderable in the active scene.
     *
     * A live [io.github.ronjunevaldoz.awake.scene.rendering.components.MeshRenderer] holds GPU
     * handles, which cannot be serialized, so [SceneLoader.fromWorld] refuses to export one
     * without a resolver. This is the resolver's data: recorded at instantiate time, when the
     * authored IDs are still in hand.
     */
    private val authoredRenderables = mutableMapOf<Entity, SceneMeshRenderer>()
    private val boundsByEntity = mutableMapOf<Int, Aabb>()

    /**
     * Local-space bounds per entity, for picking.
     *
     * A live `MeshRenderer` holds GPU handles, so the geometry it was built from is gone by the
     * time anything wants to hit-test it. The asset library knows that geometry at creation
     * time, which is the only place these can come from without reading vertices back off the
     * GPU.
     */
    fun boundsOf(entityId: Int): Aabb? = boundsByEntity[entityId]

    /** Rebuilds a document from the live world, so an edit made in the inspector is what gets
     * saved -- not the document that was loaded. */
    fun exportActive(world: World, name: String): SceneDocument = SceneLoader.fromWorld(
        world = world,
        name = name,
        meshRenderer = { entity, _ ->
            requireNotNull(authoredRenderables[entity]) {
                "Entity $entity has a MeshRenderer this loader never instantiated."
            }
        },
    )

    suspend fun preload() {
        StudioExamples.forEach { example ->
            documents[example.id] = SceneLoader.loadFromResource(example.scenePath)
        }
    }

    fun activate(exampleId: String, runtime: SceneGameRuntime) {
        teardown(runtime.world)
        val example = requireNotNull(StudioExamples.find { it.id == exampleId }) { "Unknown example '$exampleId'." }
        val document = requireNotNull(documents[exampleId]) { "Example '$exampleId' was not preloaded." }
        val instance = SceneLoader.instantiate(document, runtime.world)
        val library = runtime.requireAssetLibrary()
        authoredRenderables.clear()
        boundsByEntity.clear()
        instance.renderableRequests.forEach { request ->
            runtime.world.add(request.entity, library.resolve(runtime, request))
            authoredRenderables[request.entity] = request.meshRenderer
            StudioMeshBounds[request.meshRenderer.mesh]?.let { boundsByEntity[request.entity.id] = it }
        }
        example.onActivated?.invoke(instance, runtime.world)
        activeRoots = instance.roots.map { it.entity }
    }

    fun teardown(world: World) {
        activeRoots.forEach { world.destroy(it) }
        activeRoots = emptyList()
        authoredRenderables.clear()
        boundsByEntity.clear()
    }
}
