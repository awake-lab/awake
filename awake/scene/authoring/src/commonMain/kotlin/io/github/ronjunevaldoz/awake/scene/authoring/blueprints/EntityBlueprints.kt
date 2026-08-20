// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.authoring.blueprints

import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.render.renderer.CullMode
import io.github.ronjunevaldoz.awake.scene.authoring.SceneGameDsl
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.EntityScope
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.camera
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.meshRenderer

/**
 * Spawns a camera entity with standard camera lens and controller setup.
 *
 * @param name The descriptive name for the camera entity.
 * @param block Optional additional configuration block on the [EntityScope].
 */
fun SceneGameDsl.cameraEntity(
    name: String,
    block: EntityScope.() -> Unit = {},
) {
    entity(name) {
        camera()
        block()
    }
}

/**
 * Spawns a mesh renderer entity with geometry, material, and cull mode setup.
 *
 * @param name The descriptive name for the mesh entity.
 * @param mesh The mesh geometry asset.
 * @param material The material asset.
 * @param cullMode The rasterizer triangle face cull mode.
 * @param block Optional additional configuration block on the [EntityScope].
 */
fun SceneGameDsl.meshEntity(
    name: String,
    mesh: Mesh,
    material: Material,
    cullMode: CullMode = CullMode.None,
    block: EntityScope.() -> Unit = {},
) {
    entity(name) {
        meshRenderer(mesh, material, cullMode)
        block()
    }
}
