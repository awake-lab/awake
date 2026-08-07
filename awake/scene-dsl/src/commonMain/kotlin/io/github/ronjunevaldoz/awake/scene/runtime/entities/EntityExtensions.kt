// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime.entities

import io.github.ronjunevaldoz.awake.render.material.Material
import io.github.ronjunevaldoz.awake.render.mesh.Mesh
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameDsl
import io.github.ronjunevaldoz.awake.scene.runtime.dsl.EntityModifier
import io.github.ronjunevaldoz.awake.scene.runtime.dsl.Modifier
import io.github.ronjunevaldoz.awake.scene.runtime.dsl.camera
import io.github.ronjunevaldoz.awake.scene.runtime.dsl.meshRenderer

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
