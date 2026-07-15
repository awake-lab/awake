// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime

import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.scene.components.MeshRenderer

typealias SceneMeshFactory = SceneGameRuntime.() -> Mesh
typealias SceneMaterialFactory = SceneGameRuntime.() -> Material
typealias SceneMeshRendererFactory = SceneGameRuntime.() -> MeshRenderer

data class SceneRenderableKey(
    val mesh: String,
    val material: String
)

class SceneAssetLibrary(
    private val meshFactories: Map<String, SceneMeshFactory>,
    private val materialFactories: Map<String, SceneMaterialFactory>,
    private val rendererFactories: Map<SceneRenderableKey, SceneMeshRendererFactory>
) {
    private val meshes = linkedMapOf<String, Mesh>()
    private val materials = linkedMapOf<String, Material>()

    fun requireMesh(runtime: SceneGameRuntime, name: String): Mesh {
        return meshes.getOrPut(name) {
            val factory = checkNotNull(meshFactories[name]) {
                "No scene mesh named '$name' is registered."
            }
            runtime.factory()
        }
    }

    fun requireMaterial(runtime: SceneGameRuntime, name: String): Material {
        return materials.getOrPut(name) {
            val factory = checkNotNull(materialFactories[name]) {
                "No scene material named '$name' is registered."
            }
            runtime.factory()
        }
    }

    fun resolve(
        runtime: SceneGameRuntime,
        request: SceneRenderableRequest
    ): MeshRenderer {
        val key = SceneRenderableKey(
            mesh = request.meshRenderer.mesh,
            material = request.meshRenderer.material
        )
        val customRenderer = rendererFactories[key]
        if (customRenderer != null) {
            return runtime.customRenderer()
        }
        return MeshRenderer(
            mesh = requireMesh(runtime, key.mesh),
            material = requireMaterial(runtime, key.material)
        )
    }

    fun dispose() {
        meshes.values.forEach { mesh -> mesh.destroy() }
        materials.values.forEach { material -> material.destroy() }
        meshes.clear()
        materials.clear()
    }
}
