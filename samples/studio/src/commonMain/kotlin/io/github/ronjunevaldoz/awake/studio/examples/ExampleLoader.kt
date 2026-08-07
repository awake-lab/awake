// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.studio.examples

import io.github.ronjunevaldoz.awake.ecs.Entity
import io.github.ronjunevaldoz.awake.ecs.World
import io.github.ronjunevaldoz.awake.scene.runtime.SceneDocument
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameRuntime
import io.github.ronjunevaldoz.awake.scene.runtime.SceneLoader

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
        instance.renderableRequests.forEach { request ->
            runtime.world.add(request.entity, library.resolve(runtime, request))
        }
        example.onActivated?.invoke(instance, runtime.world)
        activeRoots = instance.roots.map { it.entity }
    }

    fun teardown(world: World) {
        activeRoots.forEach { world.destroy(it) }
        activeRoots = emptyList()
    }
}
