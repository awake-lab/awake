// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.authoring.blueprints

import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.scene.authoring.SceneGameDsl
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.EntityModifier
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.Modifier
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.camera
import io.github.ronjunevaldoz.awake.scene.authoring.dsl.meshRenderer

fun SceneGameDsl.cameraEntity(
    name: String,
    modifier: EntityModifier = Modifier(),
) {
    entity(name, modifier.camera())
}

fun SceneGameDsl.meshEntity(
    name: String,
    mesh: Mesh,
    material: Material,
    modifier: EntityModifier = Modifier(),
) {
    entity(name, modifier.meshRenderer(mesh, material))
}
