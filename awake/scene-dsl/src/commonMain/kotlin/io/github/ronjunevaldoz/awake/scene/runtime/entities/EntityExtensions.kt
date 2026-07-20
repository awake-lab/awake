// Copyright (c) Ron June Valdoz
// SPDX-License-Identifier: Apache-2.0
package io.github.ronjunevaldoz.awake.scene.runtime.entities

import io.github.ronjunevaldoz.awake.scene.runtime.SceneCameraDsl
import io.github.ronjunevaldoz.awake.scene.runtime.SceneGameDsl
import io.github.ronjunevaldoz.awake.scene.runtime.SceneTransformDsl

fun SceneGameDsl.cameraEntity(
    name: String,
    transform: SceneTransformDsl.() -> Unit = {},
    camera: SceneCameraDsl.() -> Unit = {}
) {
    entity(name) {
        transform(transform)
        camera(camera)
    }
}

fun SceneGameDsl.meshEntity(
    name: String,
    mesh: String,
    material: String,
    transform: SceneTransformDsl.() -> Unit = {}
) {
    entity(name) {
        transform(transform)
        meshRenderer(mesh = mesh, material = material)
    }
}
