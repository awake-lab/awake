// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

@AwakeSceneDsl
class SceneAssetsDsl internal constructor() {
    private val meshFactories = linkedMapOf<String, SceneMeshFactory>()
    private val materialFactories = linkedMapOf<String, SceneMaterialFactory>()
    private val rendererFactories = linkedMapOf<SceneRenderableKey, SceneMeshRendererFactory>()

    fun mesh(name: String, factory: SceneMeshFactory) {
        require(name.isNotBlank()) { "Scene mesh names must not be blank." }
        meshFactories[name] = factory
    }

    fun material(name: String, factory: SceneMaterialFactory) {
        require(name.isNotBlank()) { "Scene material names must not be blank." }
        materialFactories[name] = factory
    }

    fun renderer(
        mesh: String,
        material: String,
        factory: SceneMeshRendererFactory
    ) {
        rendererFactories[SceneRenderableKey(mesh, material)] = factory
    }

    internal fun buildLibrary(): SceneAssetLibrary = SceneAssetLibrary(
        meshFactories = meshFactories.toMap(),
        materialFactories = materialFactories.toMap(),
        rendererFactories = rendererFactories.toMap()
    )
}
